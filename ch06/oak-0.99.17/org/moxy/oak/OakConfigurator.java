package org.moxy.oak;
import org.moxy.util.*;
import java.util.*;
import java.io.*;
public class OakConfigurator{
 private static String[] mainMenu = 
	{"Configuration Menu:",
	"i). Identd Server (Status: ",
	"s). Servers",
	"f). Full Name (Status: ",
	"n). Nick (Status: ",
	"S). Security Manager (Status: ",
	"l). Log file options",
	"w). Write to disk.",
	"r). Redisplay Menu.",
	"x). Exit"};

 private static String[] identMenu =
	{"IdentD Menu (Status: ",
	"Can't confgure option.",
	"p). Port Number (Status: ",
	"u). User (Status: ",
	"s). System type (Status: ",
	"r). Redisplay Menu.",
	"b). Back to main menu."};

 private static String[] serverMenu = 
	{"Server Menu (Status: ",
	" Current server: ",
	"a). Add a server.",
	"d). Delete a server.",
	"c). Configure channels for the current server.",
	"e). Edit the current server.",
	"s). Switch the current server.",
	"l). List known servers. (List may be to large for small screens)",
	"r). Redisplay Menu.",
	"b). Back to main menu."};
 static int currentServer = 0;
 static String currentServerName = "undefined";
 private static String[] configureServer =//has to be filled out on use.
	{"Configure Server (Status: ", //current server name.
	"h). Host (Status: ", //host name
	"p). Port (Status: ", //port number
	"a). Add plugin.",
	"d). Delete plugin.",
	"l). List plugins (List may be to large for small screens)",
	"r). Redisplay Menu.",
	"b). Back to server menu."};
 private static String[] logMenu = 
	{"Log Options",
        "",
	"d). Directory ",
	"s). Maximum log size",
	"r). Redisplay Menu",
	"b). Back to main menu."};
 private static String[] channelMenu = //has to be filled out on use.
	{"Channel Menu (Status: ",
	" Current channel: ",
	"a). Add a channel.",
	"d). Delete a channel.",
	"P). Add a plugin.",
	"D). Delete a plugin.",
	"L). List plugins. (List may be to large for small screens)",
	"s). Switch current channel.",
	"l). List known channels. (List may be to large for small screens)",
	"r). Redisplay Menu.",
	"b). Back to server menu."};
 static int currentChannel = 0;
 static String currentChannelName = "undefined";
 static boolean changed = false;
 static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 static ListableProperties props = new ListableProperties();

 public static void main (String arg[]) throws Exception {
  File f = new File(getFile());
  if(f.exists())
   props.load(new FileInputStream(f));
  mainMenu[1] +=(props.getProperty("oak.identd","false").equalsIgnoreCase("true") ? "enabled)" : "disabled");
  mainMenu[3] +=props.getProperty("oak.fullname")+")";
  mainMenu[4] +=props.getProperty("oak.nick")+")";
  mainMenu[5] +=props.getProperty("oak.securitymanager")+")";
  identMenu[0] +=(props.getProperty("oak.identd","false").equalsIgnoreCase("true") ? "enabled" : "disabled")+"): ";
  identMenu[1] =props.getProperty("oak.identd","false").equalsIgnoreCase("true") ? "d). Disable server" : "e). Enable server";
  identMenu[2] +=props.getProperty("oak.identd.port","113")+")";
  identMenu[3] +=props.getProperty("oak.identd.user")+")";
  identMenu[4] +=props.getProperty("oak.identd.system")+")";
  String trash[] = props.getList("oak.servers",new String[0]);
  if(trash != null && trash[currentServer] != null){
   currentServerName = trash[currentServer];
   serverMenu[0] +=trash.length+" servers.";
   serverMenu[1] +=currentServer+" "+ currentServerName;
  }else{
   serverMenu[0] +="0 servers.";
   serverMenu[1] +=currentServer+" "+currentServerName;
  }  
  displayMenu(mainMenu);
  while(true){
   System.out.print("[i,s,f,n,S,l,w,r,x]");
   String i = in.readLine();
   if(i.equals("i"))
    handleIdent();
   if(i.equals("s"))
    handleServer();
   if(i.equals("f")){
    System.out.print("Enter full name of IRC user:");
    props.setProperty("oak.fullname",in.readLine());
    changed = true;
    mainMenu[3] = "f). Full name (status: "+props.getProperty("oak.fullname")+")";
    continue;
   }
   if(i.equals("n")){
    System.out.print("Enter nick of IRC user:");
    props.setProperty("oak.nick",in.readLine());
    changed = true;
    mainMenu[4] = "n). Nick (status: "+props.getProperty("oak.nick")+")";
    continue;
   }
   if(i.equals("S")){
    System.out.println("Enter fully qualified name of the SecurityManager to be used (org.moxy.oak.security.PropertiesSecurityManager):");
    props.setProperty("oak.securitymanager",in.readLine());
    changed = true; 
    mainMenu[5] = "s). SecurityManager (status: "+props.getProperty("oak.securitymanager")+")";
    continue;
   }
   if(i.equals("l")){
    handleLog();
    continue;
   }
   if(i.equals("w")){
    props.store(new FileOutputStream(f));
    changed = false;
    continue;
   }
   if(i.equals("r")){
    System.out.println();
    displayMenu(mainMenu);
    continue;
   }
   if(i.equals("x")){
    if(changed){
     System.out.print("Changes were made save to file? [y/N]");
     i = in.readLine();
     if(i.toLowerCase().equals("y")){
      props.store(new FileOutputStream(f));
     }
    }
    return;
   }
  }
 }
 public static void handleLog() throws Exception{
  System.out.println();
  String s = props.getProperty("log.enabled");
  boolean enabled = false;
  if(s != null && s.equalsIgnoreCase("true")){
   enabled = true;
   logMenu[1] = "D). Disable logging";
  }else{
   enabled = false;
   logMenu[1] = "E). Enable logging";
  }
  displayMenu(logMenu);
  while(true){
   if(enabled)
    System.out.print("[D,d,s,r,b]");
   else
    System.out.print("[E,d,s,r,b]");
   String i = in.readLine();
   if(i.equals("d")){
    System.out.println("Enter the directory to store the log files in.");
    System.out.println("Current setting: "+props.getProperty("log.dir"));
    i = in.readLine();
    if(!i.equals("")){
     props.setProperty("log.dir",i);
     changed = true;
    }
    continue;
   }
   if(i.equals("D")){
    props.setProperty("log.enabled","false");
    logMenu[1] = "E). Enable logging";
    enabled = false;
    changed = true;
   }
   if(i.equals("E")){
    props.setProperty("log.enabled","true");
    logMenu[1] = "D). Disable logging";
    enabled = true;
    changed = true;
   }
   if(i.equals("s")){
    System.out.print("Enter the maximum log file size in megabytes:");
    System.out.println("Current setting: "+props.getProperty("log.size"));
    i = in.readLine();
    if(!i.equals("")){
     props.setProperty("log.size",i);
     changed = true;
    }
    continue;
   }
   if(i.equals("r")){
    displayMenu(logMenu);
    continue;
   }
   if(i.equals("b")){
    return;
   }
  }
 }
 public static void handleIdent() throws Exception{
  System.out.println();
  displayMenu(identMenu);
  while(true){
   System.out.print("["+(props.getProperty("oak.identd","false").equalsIgnoreCase("true") ? "d," : "e,")+"p,u,s,r,b]");
   String i = in.readLine();
   if(i.equals("d")){
    props.setProperty("oak.identd","false");
    changed = true;
    identMenu[0] = "Identd Menu (Status: disabled)";
    identMenu[1] = "e) Enable server";
    mainMenu[1] = "i) Identd Server (Status: disabled)";
    continue;
   }
   if(i.equals("e")){
    props.setProperty("oak.identd","true");
    changed = true;
    identMenu[0] = "Identd Menu (Status: enabled)";
    identMenu[1] = "d) Disable server";
    mainMenu[1] = "i) Identd Server (Status: enabled)";
    continue;
   }
   if(i.equals("p")){
    System.out.print("Enter port number:");
    props.setProperty("oak.identd.port",in.readLine());
    changed = true;
    identMenu[2] +=props.getProperty("oak.identd.port","113")+")";
    continue;
   }
   if(i.equals("u")){
    System.out.print("Enter user:");
    props.setProperty("oak.identd.user",in.readLine());
    changed = true;
    identMenu[3] = "u). User (status: "+props.getProperty("oak.identd.user")+")";
    continue;
   }
   if(i.equals("s")){
    System.out.print("Enter system type:");
    props.setProperty("oak.identd.system",in.readLine());
    changed = true;
    identMenu[4] = "s). System type (status: "+props.getProperty("oak.identd.system")+")";
    continue;
   }
   if(i.equals("r")){
    System.out.println();
    displayMenu(identMenu);
    continue;
   }
   if(i.equals("b"))return;
  }
 }

 public static void handleServer() throws Exception{
  System.out.println();
  displayMenu(serverMenu);
  while(true){
   System.out.print("[a,d,c,e,s,l,r,b]");
   String i = in.readLine();
   if(i.equals("a")){
    System.out.print("Host name or address:");
    String server = in.readLine();
    System.out.print("Port number 6667 is the default:");
    String port = in.readLine();
    String[] oldServers = props.getList("oak.servers");
    if(oldServers == null){
     oldServers = new String[1];
     oldServers[0] = server;
     props.setList("oak.servers",oldServers);
     props.setProperty("oak."+server+".port",port);
     currentServer = oldServers.length-1;
    }else{
     String[] newServers = new String[oldServers.length+1];
     for(int x = 0; x<oldServers.length; x++)
      newServers[x] = oldServers[x];
     newServers[newServers.length-1] = server;
     props.setList("oak.servers",newServers);
     props.setProperty("oak."+server+".port",port);
     currentServer = newServers.length-1;
    }
    currentServerName = server;
    String trash[] = props.getList("oak.servers",new String[0]);
    if(trash[currentServer] != null)currentServerName = trash[currentServer];
    serverMenu[0] +=trash.length+" servers.)";
    serverMenu[1] = "Current server: "+currentServer+" "+ currentServerName;
    changed = true;
    continue;
   }
   if(i.equals("d")){
    System.out.print("Are you sure you want to delete the current server? [y/N]");
    i = in.readLine();
    if(i.equals("y")||i.equals("Y")){
     String[] oldServers = props.getList("oak.servers",new String[0]);
     if(oldServers.length==0){
      System.out.println("No server currently exists");
      continue;
     }
     if(oldServers.length>=currentServer){
      if(oldServers.length == 0)
       props.setList("oak.servers",null);//clear list.
      else{      
       //rebuild the arrray
       String newServers[] = new String[oldServers.length-1];
       for(int x = 0; x<currentServer; x++)
        newServers[x] = oldServers[x];   
       for(int x = currentServer+1; x<newServers.length; x++)
        newServers[x-1] = oldServers[x];
       props.setList("oak.servers",newServers);
      }
      //get key's and look for a substring of this server and remove it.
      Enumeration enum = props.getPropertyKeys();
      String key = "";
      while(enum.hasMoreElements()){
       key = (String)enum.nextElement();
       String temp =props.getProperty(key);
       if(key.indexOf(oldServers[currentServer])>-1)props.setProperty(key,null);
      }
      enum = props.getListKeys();
      while(enum.hasMoreElements()){
       key = (String)enum.nextElement();
       if(key.indexOf(oldServers[currentServer])>-1)props.setList(key,null);
      }
     }
     currentServer = 0;
     String trash[] = props.getList("oak.servers",new String[0]);
     if(trash[currentServer] != null)currentServerName = trash[currentServer];
     serverMenu[0] +=trash.length+" servers.";
     serverMenu[1] +=currentServer+" "+ currentServerName;
     System.out.println("Current server DELETED");
    }else{
     System.out.println("Current server NOT deleted");
     continue;
    }
   }
   if(i.equals("c")){
    String[] channels = props.getList("oak."+currentServerName+".channels");
    if(channels == null){
     currentChannel = 0;
     currentChannelName = "undefined";
     channelMenu[0] = "Channel Menu: 0 channels";
    }else{
     currentChannel = 0;
     currentChannelName = channels[currentChannel];
    channelMenu[0] = "Channel Menu: "+channels.length+" channels";
    }
    channelMenu[1] = " Current Channe: "+currentChannel+" "+currentChannelName;
    handleChannel();
    continue;
   }
   if(i.equals("e")){
    handleConfigureServer();
    continue;
   }
   if(i.equals("s")){
    String[] servers = props.getList("oak.servers");
    if(servers == null){ 
     System.out.println("No servers.");
     continue;
    }
    System.out.print("Enter server number to switch to:");
    i = in.readLine();
    try{
     int number = Integer.parseInt(i);
     if(number > servers.length) number = servers.length;
     if(number < servers.length) number = 0;
     //number should be a valid number now
     String currentServerName = servers[number];
     currentServer = number;
     String trash[] = props.getList("oak.servers",new String[0]);
     if(trash[currentServer] != null)currentServerName = trash[currentServer];
     serverMenu[0] +=trash.length+" servers.";
     serverMenu[1] +=currentServer+" "+ currentServerName;
    }catch(NumberFormatException nfe){
     System.out.println("That couldn't be identified as a valid number.");
     continue;
    }
    continue;
   }
   if(i.equals("l")){
    String[] servers = props.getList("oak.servers");
    if(servers == null){
     System.out.println("No servers.");
     continue;
    }
    for(int x = 0; x<servers.length;x++){
     System.out.println(x+"). "+servers[x]);
     if((x+1)%15==0){
      System.out.print("Paused press enter to continue.");
      in.readLine();
     }
    }
   }
   if(i.equals("r")){
    System.out.println();
    displayMenu(serverMenu);
    continue;
   }
   if(i.equals("b")){
    return;
   }
  }
 }

 public static void handleChannel() throws Exception{
  System.out.println();
  displayMenu(channelMenu);
  while(true){
   System.out.print("[a,d,p,D,L,s,l,r,b]:");
   String i = in.readLine();
   if(i.equals("a")){  
    System.out.print("Channel name:");
    String chan = in.readLine();
    String[] oldChannels = props.getList("oak."+currentServerName+".channels");
    int newLength = 0;
    if(oldChannels == null){
     oldChannels = new String[1];
     oldChannels[0] = chan;
     props.setList("oak."+currentServerName+".channels",oldChannels);
     currentChannel = oldChannels.length-1;
     newLength = oldChannels.length;
    }else{
     String[] newChannels = new String[oldChannels.length+1];
     for(int x = 0; x<oldChannels.length; x++)
      newChannels[x] = oldChannels[x];
     newChannels[newChannels.length-1] = chan;
     props.setList("oak."+currentServerName+".channels",newChannels);
     currentChannel = newChannels.length-1;
     newLength = newChannels.length;
    }
    currentChannelName = chan;
    channelMenu[0] = "Channel Menu: "+newLength+" channels";
    channelMenu[1] = " Current Channe: "+currentChannel+" "+currentChannelName;
    changed = true;
    continue;
   }
   if(i.equals("d")){
    System.out.print("Are you sure you want to delete the current channel? [y/N]");
    i = in.readLine();
    if(i.equals("y")||i.equals("Y")){
     String[] oldChannels = props.getList("oak."+currentServerName+".channels",new String[0]);
     if(oldChannels.length==0){
      System.out.println("No channel currently exists");
      continue;
     }
     if(oldChannels.length>=currentChannel){
      if(oldChannels.length == 0)
       props.setList("oak."+currentServerName+".channels",null);//clear list.
      else{
       //rebuild the arrray
       String newChannels[] = new String[oldChannels.length-1];
       for(int x = 0; x<currentChannel; x++)
        newChannels[x] = oldChannels[x];   
       for(int x = currentChannel+1; x<newChannels.length; x++)
        newChannels[x-1] = oldChannels[x];
       props.setList("oak."+currentServerName+".channels",newChannels);
      }
      if(props.getList("oak."+currentServerName+"."+currentChannelName)!=null)
       props.setList("oak."+currentServerName+"."+currentChannelName,null);
     }
     currentChannel = 0;
     currentChannelName = "undefined";
     String trash[] = props.getList("oak."+currentServerName+".channels",new String[0]);
     if(trash[currentChannel] != null)currentChannelName = trash[currentServer];
     channelMenu[0] = "Channel Menu: "+trash.length+" channels";
     channelMenu[1] = " Current Channe: "+currentChannel+" "+currentChannelName;
     System.out.println("Current channel DELETED");
    }else{
     System.out.println("Current channel NOT deleted");
     continue;
    }
   }
   if(i.equals("P")){
    System.out.println("Enter fully qualified plugin name:");
    String plugName = in.readLine();
    System.out.println("Enter any parameters if neccessary:");
    String params = in.readLine();
    if(params == null)params = "";
    plugName+=" "+params;
    String[] plugs = props.getList("oak."+currentServerName+"."+currentChannelName);
    if(plugs == null){
     plugs = new String[1];
     plugs[0] = plugName;
     props.setList("oak."+currentServerName+"."+currentChannelName,plugs);
    }else{
     String newPlugs[] = new String[plugs.length+1];
     for(int x = 0; x<plugs.length; x++)
      newPlugs[x] = plugs[x];
     newPlugs[newPlugs.length-1] = plugName;
     props.setList("oak."+currentServerName+".plugins",newPlugs);
    }
    changed = true;
    continue;
   }
   if(i.equals("D")){
    String[] plugs = props.getList("oak."+currentServerName+"."+currentChannelName);
    System.out.print("Enter the number of the plugin to delete:");
    i = in.readLine();
    int chosen;
    try{
     chosen = Integer.parseInt(i);
     if(chosen<0 || chosen>plugs.length){
      System.out.println("Not a valid plugin. Plugin NOT deleted.");
      continue;
     }
    }catch(NumberFormatException nfe){
     System.out.println("Not a valid number. Plugin NOT deleted.");
     continue;
    }
    System.out.print("Are you sure you want to delete\n"+plugs[chosen]+"? [y/N]");
    i = in.readLine();
    if(i.equals("y")||i.equals("Y")){
     if(plugs.length == 1 && chosen==0){
      props.setList("oak."+currentServerName+"."+currentChannelName,null);
     }else{
      String[] newPlugs = new String[plugs.length-1];
      for(int x = 0;x<chosen;x++)
       newPlugs[x] = plugs[x];
      for(int x = chosen+1; x<newPlugs.length; x++)
       newPlugs[x-1] = plugs[x];
      props.setList("oak."+currentServerName+"."+currentChannelName,newPlugs);
     }
    }else{
     System.out.println("No plugin deleted.");
    }
   }
   if(i.equals("L")){
    String[] plugs = props.getList("oak."+currentServerName+"."+currentChannelName);
    if(plugs == null){
     System.out.println("No channels currently defined.");
     continue;
    }
    for(int x = 0; x<plugs.length; x++){
     System.out.println(x+") "+plugs[x]);
     if((x+1)%15==0){
      System.out.println("Paused press enter to continue.");
      in.readLine();
     }
    }
    continue;
   }
   if(i.equals("s")){
    String[] chans = props.getList("oak."+currentServerName+".channels");
    if(chans == null){
     System.out.println("No channels.");
     continue;
    }
    System.out.println("Enter channel number to switch to:");
    i = in.readLine();
    try{
     int x = Integer.parseInt(i);
     if(x>=chans.length) x = chans.length-1;
     if(x<0)x = 0;
     currentChannel = x;
     currentChannelName = chans[x];
     continue;
    }catch(NumberFormatException nfe){
     System.out.println("Not a valid number.");
     continue;
    }
   }
   if(i.equals("l")){
    String chans[] = props.getList("oak."+currentServerName+".channels");
    for(int x = 0; x<chans.length; x++){
     System.out.println(x+") "+chans[x]);
     if((x+1)%15==0){
      System.out.println("Paused press enter to continue.");
      in.readLine();
     }
    }
   }
   if(i.equals("r")){
    System.out.println();
    displayMenu(channelMenu);
    continue;
   }
   if(i.equals("b")){
    return;
   }
  }
 }

 public static void handleConfigureServer() throws Exception{
  configureServer[0] = "Configure Server (Status: "+currentServer+")";
  configureServer[1] = "h) Host (Status: "+currentServerName+")";
  configureServer[2] = "p) Port (Status: "+props.getProperty("oak."+currentServerName,"6667");
  System.out.println();
  displayMenu(configureServer);
  while(true){
   System.out.print("[h,p,a,d,l,r,b]:");
   String i = in.readLine();
   if(i.equals("h")){
    System.out.print("Enter host name or address:");
    i = in.readLine();
    Enumeration enum = props.getPropertyKeys();
    String key;
    while(enum.hasMoreElements()){
     key = (String)enum.nextElement();
     if(key.indexOf(currentServerName)>-1){
      String newKey = key.substring(0,key.indexOf(currentServerName));
      newKey+=i+key.substring(key.indexOf(currentServerName)+currentServerName.length());
      props.setProperty(newKey,props.getProperty(key));//set new key
      props.setProperty(key,null);//remove old key
     }
    }  
    enum = props.getListKeys();
    while(enum.hasMoreElements()){
     key = (String)enum.nextElement();
     if(key.indexOf(currentServerName)>-1){
      String newKey = key.substring(0,key.indexOf(currentServerName));
      newKey+=i+key.substring(key.indexOf(currentServerName)+currentServerName.length());
      props.setList(newKey,props.getList(key));//set new key
      props.setList(key,null);//remove old key
     }
    }
    currentServerName = i;
    String[] servers = props.getList("oak.servers");
    servers[currentServer] = i;
    configureServer[1] = "h) Host (Status: "+i+")";
    changed = true;
    continue;
   }
   if(i.equals("p")){
    System.out.print("Port:");
    i = in.readLine();
    configureServer[2] = "p) Port (Status:"+i+")";
    props.setProperty("oak."+currentServerName,i);
    changed = true;
    continue;
   }
   if(i.equals("a")){
    System.out.println("Enter fully qualified name of plugin:");
    String plug = in.readLine();
    System.out.println("Enter any parameters if neccessary:");
    String params = in.readLine();
    if(params == null) params = "";
    plug += params;
    String[] plugs = props.getList("oak."+currentServerName+".plugins");
    if(plugs == null){
     plugs = new String[1];
     plugs[0] = plug;
     props.setList("oak."+currentServerName+".plugins",plugs);
    }else{
     String[] newPlugs = new String[plugs.length+1];
     for(int x = 0; x<plugs.length; x++)
      newPlugs[x] = plugs[x];
     newPlugs[newPlugs.length-1] = plug;
     props.setList("oak."+currentServerName+".plugins",newPlugs);
    }
    changed = true;
    continue;
   }
   if(i.equals("d")){
    String plugs[] = props.getList("oak."+currentServerName+".plugins");
    if(plugs == null){
     System.out.println("No plugins to delete.");
     continue;
    }
    System.out.print("Enter plugin number to delete:");
    i = in.readLine();
    try{
     int p = Integer.parseInt(i);
     if(p<0 || p>plugs.length){
      System.out.println("Not a valid number.");
      continue;
     }
     String newPlugs[] = new String[plugs.length-1];
     for(int x = 0; x<p; x++)
      newPlugs[x] = plugs[x];
     for(int x = p+1; x<plugs.length;x++)
      newPlugs[x-1] = plugs[x];
     changed = true;
     continue;
    }catch(NumberFormatException nfe){
     System.out.println("Not a valid number.");
     continue;
    } 
   }
   if(i.equals("l")){
    String[] plugs = props.getList("oak."+currentServerName+".plugins");
    if(plugs == null){
     System.out.println("No plugins.");
     continue;
    }
    for(int x= 0; x<plugs.length; x++){
     System.out.println(x+") "+plugs[x]);
     if((x+1)%15==0){
      System.out.println("Paused press enter to continue.");
      in.readLine();
     }
    }
   }
   if(i.equals("r")){
    System.out.println();
    displayMenu(configureServer);
    continue;
   }
   if(i.equals("b")){
    return;
   }
  }
 }


 public static void displayMenu(String[] menu){
  for(int x = 0; x<menu.length; x++)
   System.out.println(menu[x]);
 }

 public static String getFile(){
  String retValue = System.getProperty("user.home");
  String fileSep = System.getProperty("file.separator");
  //quick check for system descripencies.
  if(!retValue.endsWith(fileSep))retValue+=fileSep;
  retValue+=".OakTJB";
  File f = new File(retValue);
  if(!f.exists())
   f.mkdirs();
  retValue+=fileSep+"oak.properties";
  return retValue;
 }

}
