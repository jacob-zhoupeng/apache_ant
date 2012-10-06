package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
/**
 * Class KickCommandPlug - Provides a command to make the bot kick people
 * out of the room.
 * 
 * @author: Marcus Wenzel
 * date: 
 */
public class KickCommandPlug implements CommandPlug{

 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }
 public String[] getCommands(){
  String ret[] = {"KICK"};
  return ret;
 }
 public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
  return "Kicks someone from a channel.";
 }
 public String[] getLongHelpDescription(String command){
  String ret[] = new String[4];
  ret[0] = "/ctcp BOT KICK <channel> <nick> [mesg]";
  ret[1] = "<channel> is the channel to kick the person from.";
  ret[2] = "<nick> the person to kick.";
  ret[3] = "mesg the reason for the kick.";
  return ret;
 }
 public void doCommand(CommandConnection connection, String nick,
			String command, String param){
  IRCLine line = new IRCLine(param);
  if(!command.equals("KICK"))return;//not kick command.
  String channel = line.getNextToken();
  IRCConnection con = null;
   if(connection instanceof IRCConnection)con = (IRCConnection)connection;
  if(channel.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   channel = line.getNextToken();
  }
  if(con == null){
   connection.sendReply(nick,"KICK couldn't find that connection.");
   return;
  }
  String kicked = line.getNextToken();
  String mesg = line.getRemaining();
  doKick(connection,con,nick,channel,kicked,mesg);
 }

 private void doKick(CommandConnection connection, IRCConnection tcon, String kicker, String channel, String nick, String mesg){
  //check access to kick command.
  if(!bot.checkAccess("kick",channel,kicker,connection,tcon)){
   connection.sendReply(kicker,"KICK You're not authorized to kick people off that server, network, or channel.");
   return;
  }
  //kicks with custom message if present.
  if(mesg == null)mesg = "Bot Power! (http://oak.sourceforge.net)";
  tcon.kick(channel,nick,mesg);
 }



}
