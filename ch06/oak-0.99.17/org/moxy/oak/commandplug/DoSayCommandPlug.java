package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
public class DoSayCommandPlug implements CommandPlug{
 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;//set's the bot
  //we don't bother about the sm for this plug no need in it.
 }

 /**
  * Returns Say and do
  * @since 1.0
  */
 public String[] getCommands(){
  String[] ret = {"SAY","DO"};
  return ret;
 }

 /**
  * Returns botcontrol since that's where these commands fit in at.
  * @since 1.0
  */
 public String getGroup(){return "BOTCONTROL";}

 /**
  * Tells what say and do do.
  */
 public String getShortHelpDescription(String command){
  if(command.equals("DO"))
   return "Preforms an action on a channel.";
  if(command.equals("SAY"))
   return "Says something to a channel.";
  return null;
 }

 /**
  * Describes what params say and do take.
  * @since 1.0
  */
 public String[] getLongHelpDescription(String command){
  String ret[] = null;
  if(command.equals("DO")){
   ret = new String[3];
   ret[0] = "/ctcp BOT DO <channel> <action>";
   ret[1] = "<channel> is the channel to send the action to.";
   ret[2] =  "<action> is the actual text of the action.";
  }
  if(command.equals("SAY")){
   ret = new String[3];
   ret[0] = "/ctcp BOT SAY <channel> <message>";
   ret[1] = "<chhannel> is teh channel to send the message to.";
   ret[2] = "<message> what to say to the channel.";
  }
  return ret;
 }

 /**
  * Handles the say and do commands.
  * @since 1.0
  */
 public void doCommand(CommandConnection connection, String lnick, 
			String command, String params){
  IRCLine line = new IRCLine(params);
  String channel = line.getNextToken();//chan to preform on
  IRCConnection con = null;//connection to preform on
  if(connection instanceof IRCConnection)
   con = (IRCConnection)connection;
  if(channel.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   channel = line.getNextToken();
  }
  String mesg = line.getRemaining();//what to say.
  if(mesg == "")return;  //nothing to say
  if(command.equals("SAY")){
   doSay(connection,con,lnick,channel,mesg);
   return;
  }
  if(command.equals("DO")){
   doDo(connection,con,lnick,channel,mesg);
  }
 }

 /**
  * handles dirty details of saying stuff.
  * @since 1.0
  */
 private void doSay(CommandConnection connection, IRCConnection tcon, String nick, String channel,String mesg){
  if(channel.startsWith("#") || channel.startsWith("&")){
   //check access for channel sayings.
   if(!bot.checkAccess("say",channel,nick,connection,tcon)){
    connection.sendReply(nick, "SAY You're not authorized to say anything to that server, network, or channel.");
    return;
   }
  }else{
   //check access for private sayings.
   if(!bot.checkAccess("privsay",channel,nick,connection,tcon)){
    connection.sendReply(nick, "SAY You're not authorized to say anything to that server, network, or nick.");
    return;
   }
  }
  //if access checks out send the message.
  tcon.sendPrivMsg(channel, mesg);
 }

 /**
  * handles dirty details of the do command.
  * @since 1.0
  */
 private void doDo(CommandConnection connection, IRCConnection tcon, String nick, String channel, String mesg){
  if(channel.startsWith("#") || channel.startsWith("&")){
   //check access for channel doings
   if(!bot.checkAccess("do",channel,nick,connection,tcon)){
    connection.sendReply(nick, "SAY You're not authorized to perform actions on that server, network, or channel.");
    return;
   }
  }else{
   //check access for private actions.
   if(!bot.checkAccess("privdo",channel,nick,connection,tcon)){
    connection.sendReply(nick, "SAY You're not authorized to say anything to that server, network, or nick.");
    return;
   }
  }
  //settings check out preform action
  tcon.sendCTCPMsg(channel, "ACTION "+mesg); 
 }   

}
