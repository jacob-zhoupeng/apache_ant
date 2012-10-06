package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import java.util.*;
import org.moxy.oak.security.*;
public class TopicCommandPlug implements CommandPlug{
 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }
 public String[] getCommands(){
  String[] ret = {"TOPIC"};
  return ret;
 }
 public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
  return "Changes the topic of the room";
 }
 public String[] getLongHelpDescription(String command){
  String ret[] = new String[3];
   ret[0] = "/ctcp BOT TOPIC [on <connection>] <room> <desc>";
   ret[1] = " <room> the name of the room to change the topic in.";
   ret[2] = " <desc> is the new topic.";
  return ret;
 }

 public void doCommand(CommandConnection connection, String lnick,
			String command, String param){
  if(!command.equals("TOPIC"))return;
  String room = param;
  String topic = "";
  IRCCommandConnection con = null;
  if(connection instanceof IRCCommandConnection)con = (IRCCommandConnection)connection;
  if(param.toLowerCase().startsWith("on ")){
   param = param.substring(param.indexOf(" ")+1);
   if(param.indexOf(" ")<=0){return;}//send error
   con = bot.getIRCConnection(param.substring(0,param.indexOf(" ")));
  }
  if(con == null)return;//send error
  if(param.indexOf(" ")>0){
   room = param.substring(0,param.indexOf(" "));
   topic = param.substring(param.indexOf(" ")+1);
  }
  doTopic(connection,con,lnick,room,topic);  
 }

 private void doTopic(CommandConnection connection,IRCCommandConnection tcon, String nick, String room, String mesg){
  if(!bot.checkAccess("topic",room,nick,connection,tcon)){
   connection.sendReply(nick, "TOPIC You do not have authorization to change the topic for that room.");
   return;
  }
  tcon.setTopic(room,mesg);
 }
}
