package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;

public class JoinPartCommandPlug implements CommandPlug{

 Oak bot;

 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }
 public String[] getCommands(){
  String[] ret = {"JOIN","PART"};
  return ret;
 }

 public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
  if(command.equals("JOIN")){return "Joins a channel.";}
  if(command.equals("PART")){return "Parts a channel.";}
  return null;
 }
 public String[] getLongHelpDescription(String command){
  String ret[] = null;
  if(command.equals("JOIN")){
   ret = new String[4];
   ret[0] = "/ctcp BOT JOIN [on=<identifier>] <channel> [key]";
   ret[1] = "<identifier> is the connection identifier to join on.";
   ret[2] = "<channel> is the channel to join.";
   ret[3] = "[key] key to join the channel.";
  }
  if(command.equals("PART")){
   ret = new String[4];
   ret[0] = "/ctcp BOT PART [on=<identifier>] <channel> [mesg]";
   ret[1] = "<identifier> is the connection identifier to part.";
   ret[2] = "<channel> is the channel to part.";
   ret[3] = "mesg is the message to part with.";
  }
  return ret;
 }

 public void doCommand(CommandConnection connection, String nick,
			String command, String param){
  IRCLine line = new IRCLine(param);
  IRCConnection con = null;
  if(connection instanceof IRCConnection)
   con = (IRCConnection)connection;
  String channel = line.getNextToken();//chan to join
  if(channel.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   channel = line.getNextToken();
  }
  if(con == null){
   connection.sendReply(nick, command.toUpperCase()+" couldn't find that connection.");
   return;
  }
  if(command.equals("JOIN")){
   doJoin(connection,con,channel,nick,line.getRemaining());
   return;
  }
  if(command.equals("PART")){
   doPart(connection,con,channel,nick,line.getRemaining());
   return;
  }
 } 

 private void doJoin(CommandConnection connection, IRCConnection tconnection, String channel, String nick, String key){
  //check access to join command
  if(!bot.checkAccess("join",channel,nick,connection, tconnection)){
   connection.sendReply(nick, "JOIN You're not authorized to join that channel on that server or network.");
   return;
  }
  //if granted join.
  tconnection.joinChannel(channel,key);
 }
	
 private void doPart(CommandConnection connection, IRCConnection tcon, String channel, String nick, String reason){
  //check access to part command.
  if(!bot.checkAccess("part",channel,nick,connection,tcon)){
   connection.sendReply(nick, "PART You're not authorized to part that channel on that server or network.");
   return;
  }
  //exit with optional reason if present.
  if(reason.equals(""))
   tcon.partChannel(channel);
  else
   tcon.partChannel(channel,reason);
 }



}
