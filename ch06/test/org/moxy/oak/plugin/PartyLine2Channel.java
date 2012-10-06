package org.moxy.oak.plugin;
import java.util.*;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class PartyLine2Channel implements PartyLine2Client{
 private final String name;
 private Vector users = new Vector();

 public PartyLine2Channel(String name){
  this.name = name.toLowerCase();
 }
 public String getName(){
  return name;
 }

 public void sendMessage(int type, String nick, String message){
  if(type != PartyLine2Server.CHANNEL_MSG && 
     type != PartyLine2Server.CHANNEL_JOIN &&
     type != PartyLine2Server.CHANNEL_PART &&
     type != PartyLine2Server.CHANNEL_ACTION)return;
  Enumeration enum = users.elements();
  while(enum.hasMoreElements()){
   ((PartyLine2User)(enum.nextElement())).sendMessage(type,nick,message);
  }
 }

 public void part(PartyLine2User user){
  users.remove(user);
  sendMessage(9,user.getName()," has left the channel.");
 }

 public void join(PartyLine2User user){
  users.add(user);
  sendMessage(8,user.getName()," has joined the channel.");
 }

 public void remove(PartyLine2User user){
  users.remove(user);
 }
 
 public boolean isEmpty(){return users.isEmpty();}

}
