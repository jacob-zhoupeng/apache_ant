package org.moxy.oak.plugin;
import java.io.*;
import java.util.*;
import java.net.*;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class PartyLine2User implements PartyLine2Client, Runnable{

 private String nick;
 private PartyLine2Channel channel;
 private Socket mySocket;
 private BufferedReader input;
 private DataOutputStream output;
 private PartyLine2Server server;
 private String mask;

 public PartyLine2User(String mask, String name, Socket s, BufferedReader input, DataOutputStream output, PartyLine2Server server){
  while(name.startsWith("#")){
   name = name.substring(1);
  }
  if(name.equals("")){
   try{
    output.writeBytes("bad nick");
    s.close();
    server.removeUser(this);
   }catch(Exception e){e.printStackTrace();}
  }
  this.mySocket = s;
  this.mask = mask;
  this.nick = name.toLowerCase();
  this.input = input;
  this.output = output;
  this.server = server;
  (new Thread(this)).start();
 }

 public String getMask(){return mask;}

 public void setChannel(PartyLine2Channel channel){
  this.channel = channel;
 }

 public PartyLine2Channel getChannel(){return channel;}

 public String getName(){
  return nick;
 }
 public void sendMessage(int type, String nick, String message){
  try{
   if(nick.equals(this.nick) && (type == 0 || type == 7)){return;}
    switch(type){
     case 0 : output.writeBytes("<"+nick+"> "+message); break;//Channel
     case 1 : output.writeBytes("*"+nick+"* "+message); break;//private
     case 3 : output.writeBytes("-"+nick+"- "+message); break;//notice
     case 6 : output.writeBytes("%"+nick+"% "+message); break;//global notice
     case 4 : output.writeBytes("@"+nick+"@ "+message); break;//wallops
     case 5 : output.writeBytes("!SYSTEM NOTICE! "+message); break;//system notice
     case 7 : output.writeBytes(nick+" "+message);break;
     case 8 : output.writeBytes(nick+" has joined the channel");break;
     case 9 : output.writeBytes(nick+" has left the channel");break;
    }
   output.writeBytes("\n");
  }catch(IOException ioe){ioe.printStackTrace();}
 }

 public void run(){
  try{
   String line;
   while(true){
    line = input.readLine();
    if(line == null){
     mySocket.close();
     server.removeUser(this);
     channel.remove(this);
     server.sendMessage(9,channel.getName(),nick,null);
     return;
    }
    if(line.equals("")){continue;}
    if(line.startsWith(".") && !line.startsWith("..")){
     if(line.toLowerCase().startsWith(".me ")){
      line = line.substring(4);
      server.sendMessage(PartyLine2Server.CHANNEL_ACTION,channel.getName(),nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".msg ")){
      line = line.substring(5);
      String to = line.substring(0,line.indexOf(" "));
      line = line.substring(line.indexOf(" ")+1);
      server.sendMessage(PartyLine2Server.PRIVATE_MSG,to.toLowerCase(),nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".notice ")){
      line = line.substring(8);
      String to = line.substring(0,line.indexOf(" "));
      line = line.substring(line.indexOf(" ")+1);
      server.sendMessage(PartyLine2Server.NOTICE_MSG,to.toLowerCase(),nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".wallops ")){ // && status == OWNER){
      line = line.substring(9);
      server.sendMessage(PartyLine2Server.WALLOPS,null,nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".snotice ")){// && status == OWNER){
      line = line.substring(9);
      server.sendMessage(PartyLine2Server.SYSTEM_NOTICE,null,nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".gnotice ")){// && status == OWNER){
      line = line.substring(9);
      server.sendMessage(PartyLine2Server.GLOBAL_NOTICE,null,nick,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".join ")){
      line = line.substring(6);
      server.joinChannel(this,line);
      continue;
     }
     if(line.toLowerCase().startsWith(".who")){
      Enumeration enum = server.getClients().elements();
      String mask = "*";
      if(line.indexOf(" ")>-1){
       mask = line.substring(line.indexOf(" ")+1);
      }
      PartyLine2User current;
      while(enum.hasMoreElements()){
       current = (PartyLine2User)enum.nextElement();
       if(isMatch(mask,current.getName())){
        output.writeBytes(current.getName()+" on "+current.getChannel().getName()+" "+
                          current.getMask()+"\n");
       }
      }
      output.writeBytes("Who done.");
      continue;
     }
     continue;
    }
    if(line.startsWith("."))line = line.substring(1);
    server.sendMessage(PartyLine2Server.CHANNEL_MSG,channel.getName(),nick,line);
   }
  }catch(IOException ioe){
   ioe.printStackTrace();
   try{
    mySocket.close();
    server.removeUser(this);
    channel.remove(this);
   }catch(Exception e){e.printStackTrace();}
   server.sendMessage(9,channel.getName(),nick,null);
  }
 }


    public static boolean isMatch(String mask, String nick) {
        mask = mask.trim();
        if (mask.equalsIgnoreCase("*")) {
            return true;
        }
        if (mask.startsWith("*")) {
            int end = -1;
            if (mask.indexOf("*",mask.indexOf("*") + 1) == -1) {
                end = mask.length();
            } else {
                end = mask.indexOf("*",mask.indexOf("*") + 1);
            }
            if (end == -1) {
                return false;
            }
            String tmask = mask.substring(1, end).trim();
            if (nick.indexOf(tmask) == -1) {
                return false;
            } else {
                mask = mask.substring(end).trim();
                nick = nick.substring(nick.indexOf(tmask)).trim();
                return isMatch(mask, nick);
            }
        }
        int end = -1;
        if (mask.indexOf("*") == -1) {
            end = mask.length();
        } else {
            end = mask.indexOf("*");
        }
        if (end == -1) {
            return false;
        }
        String tmask = mask.substring(0, end).trim();
        if (nick.startsWith(tmask)) {
            if (end == mask.length()) {
                return true;
            }
            mask = mask.substring(end).trim();
            nick = nick.substring(tmask.length()).trim();
            return isMatch(mask, nick);
        } else {
            return false;
        }
    }


}
