package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;

public class ConnectionPlug implements CommandPlug{

 Oak bot = null;
 BotSecurityManager bsm = null;

 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b; //set the bot
  this.bsm = bsm;//set the security manager.
 } 

 /**
  * Say's this plugin belongs to the botcontrol group.
  * @since 1.0
  */
 public String getGroup(){return "BOTCONTROL";}

 /**
  * Explains what connect and disconnect do.
  * @since 1.0
  */
 public String getShortHelpDescription(String command){
  if(command.equals("CONNECT")){return "Connects to an IRC server";}
  if(command.equals("DISCONNECT")){return "Disconnects from an IRC server";}
  return "No description for "+command;
 }
 /**
  * Gives indepth help about how connect and
  * disconnect work.
  * @scine 1.0
  */
 public String[] getLongHelpDescription(String command){
  String ret[] = null;
  if(command.equals("CONNECT")){
   ret = new String[4];
   ret[0] = "/ctcp BOT CONNECT <SERVER> [on=<connection identifier>] [port=<port>] [nick=<nick>]";
   ret[1] = " on=<connection identifier> specifies the unique identifier which oak addresses the  connection with";
   ret[2] =  " port=<port> Allows the override of the port number the default is 6667";
   ret[3] =  " nick=<nick> Allows the specification of the nick the bot should try to use";
  }
  if(command.equals("DISCONNECT")){
   ret = new String[1];
   ret[0] = "/ctcp BOT DISCONNECT <connection identifier>";
  }
  return ret;  
 }

 /**
  * Returns Connect and disconnect.
  * @since 1.0
  */
 public String[] getCommands(){
  String[] ret = {"CONNECT","DISCONNECT"};
  return ret;
 }

 /**
  * Handles the connect/disconnect commands.
  * @since 1.0
  **/
 public void doCommand(CommandConnection connection, String fromLongNick, 
			String command, String params){
  IRCLine line = new IRCLine(params);
  if(command.equalsIgnoreCase("CONNECT")){ 
   String host = line.getNextToken();
   int port = 6667;
   String identifier = null;//custom identifier if one is passed.
   String nick = null;
    if(connection instanceof IRCConnection)
     nick = ((IRCConnection)connection).getNick();
    else
     bot.getCurrentConnection().getNick();//if a nick isn't specified use
					//same nick as current connection.
   String name = null;
    if(connection instanceof IRCConnection)
     name = ((IRCConnection)connection).getFullName();
    else
     bot.getCurrentConnection().getFullName();
   String temp = "";
   while(line.hasMoreTokens()){
    temp = line.getNextToken();
    if(temp.toLowerCase().startsWith("on=")){//set identifier
     identifier=temp.substring(3);
     continue;
    }
    if(temp.toLowerCase().startsWith("port=")){//set port num.
     try{
      port = Integer.parseInt(temp.substring(5));
     }catch(NumberFormatException nfe){nfe.printStackTrace();}
     continue;
    }
    if(temp.toLowerCase().startsWith("nick=")){//set nick
     nick = temp.substring(5);
     continue;
    }
    if(temp.toLowerCase().startsWith("name=")){//set name.
     name = temp.substring(5);
     continue;
    }
   }
   doConnect(connection,fromLongNick,new IRCCommandConnection(host,port,nick,name,bot.isIdentdEnabled()),identifier);
   return;
  }
  if(command.equalsIgnoreCase("DISCONNECT")){
   IRCCommandConnection con = bot.getIRCConnection(line.getNextToken());
   if(con == null){
    connection.sendReply(fromLongNick,"DISCONNECT Connection not found.");
    return;
   }
   String mesg = "";
   while(line.hasMoreTokens())//get all the params as quit message.
    mesg= mesg+line.getNextToken()+" ";
   doDisconnect(connection,con,fromLongNick,mesg);
  }
 } 

 /**
  * Handles the dirty details of connecting to an irc server.
  * @since 1.0
  */
 private void doConnect(CommandConnection connection, String lnick, IRCCommandConnection newConnection, String identifier){
  //check access before continuing.
  if(!bot.checkAccess("connect",null,lnick,connection,newConnection)){
   connection.sendReply(lnick, "CONNECT You're not authorized to connect to that server or network.");
   return;
  }
  try{
   if(identifier != null)
    bot.addNewConnection(newConnection,identifier);
   else
    bot.addNewConnection(newConnection);
  }catch(Exception e){
   connection.sendReply(lnick, "CONNECT Error connecting couldn't find host.");
  }
 }
  

 /**
  * Handles dirty details about disconnecting.
  * @since 1.0
  */
 private void doDisconnect(CommandConnection connection, IRCCommandConnection tconnection, String nick, String mesg){
  if(!bot.checkAccess("disconnect",null,nick,connection,tconnection)){
   connection.sendReply(nick, "DISCONNECT You're not authorized to disconnect from that server or network.");
   return;
  }
  if(mesg != ""){
   tconnection.disconnect(mesg);
   bot.removeConnection(tconnection);
   return;
  }
  tconnection.disconnect();
  bot.removeConnection(tconnection);
 }



}
