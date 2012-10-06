package org.moxy.oak.plugin;
import java.util.*;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class PartyLine2Server{

 public Hashtable clients = new Hashtable();//pingable
 public Oak bot;

 public PartyLine2Server(Oak bot){
  clients.put("#lobby",new PartyLine2Channel("#lobby"));
  this.bot = bot;
 }

 public Vector getClients(){
  Vector v = new Vector();
  Enumeration enum = clients.elements();
  PartyLine2Client current;
  while(enum.hasMoreElements()){
   current = (PartyLine2Client)enum.nextElement();
   if(current instanceof PartyLine2User){
    v.add(current);
   }
  }
  return v;
 }

 public void joinChannel(PartyLine2User user,String channel){
  PartyLine2Channel curChan = user.getChannel();
  curChan.part(user);
  if(curChan.isEmpty() && !curChan.getName().equals("#lobby"))
   clients.remove(channel);
  channel = channel.toLowerCase();
  curChan = (PartyLine2Channel)clients.get(channel);
  if(curChan == null){
   curChan = new PartyLine2Channel(channel);
   clients.put(channel,curChan);
  }
  user.setChannel(curChan);
  curChan.join(user);
 }

 public void sendMessage(int type, String to, String from, String message){
  if(to == null){
   Enumeration enum = clients.elements();
   PartyLine2Client current;
   while(enum.hasMoreElements()){
    current = (PartyLine2Client)enum.nextElement();
    if(current.getName().startsWith("#"))continue;
    current.sendMessage(type,from,message);
   }
   return;
  }
  PartyLine2Client target = (PartyLine2Client)clients.get(to);
  if(target == null){return;}//unknown
  target.sendMessage(type,from,message);
 }

 public void addUser(PartyLine2User user){
  clients.put(user.getName(),user);
  ((PartyLine2Channel)clients.get("#lobby")).join(user);
  user.setChannel((PartyLine2Channel)clients.get("#lobby"));
 }

 public void removeUser(PartyLine2User user){
  clients.remove(user.getName());
 }

 public boolean isValidNick(String nick){
  return (clients.get(nick) == null);
 }
 

 //message types
 public static final int CHANNEL_MSG = 0;
 public static final int PRIVATE_MSG = 1;
 public static final int NOTICE_MSG = 3;
 public static final int WALLOPS = 4;
 public static final int SYSTEM_NOTICE = 5;
 public static final int GLOBAL_NOTICE = 6;
 public static final int CHANNEL_ACTION = 7;
 public static final int CHANNEL_JOIN = 8;
 public static final int CHANNEL_PART = 9;
}
