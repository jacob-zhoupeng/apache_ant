package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class SMCommandPlug implements CommandPlug{

 Oak bot = null;
 BotSecurityManager securityManager = null;

 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){
  bot = b;
  securityManager = bsm;
 }
 public String[] getCommands(){
  String[] ret = {"SM"};
  return ret;
 }

 public String getGroup(){return "SECURITY";}
 public String getShortHelpDescription(String command){
  return "Set's security permissions for users.";
 } 
 public String[] getLongHelpDescription(String command){
  String ret[] = new String[11];
  ret[0] = "/ctcp bot SM <add|set|filter> <user> level <level>"+
	" [on <server>] [for <channel>]";
  ret[1] = "/ctcp bot SM setPriv <user> <priv> [yes|true|on|no|false|off]"+
  	" [on <serveretwork>] [for <channel>]";
  ret[2] = "/ctcp bot SM adduser <user> <password> <mask>";
  ret[3] = "/ctcp bot SM remuser <user>";
  ret[4] = " <user> the login name of the user.";
  ret[5] = " <level> the level to set to.";
  ret[6] = " <server> the server to set  on.";
  ret[7] = " <channel> the channel to set on.";
  ret[8] = " <priv> the privilege to set.";
  ret[9] = " <password> the password of the user.";
  ret[10] = " <mask> the nick mask of the user.";
  return ret;
 }

 public void doCommand(CommandConnection connection, String fnick,
			String command, String params){
  IRCLine line = new IRCLine(params);
  if(connection instanceof IRCConnection)
   doSMCommand(connection,(IRCConnection)connection,line,fnick);
 }


 private void doSMCommand(CommandConnection connection, IRCConnection targetConnection, IRCLine st, String fullnick){
		String nick = IRCMessage.getNick(fullnick);

		String subCommand = st.getNextToken().toLowerCase();
		if(subCommand.equals("add")){
		//isn't this just a convient way to call setpriv?
		if(!bot.checkAccess("setpriv",null,fullnick,connection, targetConnection)){
                 connection.sendReply(nick, "SM You're not authorized to set privileges for other users on the system");
		 return;
		}
		//add a privlege level to a user
		String user = st.getNextToken();
		st.getNextToken();//"level"
		if(!st.hasMoreTokens()){return;}
		int level = -1;
		try{
		 level = Integer.parseInt(st.getNextToken());
		}catch(NumberFormatException nfe){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		if(level<0 || level > 9){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		String network = st.getNextToken();
		String chan = null;
		if(st.hasMoreTokens()){
       		 st.getNextToken();
		 chan = st.getNextToken();
		}
		try {
		switch(level){
		 case 8 : 
			setPriv("exit",chan,fullnick,connection, network, user,true);
		 case 7 :
			setPriv("connect",chan,fullnick,connection, network, user,true);
			setPriv("disconnect",chan,fullnick,connection, network, user,true);
		 case 6 :
			setPriv("serverplugin",chan,fullnick,connection, network, user,true);
		 case 5 :
			setPriv("channelplugin",chan,fullnick,connection, network, user,true);
			setPriv("pluginlist",chan,fullnick,connection, network, user,true);
		 case 4 : 
			setPriv("op",chan,fullnick,connection, network, user,true);
			setPriv("deop",chan,fullnick,connection, network, user,true);
		 case 3 :
			setPriv("kick",chan,fullnick,connection, network, user,true);
			setPriv("ban",chan,fullnick,connection, network, user,true);
		 case 2 :
			setPriv("voice",chan,fullnick,connection, network, user,true);
			setPriv("devoice",chan,fullnick,connection, network, user,true);
		 case 1 :
			setPriv("join",chan,fullnick,connection, network, user,true);
			setPriv("part",chan,fullnick,connection, network, user,true);
		 case 0 :
			setPriv("say",chan,fullnick,connection, network, user,true);
			setPriv("do",chan,fullnick,connection, network, user,true);
		}
                connection.sendReply(nick, "SM Added "+user+" to level "+level+"");
		}
		catch (BotSecurityException e)
		{
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		return;
		}
		if(subCommand.equals("set")){
		//isn't this just a convient way of calling setpriv?
		if(!bot.checkAccess("setpriv",null,fullnick,connection, targetConnection)){
                 connection.sendReply(nick, "SM You're not authorized to set privileges for other users on the system");
		 return;
		}
		//easiest way to do this is remove all levels then add the ones that
		//are needed.
		String user = st.getNextToken();
		st.getNextToken();//"level"
		if(!st.hasMoreTokens()){return;}
		int level = -1;
		try{
		 level = Integer.parseInt(st.getNextToken());
		}catch(NumberFormatException nfe){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		if(level<0 || level > 9){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		String network = st.getNextToken();
		String chan = null;
		if(st.hasMoreTokens()){
       		 st.getNextToken();
		 chan = st.getNextToken();
		}
		try {
		//removing all standard priv's
		//this is going to fuck up big time if you try executing this on yourself ;o)
		setPriv("say",chan,fullnick,connection, network, user,false);
		setPriv("do",chan,fullnick,connection, network, user,false);
		setPriv("join",chan,fullnick,connection, network, user,false);
		setPriv("part",chan,fullnick,connection, network, user,false);
		setPriv("voice",chan,fullnick,connection, network, user,false);
		setPriv("devoice",chan,fullnick,connection, network, user,false);
		setPriv("kick",chan,fullnick,connection, network, user,false);
		setPriv("ban",chan,fullnick,connection, network, user,false);
		setPriv("op",chan,fullnick,connection, network, user,false);
		setPriv("deop",chan,fullnick,connection, network, user,false);
		setPriv("channelplugin",chan,fullnick,connection, network, user,false);
		setPriv("pluginlist",chan,fullnick,connection, network, user,false);
		setPriv("serverplugin",chan,fullnick,connection, network, user,false);
		setPriv("connect",chan,fullnick,connection, network, user,false);
		setPriv("disconnect",chan,fullnick,connection, network, user,false);
		setPriv("exit",chan,fullnick,connection, network, user,false);
		//securityManager.setPrivilege(user,"YETTOBEDETIRMINED",false,args[0],args[1]);
		//setting the required priv's
		//neat trick with the switch eh?

		switch(level){
		 case 9 : 
			setPriv("exit",chan,fullnick,connection, network, user,true);
		 case 8 :
			setPriv("connect",chan,fullnick,connection, network, user,true);
			setPriv("disconnect",chan,fullnick,connection, network, user,true);
		 case 7 :
			setPriv("serverplugin",chan,fullnick,connection, network, user,true);
		 case 6 :
			setPriv("channelplugin",chan,fullnick,connection, network, user,true);
			setPriv("pluginlist",chan,fullnick,connection, network, user,true);
		 case 5 :
			setPriv("op",chan,fullnick,connection, network, user,true);
			setPriv("deop",chan,fullnick,connection, network, user,true);
		 case 4 : 
			setPriv("kick",chan,fullnick,connection, network, user,true);
			setPriv("ban",chan,fullnick,connection, network, user,true);
		 case 3 : 
			setPriv("voice",chan,fullnick,connection, network, user,true);
			setPriv("devoice",chan,fullnick,connection, network, user,true);
		 case 2 :
			setPriv("join",chan,fullnick,connection, network, user,true);
			setPriv("part",chan,fullnick,connection, network, user,true);
		 case 1 : 
			setPriv("say",chan,fullnick,connection, network, user,true);
			setPriv("do",chan,fullnick,connection, network, user,true);
		}
                connection.sendReply(nick, "SM SET "+user+" set to level "+level+"");
		}
		catch (BotSecurityException e){
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		return;
		}
		if(subCommand.equals("filter")){
		// isn't this really just a convient way to call setpriv?
		if(!bot.checkAccess("setpriv",null,fullnick,connection, targetConnection)){
                 connection.sendReply(nick, "SM You're not authorized to set privileges for other users on the system");
		 return;
		}
		//remove priv's of a specific level.
		String user = st.getNextToken();
		st.getNextToken();//"level"
		if(!st.hasMoreTokens()){return;}
		int level = -1;
		try{
		 level = Integer.parseInt(st.getNextToken());
		}catch(NumberFormatException nfe){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		if(level<0 || level > 9){
                 connection.sendReply(nick, "SM Unknown level");
		 return;
		}
		String network = st.getNextToken();
		String chan = null;
		if(st.hasMoreTokens()){
       		 st.getNextToken();
		 chan = st.getNextToken();
		}
		//filter 9 won't do anything.
		//here's a nice trick since switch automatically "falls through" once it
		//finds a match. ;o)
		//this looks nasty 'cause of all the checkAccess calls
		//using the ? operator keeps it neater than a bunch of if/thens though
		try {
		switch(level){
		 case 0 : 
			setPriv("say",chan,fullnick,connection, network, user,false);
			setPriv("do",chan,fullnick,connection, network, user,false);
		 case 1 :
			setPriv("join",chan,fullnick,connection, network, user,false);
			setPriv("part",chan,fullnick,connection, network, user,false);
		 case 2 :
			setPriv("voice",chan,fullnick,connection, network, user,false);
			setPriv("devoice",chan,fullnick,connection, network, user,false);
		 case 3 :
			setPriv("kick",chan,fullnick,connection, network, user,false);
			setPriv("ban",chan,fullnick,connection, network, user,false);
		 case 4 :
			setPriv("op",chan,fullnick,connection, network, user,false);
			setPriv("deop",chan,fullnick,connection, network, user,false);
		 case 5 :
			setPriv("channelplugin",chan,fullnick,connection, network, user,false);
			setPriv("pluginlist",chan,fullnick,connection, network, user,false);
		 case 6 :
			setPriv("serverplugin",chan,fullnick,connection, network, user,false);
		 case 7 : 
			setPriv("connect",chan,fullnick,connection, network, user,false);
			setPriv("disconnect",chan,fullnick,connection, network, user,false);
		 case 8 : 
			setPriv("exit",chan,fullnick,connection, network, user,false);
		}
                connection.sendReply(nick, "SM FILTER "+user+" filtered to level "+level+"");
		}
		catch (BotSecurityException e)
		{
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		return;
		}
		if(subCommand.equals("setpriv")){
		if(!bot.checkAccess("setpriv",fullnick,connection)){
                 connection.sendReply(nick, "SM You're not authorized to set privileges for other users on the system");
		 return;
		}
		String user = st.getNextToken();
		String priv = st.getNextToken();
		String network = st.getNextToken();
		String sets = st.getNextToken().toLowerCase();
		String chan = null;
		if(st.hasMoreTokens()){
		 st.getNextToken();
		 chan = st.getNextToken();
		}
		boolean setb = false;
		if(sets.equals("yes") || sets.equals("true") || sets.equals("on")){
		 setb = true;
		}else{
		 if(sets.equals("no") || sets.equals("false") || sets.equals("off")){
		  setb = false;
		 }else return;
		}
		sets = null;
		try{
 		 setPriv(priv,chan,fullnick,connection, network, user,setb);
		}
		catch (BotSecurityException e)
		{
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		priv = null;
		return;
		}
		if(subCommand.equals("adduser")){
		if(!bot.checkAccess("adduser",fullnick,connection)){
                 connection.sendReply(nick, "SM You're not authorized to add users from the system");
		 return;
		}
		String user = st.getNextToken();
		String pwrd = st.getNextToken();
		String mask = st.getNextToken();
		try
		{
    		securityManager.addUser(user,pwrd,mask);
                connection.sendReply(nick, "SM ADDUSER "+user+" has been added to the system.");
		}
		catch (BotSecurityException e)
		{
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		return;
		}
		if(subCommand.equals("deluser")){
		//removes a user from the system.
		if(!bot.checkAccess("deluser",fullnick,connection)){
                 connection.sendReply(nick, "SM You're not authorized to remove users from the system");
		 return;
		}
		String user = st.getNextToken();
		try
		{
    		securityManager.removeUser(user);
                connection.sendReply(nick, "SM DELUSER "+user+" has been removed from the system.");
		}
		catch (BotSecurityException e)
		{
			//sendNotice(connection, nick, e.toString());
			e.printStackTrace();
		}
		return;
		}
	}


	
 private static String[] getArgs(IRCLine st){
  String[] s = new String[4];
  String str;
  //0 on
  //1 for
  //2 with
  //3 level
  s[0] = null;
  s[1] = null;
  s[2] = "";
  s[3] = null;
  boolean with = false;
  while(st.hasMoreTokens()){
   if(with == true){
    s[2] = s[2] +" "+st.getNextToken();
    continue;
   }
   str = st.getNextToken();
   if(str.equalsIgnoreCase("on")){
    s[0] = st.getNextToken();
    s[2] = null;
    continue;
   }
   if(str.equalsIgnoreCase("for")){
    s[1] = st.getNextToken();
    s[2] = null;
    continue;
   }
   if(str.equalsIgnoreCase("level")){
    s[3] = st.getNextToken();
    s[2] = null;
    continue;
   }
   if(str.equalsIgnoreCase("with")){
    s[2] = st.getNextToken();
    with = true;
    continue;
   }
   s[2] = s[2] +" "+str;
  }
  return s;
 }

public void setPriv(String priv,String channel, String fullnick,
	CommandConnection connection, String network, String user,
	boolean setting)throws BotSecurityException{
  if(bot.checkAccess(priv,channel,fullnick,connection, network))
   securityManager.setPrivilege(user,priv,setting,network,channel);
  else
   connection.sendReply(fullnick,"SM unable to set the "+priv+" privliege");
 }

}
