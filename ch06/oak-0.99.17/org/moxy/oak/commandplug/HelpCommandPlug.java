package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
import java.util.*;
public class HelpCommandPlug implements CommandPlug{

 OakCommand oc; 

 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  this.oc = oc;
 }

 public String[] getCommands(){
  String ret[] = {"HELP"};
  return ret;
 }

 public void doCommand(CommandConnection connection, String fromLongNick, 
			String command, String params){
  if(params == null || params.equals("")){//got /ctcp bot HELP
   Hashtable table = new Hashtable();
   Enumeration enum = oc.getCommandPlugs();
   CommandPlug plug = null;
   while(enum.hasMoreElements()){//gathers all the gropus and commands
     plug = (CommandPlug)enum.nextElement();//for those groups
     String group = plug.getGroup();
     if(group == null || group.equals(""))continue;
     String s = (String)table.get(group.toUpperCase());
     if(s==null) s = "";
     String commands[] = plug.getCommands();
     for(int x = 0; x<commands.length; x++)
      s=s+" "+commands[x].toUpperCase();
    table.put(plug.getGroup().toUpperCase(),s);
    s = null; group = null;
   }
   enum = table.keys();
   String key = null;
   while(enum.hasMoreElements()){
    key = (String)enum.nextElement();
    //print out the group name.
    connection.sendReply(fromLongNick,"HELP Group: "+key);
    //print out the commands for the group
    connection.sendReply(fromLongNick,"HELP      "+(String)table.get(key));
   }
   return;
  }
  params = params.toUpperCase();
  if(params.startsWith("LIST") && params.indexOf(" ")>-1){
   //got a /ctcp bot list group 
   Enumeration enum = oc.getCommandPlugs();
   CommandPlug plug = null;
   String key = params.toUpperCase().substring(5).trim();
   //send group name.
   connection.sendReply(fromLongNick,"HELP "+key);
   while(enum.hasMoreElements()){
     plug = (CommandPlug)enum.nextElement();
     String group = plug.getGroup();
     if(group == null || group.equals(""))continue;
     if(group.toUpperCase().equals(key)){//find commandplugs of this group
      String[] commands = plug.getCommands();
      for(int x = 0; x<commands.length; x++)//print out all the short desc's
       connection.sendReply(fromLongNick,"HELP   "+commands[x]+" "+plug.getShortHelpDescription(commands[x]));
     }
   }
   return;
  }
  //got a /ctcp bot command
  params = params.toUpperCase();
  Enumeration enum = oc.getCommandPlugs();
  CommandPlug plug;
  while(enum.hasMoreElements()){
   plug = (CommandPlug)enum.nextElement();//loop through the plugs 'till the
   String[] commands = plug.getCommands();//command is found.
   for(int x = 0; x<commands.length; x++)
    if(commands[x].toUpperCase().equals(params)){
     String[] lhelp = plug.getLongHelpDescription(params);//get the help
     for(int x1 = 0; x1<lhelp.length; x1++)//print the help strings out.
      connection.sendReply(fromLongNick,"HELP "+lhelp[x1]);
     return;
    }
  }
  connection.sendReply(fromLongNick,"Couldn't find that command.");
 }
 public String getGroup(){return "HELP";}
 public String getShortHelpDescription(String command){
  return "Retrieves help for a command";}
 public String[] getLongHelpDescription(String command){
  String[] ret = new String[3];
  ret[0] = "/ctcp bot help [<command> | list <group>]";
  ret[1] = "  command the name of the command to get help on.";
  ret[2] = "  group the group to list the commands in.";
  return ret;
 }
}
