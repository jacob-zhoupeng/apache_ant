package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.plugin.*;
import org.moxy.oak.security.*;
import java.util.Enumeration;
public class PluginCommandPlug implements CommandPlug{

 Oak bot = null;

 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
 }

 public String[] getCommands(){
  String ret[] =
     {"LOAD","UNLOAD","PLUGINLIST","LOADCP","UNLOADCP","LOADCMD","UNLOADCMD"};
return ret;
 }

 public String getGroup(){return "PLUGIN";}
 public String getShortHelpDescription(String command){
  if(command.equals("LOAD")){return "Loads a server plugin.";}
  if(command.equals("UNLOAD")){return "Unloads a server plugin.";}
  if(command.equals("PLUGINLIST")){return "not implemented yet.";}
  if(command.equals("LOADCP")){return "Loads a ChanelPlugin.";}
  if(command.equals("UNLOADCP")){return "Unloads a ChannelPlugin.";}
  if(command.equals("LOADCMD"))return "Loads a commandplug.";
  if(command.equals("UNLOADCMD"))return "Unloads a commandplug.";
  return null;
 }

 public String[] getLongHelpDescription(String command){
  String ret[] = new String[1];
  ret[0] = "I'm to lazy to cdocument this right now.";
  return ret;
 }

 public void doCommand(CommandConnection connection, String nick,
			String command, String param){
  IRCLine line = new IRCLine(param);
  if(command.equals("PLUGINLIST")){
	doPluginlist(connection,nick, param);
	return;
  }//not implemented
  if(command.equals("LOADCP")){
   doNLoad(line,connection,nick);
   return;
  }
  if(command.equals("UNLOADCP")){
   doNunload(line,connection,nick);
   return;
  }
  IRCConnection con = null;
  if(connection instanceof IRCConnection)
   con = (IRCConnection) connection;
  String param1 = line.getNextToken();
  if(param1.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   param1 = line.getNextToken();
  }
System.out.println(command);
System.out.println(con);
  if(con == null)return;//unknown connection
  if(command.equals("LOAD")){
   String[] params = new String[line.countTokens()];
   for(int x = 0; x<params.length;x++)
    params[x] = line.getNextToken();
   doLoad(connection,con,nick,param1,params);
  }
  if(command.equals("UNLOAD")){
   int ref = -1;//bogus references
   try{
    ref = Integer.parseInt(param1);
   }catch(NumberFormatException nfe){nfe.printStackTrace();}
   doUnload(connection,con,nick,ref);
   return;
  }
  if(command.equals("LOADCMD")){
   doLoadCMD(connection,con,nick,param1);
   return;
  }
  if(command.equals("UNLOADCMD")){
System.out.println("unload cmd being handled.");
   doUnLoadCMD(connection,con,nick,param1);
   return;
  }
 }

 private void doUnLoadCMD(CommandConnection connection, IRCConnection tcon, String fnick, String cmd){
  if(!bot.checkAccess("serverplugin",null,fnick,connection,tcon)){
   connection.sendReply(fnick,"UNLOADCMD You're not authorized to load plugins on that server,networ, or channel.");
   return;
  }
  bot.unloadCommandPlug(cmd);
  connection.sendReply(fnick,"UNLOADCMD "+cmd+" unloaded.");
 }

 private void doLoadCMD(CommandConnection connection, IRCConnection tcon, String fnick, String classname){
  if(!bot.checkAccess("serverplugin",null,fnick,connection,tcon)){
   connection.sendReply(fnick,"LOADCMD You're not authorized to load plugins on that server, network, or channel.");
   return;
  }
  try{
   Object plugObj = Class.forName(classname).newInstance();
   if(!(plugObj instanceof CommandPlug)){
    connection.sendReply(fnick,"LOADCMD Not a valid command plugin.");
    return;
   }
   bot.loadCommandPlug((CommandPlug)plugObj);
  }catch(ClassNotFoundException cnfe){cnfe.printStackTrace();}
   catch(InstantiationException ie){ie.printStackTrace();}
   catch(IllegalAccessException iae){iae.printStackTrace();}
 }

 private void doLoad(CommandConnection connection, IRCConnection tcon, String fnick, String classname, String[] params){
  if(!bot.checkAccess("serverplugin",null,fnick,connection,tcon)){
   connection.sendReply(fnick, "LOAD You're not authorized to load plugins on that server, network, or channel.");
   return;
  }
  try{
   Object plugObj = Class.forName(classname).newInstance();
   if(!(plugObj instanceof OakPlugin)){
    connection.sendReply(fnick, "LOAD Not a valid plugin.");
    return;
   }
   bot.loadPlugin((OakPlugin)plugObj, params, tcon);
  }catch(ClassNotFoundException cnfe){cnfe.printStackTrace();}
   catch(InstantiationException ie){ie.printStackTrace();}
   catch(IllegalAccessException iae){iae.printStackTrace();}
 }

 private void doUnload(CommandConnection connection, IRCConnection tcon, String fnick, int ref){
  if(!bot.checkAccess("serverplugin",null,fnick,connection,tcon)){
   connection.sendReply(fnick, "UNLOAD You're not authorized to load plugins on that server, network, or channel.");
   return;
  }
   bot.unloadPlugin(ref);
 }

 private void doNLoad(IRCLine message, CommandConnection connection, String fnick){
  //command plugs will be rewritten this is a stop 
  // gap measure to ensure ChannelPlugin works properly
System.out.println("nload starting");
  IRCConnection con = null;
  if(connection instanceof IRCConnection)con=(IRCConnection)connection;
  try{
   String cname = message.getNextToken();
   if(cname.equalsIgnoreCase("on")){
    con = bot.getIRCConnection(message.getNextToken());
    cname = message.getNextToken();
   }
   if(con == null){return;}
  if(!bot.checkAccess("channelplugin",null,fnick,connection, con)){
   connection.sendReply(fnick, "LOADCP You're not authorized to load plugins on that server, network, or channel.");
   return;
  }
System.out.println("cname="+cname);
   Class clazz = Class.forName(cname);
   Object o = clazz.newInstance();
   if(!(o instanceof ChannelPlugin)){
System.out.println("not a channel plugin");
    return;
   }
   ChannelPlugin plug = (ChannelPlugin)o;
   String identifier = message.getNextToken();
   String room = message.getNextToken();
   String[] params = new String[message.countTokens()];
   int x = 0;
   while(message.hasMoreTokens())
    params[x++] = message.getNextToken();
   bot.loadChannelPlugin(plug,null, params,room,con,identifier);
  }catch(Exception e){
   e.printStackTrace();
  }
 }
 public void doNunload(IRCLine message, CommandConnection connection, String fnick){
if(Preferences.DEBUG_LEVEL>=1)
 System.out.println("starting nunload");
  IRCConnection con = null;
  if(connection instanceof IRCConnection)con = (IRCConnection)con;
  String identifier = message.getNextToken();
  if(identifier.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(message.getNextToken());
   identifier = message.getNextToken();
  }
  if(con == null){return;}//dunno the connection
  if(!bot.checkAccess("channelplugin",null,fnick,connection, con)){
   connection.sendReply(fnick, "LOADCP You're not authorized to load plugins on that server, network, or channel.");
   return;
  }  
  bot.unloadChannelPlugin(identifier);  
 }

 public void doPluginlist(CommandConnection connection, String nick, String type){
  type = type.toUpperCase();
  if(type.equals("CHANNEL")){
  Enumeration list = bot.getChannelPlugins();
  while(list.hasMoreElements()){
   ChannelPlugin plug = (ChannelPlugin)list.nextElement();
   connection.sendReply(nick,"PLUGINLIST "+plug.getIdentifier()+" "+plug.getServer().getServer()+" "+plug.getClass().getName());
  }
  connection.sendReply(nick,"PLUGINLIST done.");
  return;
  }else
   if(type.equals("SERVER")){
    Enumeration list = bot.getPlugins();
    while(list.hasMoreElements()){
     OakPlugin plug = (OakPlugin)list.nextElement();
     connection.sendReply(nick,"PLUGINSLIT "+plug.getIdentifier()+" "+plug.getConnection().getServer()+" "+plug.getClass().getName());
    }
    connection.sendReply(nick,"PLUGINLIST done.");
    return;
   }
  connection.sendReply(nick,"PLUGINLIST /ctcp []A]{ PLUGINLIST [CHANNEL|SERVER]");
 }

}
