package org.moxy.oak.plugin;
/*
  This class will be configured via a specialized dcc connection.

  Each plugin will be given a number wich repersents 1+ it's array
  index.

  should be ported to an OakPlugin
 */
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
import org.moxy.util.*;
import java.net.*;
import java.io.*;
public class DCCChatManager extends DefaultOakPlugin{

 Oak bot;
 DCCChatConfigurator configurator;
 DCCChatPlugin[] plugs = new DCCChatPlugin[0];
 private ThreadInteruptingVector connectingSockets = new ThreadInteruptingVector(240000);//~4 minutes


 public void initFullServerPlugin(Oak b, String[] params){
if(Preferences.DEBUG_LEVEL>=20){
 System.out.println("Starting DCCChatManager.");
}
  this.bot = b;
  configurator = new DCCChatConfigurator();
  configurator.init(bot,this);
 }


 public void handleCTCPMSG(IRCConnection connection, IRCMessage message){
if(Preferences.DEBUG_LEVEL>=20){
 System.out.println("{DCCChatManager.handleCTCPMSG} starting.");
}
 IRCLine line;
 line = message.getIRCLine();
if(Preferences.DEBUG_LEVEL>=20){
 String tok = line.getNextToken();
 System.out.println("{DCCChatManager.handleCTCPMSG} "+tok);
 line.putBack(tok);
}
  if(!line.getNextToken().equalsIgnoreCase("DCC"))return;//not dcc message
  if(line.getNextToken().equalsIgnoreCase("CHAT")){
   line.getNextToken();//dcc chat
   String address = "";
   try{
    long addy = (new Long(line.getNextToken())).longValue();
    address = ""+(addy & 0xff);
    address =  ((addy & 0xff00) >> 8)+"."+address;
    address =  ((addy & 0xff0000) >> 16)+"."+address;
    address =  ((addy & 0xff000000) >> 24)+"."+address;
    int port = Integer.parseInt(line.getNextToken());
if(Preferences.DEBUG_LEVEL >=20){
 System.out.println("Tring to connect to "+address+":"+port);
}
    new SocketOpener(address,port,message.getSender(),connection);
   }catch(NumberFormatException nfe){nfe.printStackTrace();return;}
    catch(Exception e){e.printStackTrace();}
  }
 }

 class SocketOpener extends Thread{
  String address;
  int port;
  String sender;
  IRCConnection connection;
  public SocketOpener(String address, int port, String sender, IRCConnection connection){
   this.connection = connection;
   this.address = address;
   this.port = port;
   this.sender = sender;
   this.start();
   connectingSockets.add(this);
  }
  public void run(){
   try{
    Socket s = new Socket(address,port);
    connectingSockets.remove(this);
    newConnection(s,sender,connection);
   }catch(Exception e){
    e.printStackTrace();
    System.err.println("An error occured on "+address+":"+port);
    System.err.println("Possibly timed out.");
   }
  }
 }
 
 public void newConnection(Socket s, String fullNick, IRCConnection connection){
  boolean closeSocket = false;
  try{
   BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
   DataOutputStream output = new DataOutputStream(s.getOutputStream());
   if(plugs.length > 0)
    output.writeBytes("DCC Menu:\n");
   for(int x = 0; x<plugs.length; x++)
    output.writeBytes((x+1)+") "+plugs[x].getDescription()+"\n");   
   String reply;
   if(plugs.length > 0){
    output.writeBytes("Type exit to quit.\n");
    output.writeBytes("Please enter your selection:\n");
   }
   while(true){
    reply = input.readLine();
    try{
     if(reply.equalsIgnoreCase("configure")){
      configurator.newConnection(fullNick,connection,s);
      return;     
     }
     if(reply.equalsIgnoreCase("exit")){
      closeSocket = true;
      break;
     }
     int number = (Integer.parseInt(reply))-1;
     if(number < 0 || number >= plugs.length){
      output.writeBytes("Invalid option");
      continue;
     }
     plugs[number].newConnection(fullNick,connection,s);
     break;
    }catch(NumberFormatException nfe){
     output.writeBytes("Invalid option.");
    }
   }
  }catch(IOException ioe){ioe.printStackTrace();}
  //if we reach here then we should try to close the socket.
  try{
   if(closeSocket)
    s.close();
  }catch(Exception e){}//ignored
 }
  
 public void loadPlugin(DCCChatPlugin p){
  DCCChatPlugin[] newPlugs = new DCCChatPlugin[plugs.length+1];
  for(int x = 0; x<plugs.length; x++)
   newPlugs[x] = plugs[x];
  newPlugs[plugs.length] = p;
  newPlugs[plugs.length].init(bot,this);
  plugs = newPlugs;
 }

 public void removePlugin(int i){
  if(i < 0 || i >= plugs.length)return;
  DCCChatPlugin[] newPlugs = new DCCChatPlugin[plugs.length-1];
  for(int x = 0; x<i; x++)
   newPlugs[x] = plugs[x];
  for(int x = i+1; x<plugs.length; x++)
   newPlugs[x-1] = plugs[x];
  plugs = newPlugs;
 }

 public DCCChatPlugin[] getPlugins(){
  return plugs;
 }


}
