/**
 * Handles all the defualt commands passed to Oak via IRC.<br>
 * TODO:
 *  There needs to be a way for Oak to accept commands
 *  via CTCP,private message, or both. This should be
 *  implemented via a method call setting in oak's
 *  configuration.
 * @version 1.0
 * @author Marcus Wenzel
 */

package org.moxy.oak;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.commandplug.*;
import org.moxy.oak.security.*;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.sql.*;

public class OakCommand extends DefaultIRCListener{

 private Oak bot;
 private BotSecurityManager securityManager = null;

 public OakCommand(){} 

 /**
  * Called by Oak to initilize the bot.
  * @since 1.0
  * @param b the bot.
  */
 public void init(Oak b, BotSecurityManager bsm){
  bot = b;
  securityManager = bsm;
  loadCommandPlugin(new TopicCommandPlug());
  loadCommandPlugin(new HelpCommandPlug());
  loadCommandPlugin(new KickCommandPlug());
  loadCommandPlugin(new ServerListCommandPlug());
  loadCommandPlugin(new ConnectionPlug());
  loadCommandPlugin(new DoSayCommandPlug());
  loadCommandPlugin(new DBCommandPlug());
  loadCommandPlugin(new ModerCommandPlug());
  loadCommandPlugin(new NickCommandPlug());
  loadCommandPlugin(new SMCommandPlug());
  loadCommandPlugin(new JoinPartCommandPlug());
  loadCommandPlugin(new ExitCommandPlug());
  loadCommandPlugin(new PluginCommandPlug());
 }

 /*
  * COMMAND PLUG STUFF STARTING
  */
 private Hashtable plugCommands = new Hashtable();

 private Vector plugs = new Vector();

 public void loadCommandPlugin(CommandPlug plug){
  plug.init(bot,securityManager, this);
  String[] cmds = plug.getCommands();
  for(int x = 0; x<cmds.length; x++)
   plugCommands.put(cmds[x].toUpperCase(),plug);
  plugs.addElement(plug);
 }

 public void unloadCommandPlugin(String command){
  plugCommands.remove(command.toUpperCase());
 }
 
 public Enumeration getCommandPlugs(){
  return plugs.elements();
 }

 public void doCommand(CommandConnection connection, String remoteNick, 
 			String command, String params){
  if(!bot.isLoggedIn(connection, remoteNick))return;
  CommandPlug cp = (CommandPlug)plugCommands.get(command.toUpperCase());
  if(cp == null)return;//command not found.
  cp.doCommand(connection,remoteNick,command,params);
 }

 /*
  * COMMAND PLUG STUFF ENDING
  */
 
	/**
	 * Handles the incomming ctcp commands.
	 * <br>Commands to be listed later.
	 * @since 1.0
	 */
	public void handleCTCPMSG(IRCConnection connection, String sender, String target, String msg){
 	 IRCLine line = new IRCLine(msg);
	 String command = line.getNextToken();
	 String param = line.getRemaining();
   	 doCommand((IRCCommandConnection)connection,sender,command,param);
if(Preferences.DEBUG_LEVEL>=20)
 System.out.println("{OakCommand.handleCTCPMSG} command="+command);

//                doCommand((IRCCommandConnection)connection,message);
	}


}


