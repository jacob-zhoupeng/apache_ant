package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
public class ModerCommandPlug implements CommandPlug{

 Oak bot = null;
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc){bot = b;}
 public String[] getCommands(){
  String ret[] = {"DEOP","OP","DEVOICE","VOICE","BAN","UNBAN"};
  return ret;
 }
  public String getGroup(){return "BOTCONTROL";}
 public String getShortHelpDescription(String command){
   if(command.equals("OP"))return "Gives a nick op's in a channel.";
   if(command.equals("DEOP"))return "Takes op's away from a nick in a channel.";
   if(command.equals("DEVOICE"))return "Takes voice away from a nick in a channel.";
   if(command.equals("VOICE")) return "Gives voice to a nick in a channel.";
   if(command.equals("BAN")) return "Bans a mask in a channel.";
   if(command.equals("UNBAN")) return "Removes a ban in a channel.";
   return null;
  }
 public String[] getLongHelpDescription(String command){
  String ret[] = null;
  if(command.equals("OP")|| command.equals("VOICE")|| command.equals("DEVOICE")||command.equals("DEOP")){
   ret = new String[3];
   ret[0] = "/ctcp BOT "+command+" <channel> <nick>";
   ret[1] = "<channel> The channel to "+command.toLowerCase()+" the nick in.";
   ret[2] = "<nick>    The nick to "+command.toLowerCase();
  }
  if(command.equals("BAN") || command.equals("UNBAN")){
   ret = new String[3];
   ret[0] = "/ctcp BOT "+command+" <channel> <mask>";
   ret[1] = "<channel> the channel to "+command.toLowerCase()+" the mask in.";
   ret[2] = "<mask> the mask of the person to "+command.toLowerCase();
  }
  return ret;
 }

 public void doCommand(CommandConnection connection, String fnick,
			String command, String params){
  IRCLine line = new IRCLine(params);
  String channel = line.getNextToken();
  IRCConnection con = null;
   if(connection instanceof IRCConnection)
    con = (IRCConnection)connection; 
  if(channel.equalsIgnoreCase("on")){
   con = bot.getIRCConnection(line.getNextToken());
   channel = line.getNextToken();
  }
  if(con == null)return;//unknown connection
  String nick = line.getNextToken();
  if(command.equals("DEOP")){
   doDeOp(connection,con,fnick,channel,nick);
   return;
  }
  if(command.equals("OP")){
   doOp(connection,con,fnick,channel,nick);
   return;
  }
  if(command.equals("VOICE")){
   doVoice(connection,con,fnick,channel,nick);
   return;
  }
  if(command.equals("DEVOICE")){
   doDeVoice(connection,con,fnick,channel,nick);
   return;
  }
  if(command.equals("BAN")){
   doBan(connection,con,fnick,channel,nick);
   return;
  }
  if(command.equals("UNBAN")){
   doUnban(connection,con,fnick,channel,nick);
   return;
  }
 }

 private void doOp(CommandConnection connection, IRCConnection tcon, String fnick, String channel, String nick){
  if(!bot.checkAccess("op",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "OP You're not authorized to op people on that server, network, or channel.");
   return;
  }
  tcon.op(channel,nick);
 }

 private void doDeOp(CommandConnection connection, IRCConnection tcon, String fnick, String channel, String nick){
  if(!bot.checkAccess("deop",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "DEOP You're not authorized to deop people on that server, network, or channel.");
   return;
  }
  tcon.deOp(channel,nick);
 }

 private void doVoice(CommandConnection connection, IRCConnection tcon, String fnick, String channel, String nick){
  if(!bot.checkAccess("voice",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "VOICE You're not authorized to voice people on that server, network, or channel.");
   return;
  }
  tcon.voice(channel,nick);
 }

 private void doDeVoice(CommandConnection connection, IRCConnection tcon, String  fnick, String channel, String nick){                
  if(!bot.checkAccess("devoice",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "DEVOICE You're not authorized to devoice people on that server, network, or channel.");
   return;
  }
  tcon.deVoice(channel,nick);
 }

 private void doBan(CommandConnection connection, IRCConnection tcon, String fnick, String channel, String nick){                
  if(!bot.checkAccess("ban",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "BAN You're not authorized to ban people on that server, network, or channel.");
   return;
  }
  tcon.ban(channel,nick);
 }

 private void doUnban(CommandConnection connection, IRCConnection tcon, String fnick, String channel, String nick){             
  if(!bot.checkAccess("unban",channel,fnick,connection,tcon)){
   connection.sendReply(fnick, "UNBAN You're not authorized to unban people on that server, network, or channel.");
   return;
  }
  tcon.unBan(channel,nick);
 }

}

