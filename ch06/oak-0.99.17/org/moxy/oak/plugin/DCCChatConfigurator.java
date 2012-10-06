package org.moxy.oak.plugin;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
import java.net.*;
import java.io.*;
public class DCCChatConfigurator implements DCCChatPlugin{

 public DCCChatManager manager;
 public Oak bot;

 public String getDescription(){return "DCCChatConfigurator";}

 public void init(Oak bot, DCCChatManager manager){
  this.bot = bot;
  this.manager = manager;
 }

 public void newConnection(String fullNick, IRCConnection origin, Socket s){
  try{
   DataOutputStream output = new DataOutputStream(s.getOutputStream());
   if(!bot.isLoggedIn((IRCCommandConnection)origin, fullNick)){
    output.writeBytes("You must first log into the bot before you may "+
                      "access this option.\n");
    return;
   }
   if(!bot.checkAccess("dcc.configure",fullNick,(IRCCommandConnection)origin)){
    output.writeBytes("You do not have access to this portion of the bot.\n");    
    return;
   }
   (new DCCChatConfiguratorConnectionHandeler(fullNick,origin,s)).start();
  }catch(IOException ioe){
   try{
    s.close();
   }catch(Exception e2){
    e2.printStackTrace();
   }   
   return;
  }
 }

 class DCCChatConfiguratorConnectionHandeler extends Thread{
  String nick;
  IRCConnection origin;
  Socket s;
  BufferedReader input;
  DataOutputStream output;
  public DCCChatConfiguratorConnectionHandeler(
   String fullNick, IRCConnection origin, Socket s){
    this.nick = fullNick;
    this.origin = origin;
    this.s = s;
    try{
     this.input = new BufferedReader(new InputStreamReader(s.getInputStream()));
     this.output = new DataOutputStream(s.getOutputStream());
    }catch(IOException ioe){
     ioe.printStackTrace();
     input = null; output = null;     
    }
  }
  public void run(){
   try{
    boolean cont = true;
    while(cont){
      output.writeBytes("Configuration of the DCC Menu\n");
      output.writeBytes("1) Load Plugin.\n");
      output.writeBytes("2) Unload Plugin.\n");
      output.writeBytes("3) Return to main menu.\n");
      try{
       switch(Integer.parseInt(input.readLine())){
        case 1: loadPlug(input,output);break;
        case 2: unloadPlug(input,output);break;
        case 3: cont = false; break;
       }       
      }catch(NumberFormatException nfe){
       output.writeBytes("Bad Number.\n");
       continue;
      }
    }
    manager.newConnection(s,nick,origin);
   }catch(Exception e){
     e.printStackTrace();
     try{
      s.close();
     }catch(Exception e1){e1.printStackTrace();}
    }
  }
 }

 public void loadPlug(BufferedReader input, DataOutputStream output) throws IOException{
  output.writeBytes("Enter the fully qualified name of the plugin to load.\n");
  String className = input.readLine();
  Class clazz;
  try{
   clazz = Class.forName(className);
  }catch(ClassNotFoundException cnfe){
   output.writeBytes("Couldn't find that class.\n");
   return;
  }
  Object plugin;
  try{
   plugin = clazz.newInstance();
   if(!(plugin instanceof DCCChatPlugin)){
    output.writeBytes("Not a valid DCC plugin.\n");
    return;
   }
   manager.loadPlugin((DCCChatPlugin)plugin);
  }catch(Exception ie){
   output.writeBytes("Error occured while creating the plugin.\n");
   ie.printStackTrace();
   return;
  }
 }

 public void unloadPlug(BufferedReader input, DataOutputStream output) throws IOException{
  DCCChatPlugin[] plugs = manager.getPlugins();
  output.writeBytes("Enter the number of the plugin to remove.\n");
  output.writeBytes("Enter quit to leave with out removing a plugin.\n");
  for(int x = 0; x<plugs.length; x++)
   output.writeBytes((x+1)+") "+plugs[x].getDescription()+"\n");
  try{
   String si = input.readLine();
   if(si.equalsIgnoreCase("quit")){return;}
   int i = Integer.parseInt(si);
   i = i-1;
   if(i>=plugs.length){
    output.writeBytes("Bad Number.\n");
    return;
   }
   manager.removePlugin(i);
  }catch(NumberFormatException nfe){
    output.writeBytes("Bad Number.\n");
    return;
   }
  return;  
 }

}
