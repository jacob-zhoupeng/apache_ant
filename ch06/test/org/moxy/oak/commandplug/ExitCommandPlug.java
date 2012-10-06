package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import java.util.*;
import org.moxy.oak.security.*;
public class ExitCommandPlug implements CommandPlug{
 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;//set bot ignore sm
 }

 /**
  * Retuns exit 'cause that's what we handle.
  * @since 1.0
  */
 public String[] getCommands(){
  String[] ret = {"EXIT"};
  return ret;
 }

 /**
  * Returns botcontrol 'cause that's where this command falls.
  * @since 1.0
  */
 public String getGroup(){return "BOTCONTROL";}
 /**
  * tells what exit does.
  * @since 1.0
  */
 public String getShortHelpDescription(String command){
  return "Makes the but quit running.";
 }

 /**
  * Explains the params exit can take.
  * @since 1.0
  */
 public String[] getLongHelpDescription(String command){
  String ret[] = new String[2];
   ret[0] = "/ctcp BOT EXIT [desc]";
   ret[1] = "desc is the quit message to use on all connections.";
  return ret;
 }

 /**
  * Handles the exit command.
  * @since 1.0
  */
 public void doCommand(CommandConnection connection, String lnick,
			String command, String param){
  if(!command.equals("EXIT"))return;
  doExit(connection,lnick,param);  
 }

 /**
  * Handles the diry work of the exit command.
  * @since 1.0
  */
 private void doExit(CommandConnection connection, String nick, String mesg){
  //check privilege
  if(!bot.checkAccess("exit",nick,connection)){
   connection.sendReply(nick, "EXIT You do not have authorization to kill the bot.");
   return;
  }
  Enumeration cons = bot.getAllConnections();//get all the connections.
  IRCConnection con;
  while(cons.hasMoreElements()){
   con = (IRCConnection)cons.nextElement();
   //disconnect if connected.
   if(con.connected()){
    if(mesg != null)con.disconnect(mesg);
    else con.disconnect();
   }
  }
  try{
   Thread.sleep(1300);//give time for streams to fully flush. 
   }catch(Exception e){}//needed for the finally for some reason
   finally{
    System.exit(0);
   }
  }


}
