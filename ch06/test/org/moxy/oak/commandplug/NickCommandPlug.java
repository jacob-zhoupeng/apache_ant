package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
/**
 * Class NickCommandPlug - write a description of the class here
 * 
 * @author: 
 * date: 
 */
public class NickCommandPlug implements CommandPlug{
 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }
 public String[] getCommands(){
  String ret[] = {"NICK"};
  return ret;
 }
 public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
  if(command.equals("NICK")){
   return "Changes the nick of the bot on an a connection.";
  }
  return null;
 }

 public String[] getLongHelpDescription(String command){
  if(!command.equals("NICK")){return null;}
  String ret[] = new String[3];
  ret[0] = "/ctcp bot NICK [on <connection>] <nick>";
  ret[1] = " <nick> is the new nick the bot should try to take.";
  ret[2] = " <connection> the connection identifier to change the nick on.";
  return ret;
 }

 public void doCommand(CommandConnection connection, String fnick,
			String command, String param){
  IRCLine line = new IRCLine(param);
  if(!command.equalsIgnoreCase("NICK"))return;//not nick command
  IRCConnection con = null;
  if(connection instanceof IRCConnection)
   con = (IRCConnection)connection;
  String nick = line.getNextToken();
  if(nick.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   nick = line.getNextToken();
  }
//  nick = line.getNextToken();
  if(con == null)return;//unknown connection
  doNick(connection,con,fnick,nick);
 }

 private void doNick(CommandConnection connection, IRCConnection tcon, String fnick, String nick){
  if(!bot.checkAccess("nick",null,fnick,connection,tcon)){
   connection.sendReply(fnick, "NICK You're not authorized to change the nick of the bot on this server or network.");
   return;
  }
  tcon.changeNick(nick);
 }


}

