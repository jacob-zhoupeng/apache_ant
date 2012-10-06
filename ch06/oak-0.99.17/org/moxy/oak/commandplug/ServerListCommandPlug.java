package org.moxy.oak.commandplug;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.irc.*;
import org.moxy.oak.security.*;
import java.util.*;
public class ServerListCommandPlug implements CommandPlug{
 Oak bot;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }
 public String[] getCommands(){
  String[] ret = {"SERVERLIST"};
  return ret;
 }

 public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
  return "Lists all the servers the bot is currently connected to.";
 }
 public String[] getLongHelpDescription(String command){
  String ret[] = new String[1];
  ret[0] = "/ctcp bot SERVERLIST";
  return ret;
 }

 public void doCommand(CommandConnection connection, String nick,
			String command, String params){
  if(!bot.checkAccess("serverlist",nick, connection)){
   connection.sendReply(nick, "SERVERLIST You're not authorized to access this command");
   return;
  }
  connection.sendReply(nick,"SERVERLIST Listing servers.");
  Enumeration connections = bot.getAllConnections();
  IRCConnection current;
  while(connections.hasMoreElements()){
   current = (IRCConnection)connections.nextElement();
   if(current.connected())
    connection.sendReply(nick, "SERVERLIST "+
		bot.getConnectionIdentifier(current)+"). "+current.getServer()+
		" "+ current.getPort() +" "+current.getNick()+" connected");
   else
    connection.sendReply(nick,"SERVERLIST "+
     bot.getConnectionIdentifier(current)+"). "+current.getServer()+
    	" <disconnected> "+current.getLastError());
  }
  connection.sendReply(nick, "SERVERLIST Finished listing connections.");
 }
     
  

}
