/**
 * This is the heart of Oak The Java Bot.<br>
 * Todo: some console commands. An idea is to create
 * a command to set the "current" IRCConnection then
 * implement some basic commands that use the current
 * IRCConnection.
 * @version 1.0
 * @author Marcus Wenzel
 */
/*/
 * An IRC bot.
 * Copyright (C) 2000 Marcus Wenzel
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.moxy.oak;
import org.moxy.irc.*;
import org.moxy.util.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.commandplug.*;
import org.moxy.oak.plugin.*;
import org.moxy.oak.security.*;
import java.util.*;
import java.io.*;
import java.net.UnknownHostException;
import java.lang.reflect.Constructor;

public class Oak extends DefaultIRCListener {


	private Hashtable connections = new Hashtable();
	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private OakCommand commandPlug = new OakCommand();
	private BotSecurityManager loginManager;
	private IdentdThread identserver = null;
	private ListableProperties props = new ListableProperties();
	private String currentConsoleConnection = "";
	private Vector serverPlugins = new Vector();
	private Hashtable channelPlugins = new Hashtable();
	private DCCChatManager dccManager = new DCCChatManager();
	private File propsFile;
	private PrintStream screen = System.out;
	

    /**
     * @since 1.0
     */
 public Oak(){
  printLogo();
  String s;
  s = System.getProperty("user.home");
  if(!s.endsWith(System.getProperty("file.separator"))){
   s = s+System.getProperty("file.separator");
  }
  s+=".OakTJB";
  propsFile = new File(s,"oak.properties");
  try{
   s = s+".OakTJB";
   if(propsFile.exists()){
    props.load(new FileInputStream(propsFile));
    String securityManager = props.getProperty("oak.securitymanager","org.moxy.oak.security.PropertiesSecurityManager");
    try{
     loginManager = (BotSecurityManager)Class.forName(securityManager).newInstance();
    }catch(Exception e){
     e.printStackTrace();
     System.err.println("An error occured while setting the custom security manager going to default. PropertiesSecurityManager.");
     loginManager = new PropertiesSecurityManager();
    }
    String identdEnabled = props.getProperty("oak.identd",null);
    if(identdEnabled != null && identdEnabled.toLowerCase().equals("true")){
     int iport = 113;
     try{
      iport = Integer.parseInt(props.getProperty("oak.identd.port","113"));
     }catch(NumberFormatException nfe){
      System.err.println("Error reading oak.identd.port value defaulting to 113");
      iport = 113;
     }
     String iuser = props.getProperty("oak.identd.user","user");
     String isystem = props.getProperty("oak.identd.system","java"+System.getProperty("java.version"));
     identserver = new IdentdThread(iport,iuser,isystem);
     iuser = isystem = null;
    }
   }else{
    System.out.println("The configuration file wasn't found please run a configuration program first.");
    System.out.println("Run OakConfigurator now?[y/N]");
    String response = in.readLine();
    if(response.toLowerCase().equals("y")){
     OakConfigurator.main(null);
    }
    System.exit(-1);//no config file found.
   }
  }catch(Exception e){System.err.println(e);}
  loginManager.init(this);
  commandPlug.init(this,loginManager);
  runListableProperties(props);
  startNewLogFile();
 }
 public void startNewLogFile(){
  if(!props.getProperty("oak.log.enabled","").equalsIgnoreCase("true")){return;}
  String dir = props.getProperty("oak.log.dir");
  if(dir == null)
   dir = System.getProperty("user.home","");
  GregorianCalendar cal = new GregorianCalendar();
  String logFile = new String("oak_");
  logFile += cal.get(cal.YEAR)+"_"+cal.get(cal.MONTH)+"_";
  logFile += cal.get(cal.DAY_OF_MONTH)+"_"+cal.get(cal.HOUR)+"_";
  logFile += cal.get(cal.MINUTE)+"_"+cal.get(cal.SECOND);
  try{
	  PrintStream output = new PrintStream(new FileOutputStream( new
		File(dir,logFile+".log")));
	  PrintStream errput = new PrintStream(new FileOutputStream( new
		File(dir,logFile+".err")));
	  System.setOut(output);
	  System.setErr(errput);  
  }catch(IOException ioe){
   ioe.printStackTrace();
   System.out.println("There was a problem creating the log file.\n"+
	"Not starting a new log file.");
  }
 } 

 public void runListableProperties(ListableProperties prop){
  //does not check for idetd shit.
  String nick = prop.getProperty("oak.nick","OakBot");
  String fullname = prop.getProperty("oak.fullname","Oak the IRC Java Bot http://oak.sourceforge.net");
  String servers[] = prop.getList("oak.servers");
  if(servers == null){return;}//no servers to join we can't do squat ;oP
  //loop through servers joining them and then joining the channels
  // and loading their plugins
  int port = 6667;
  for(int currentServer = 0; currentServer<servers.length; currentServer++){
   try{
    port = Integer.parseInt(prop.getProperty("oak."+servers[currentServer],"6667"));
   }catch(NumberFormatException nfe){port = 6667;}
   IRCConnection connection;
   try{
    connection = getIRCConnection(createNewConnection(servers[currentServer],port,nick,fullname));
   }catch(Exception e){
    if(Preferences.DEBUG_LEVEL>=10)e.printStackTrace();
    else System.err.println("Couldn't connect to "+servers[currentServer]);
    continue;
   }
   //load OakPlugins first then the channel plugins
   String[] oplugins = prop.getList("oak.servers."+ currentServer +".plugins");
   if(oplugins != null){  
    for(int currentOPlugin = 0; currentOPlugin<oplugins.length; currentOPlugin++){
     String plugname = oplugins[currentOPlugin];
     String[] params = null;
     try{
      if(plugname.indexOf(" ")>-1){
       StringTokenizer st = new StringTokenizer(oplugins[currentOPlugin]);
       plugname = st.nextToken();
       params = new String[st.countTokens()];
       for(int x =0; x<params.length; x++)
        params[x] = st.nextToken();
      }
      OakPlugin plug = (OakPlugin)(Class.forName(plugname)).newInstance();
      loadPlugin(plug,params,connection);
     }catch(Exception e){
      if(Preferences.DEBUG_LEVEL >= 10)e.printStackTrace();
      else System.err.println("Error loading "+plugname);
     }
    }//for(int currentOPlugin...)
   }//if(oplugins!=null)
   String channels[] = prop.getList("oak.servers." + currentServer + ".channels");
   if(channels != null){
    //let's load the plugins then join the channels.
    for(int currentChannel = 0; currentChannel<channels.length; currentChannel++){
     String plugins[] = prop.getList("oak.servers." + currentServer + ".channels." + currentChannel + ".plugins");
     if(plugins!= null){
      //we've got plugins so let's load them before we join the channel
      for(int currentPlugin = 0; currentPlugin<plugins.length; currentPlugin++){
       String plugname = plugins[currentPlugin];
       String params[] = null;
       if(plugins[currentPlugin].indexOf(" ")>-1){
        //we have params we need to parse.
        StringTokenizer st = new StringTokenizer(plugins[currentPlugin]);
        plugname = st.nextToken();
        params = new String[st.countTokens()];
        for(int x = 0; x<params.length; x++)
          params[x] = st.nextToken();
       }//if(plugins[currentPlugin]....
       try{
        ChannelPlugin plug = (ChannelPlugin)(Class.forName(plugname)).newInstance();
        loadChannelPlugin(plug,
		"oak.servers." + currentServer + ".channels." + currentChannel + ".plugins." + currentPlugin, 
		params,channels[currentChannel],connection, String.valueOf((int)(Math.random()*1000)));
       }catch(Exception e){
        if(Preferences.DEBUG_LEVEL>=10)
         e.printStackTrace();
        else System.err.println("Error loading "+plugname);
       }//catch
      }//for(int currentPlugin = 0; currentPlugin<plugins.length; currentPlugin++)...
     }//if(plugins!=null)....
     //plugins have been loaded if they exist now we can join the channel
     connection.joinChannel(channels[currentChannel]);
    }//for(int currentChannel....
   }//if(channels!=null)
   //server plugins loaded
  }//for(int currentServer....)
 }

    /**
     * Returns the current IRCConnection. 
     * @since 1.0
     * @returns the current IRCConnection.
     **/
    public IRCConnection getCurrentConnection(){
     return getIRCConnection(currentConsoleConnection);
    }

    /**
       * Creates and adds a new IRCConnection to the bot with a random identifier.
       * @since 1.0
       * @param server the hostname or ip address of the IRC server
       * @param port the port to connect on.
       * @param nick the nick to use.
       * @param name the full name to use.
       */
    public String createNewConnection(String server, int port, String nick,
            String name) throws UnknownHostException, IOException {
        IRCCommandConnection connect =
                new IRCCommandConnection(server, port, nick, name, identserver);
        return addNewConnection(connect);
    }

    /**
       * Adds a new IRCConnection to the bot with the specified identifier.
       * @since 1.0
       * @param con the IRCConnection to add.
       * @param identifier the identifier to use.
       */
    public void addNewConnection(IRCCommandConnection con,
            String identifier) throws UnknownHostException, IOException {
	//if the current console isn't set then set it to the newly created one.
        if(currentConsoleConnection.equals(""))currentConsoleConnection = identifier;
        connections.put(identifier, con);
        con.registerIRCListener(this); //register this so we can listen to ctcp and other pertanit events.
        con.registerIRCListener(commandPlug);//register the command listener for ctcp commands
	con.registerIRCListener(dccManager);//this way all connections share the same dcc stuff.
        con.connect();//finally connect to the server
        display("Connected to "+con.getServer() + " on "+identifier + " as "+
                con.getNick());//display on the console that we connected  and the nick we connected with.
    }

    /**
       * Adds a new IRCConnection to the bot with a random identifier.
       * @since 1.0
       * @param con the IRCConnection to add.
       */
    public String addNewConnection(IRCCommandConnection con)
            throws UnknownHostException, IOException {
        int i;
	//create a pesudo random identifier
        while (true) {
            i = (int)(Math.random() * 400);//'tween 0 and 400
            if (connections.get(String.valueOf(i)) == null) {
                break;//if it's not taken use it.
            }
        }
        addNewConnection(con, String.valueOf(i));//adds the connection
     return String.valueOf(i);//returns the identifier.
    }

    /**
       * Returns the identifier for an IRCConnection.
       * @since 1.0
       * @param connection the connection to look the key up for.
       * @return the identifier for the connection.
       */
    public String getConnectionIdentifier(IRCConnection connection) {
        Enumeration enum = connections.keys();//get all the keys for the connections
        String key;
        while (enum.hasMoreElements()) {
            key = (String) enum.nextElement();
            if (connections.get(key) == connection) {//compare the value of each key to the param
                return key;//return the key if it's here
            }
        }
        return null;//return null if it wasn't found.
    }

    /**
       * Returns all the connection identifiers.
       * @since 1.0
       * @return all the connection identifiers.
       */
    public Enumeration getAllConnectionIdentifiers() {
        return connections.keys();
    }

    /**
       * Removes the connection refered to by the identifier.
       * @since 1.0
       * @param identifier the identifier of the connection to remove.
       */
    public void removeConnection(String identifier) {
        connections.remove(identifier);
    }

    /**
     * Remove's an IRCConnection from the list.
     * @param connection to remove.
     **/
    public void removeConnection(IRCCommandConnection connection) {
      String con = getConnectionIdentifier(connection);
      if(con != null){ connection.disconnect();connections.remove(con);}
    }

    /**
       * Returns an Enumeration of all the IRCConnections the bot's currently on.
       * @since 1.0
       * @return all the IRCConnections the bot's on.
       */
    public Enumeration getAllConnections() {
        return connections.elements();
    }

    /**
       * Returns the IRCConnection for the specific identifier.
       * @since 1.0
       * @param identifier the identifier to look the connection up for.
       * @return the IRCConnection coresponding to the identifier.
       */
    public IRCCommandConnection getIRCConnection(String identifier) {
        //if(identifier == null)return null;
        return (IRCCommandConnection)connections.get(identifier);
    }

    /**
       * Puts a persistant value in the configuration file.
       * This method will attempt save the file before returning.
       * @since 1.0
       * @param key The key to save the value with.
       * @param value The value to save.
       */
    public void putProperty(String key, String value) {
        props.setProperty("plugs."+key, value);
        try {
            props.store(new FileOutputStream(propsFile));
        } catch (IOException ioe) {
            display("Error occured while saving configuration file. "+
                    "Possible persistant data corruption.");
        }
    }

    /**
       * Retrieves a value from the persistant storage.
       * @since 1.0
       * @param key The key of the value.
       * @return The value or null if it wasn't found.
       */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     * Checks to see if the idend server is enabled 
     * via the startup configuration file.
     * @since 1.0
     * @returns true if the server was enabled false otherwise
     **/
    public boolean isIdentdEnabled(){
     return identserver != null;
    }

    /**
       * Retrieves a value from the persistant storage.
       * @since 1.0
       * @param key The key of the value.
       * @param value The value to return if key's not found.
       * @return The value of the key or value if it's not found.
       */
    public String getProperty(String key, String value) {
        return props.getProperty(key, value);
    }

    /**
       * Used internally to print a message to the console when the bot
       * joins a channel.
       * @since 1.0
       */
    public void handleJOIN(IRCConnection connection, String sender, String channel) {
	if(sender.toLowerCase().startsWith(connection.getNick().toLowerCase())){
            display("Joined "+channel);
        }
    }

    /**
       * Used internally to print a message to the console when the bot
       * parts a channel.
       * @since 1.0
       */
    public void handlePART(IRCConnection connection, String nick, String channel, String msg) {
	if(nick.toLowerCase().startsWith(connection.getNick().toLowerCase())){
            display("Left "+channel);
        } else {
            if (loginManager.isLoggedIn((IRCCommandConnection)connection, nick) &&
                    !isOnAnyChannel(connection, nick)) {
                loginManager.logout((IRCCommandConnection)connection, nick);
            }
        }
    }


    /**
       * Used internally to log people out that quit irc.
       * @since 1.0
       */
    public void handleQUIT(IRCConnection connection, String nick, String msg) {
        if (loginManager.isLoggedIn((IRCCommandConnection)connection, nick))
            loginManager.logout((IRCCommandConnection)connection, nick);
    }

    public void handle433(IRCConnection connection, IRCMessage message){
	System.out.println("Nick changed to "+connection.getNick());
    }

    /**
       * Used internally to change the nicks of the logged in users when a nick command is recieved.
       * @since 1.0
       */
    public void handleNICK(IRCConnection connection, String onick, String newName) {
	String nnick = newName+onick.substring(onick.indexOf("!"));
        if (loginManager.isLoggedIn((IRCCommandConnection)connection, onick)) {        
            loginManager.nickChanged((IRCCommandConnection)connection, onick, nnick);
        }
    }

    /**
       * Used internally to log users out that leave the sight of the bot.
       * @since 1.0
       */
//EDITING
    public void handleKICK(IRCConnection connection, String kicker, String channel, String nick, String msg) {
		System.err.println("A user was kicked that may not have been logged out of the bot.");
        if (loginManager.isLoggedIn((IRCCommandConnection)connection, nick) &&
                !isOnAnyChannel(connection, nick)) {
            loginManager.logout((IRCCommandConnection)connection, nick);
        }
    }

    /**
       * Used internally to print errors to the console.
       * @since 1.0
       */
    public void handleERROR(IRCConnection connection, IRCMessage message) {
        //I ain't changing this it's a good use of rawLine!
	System.out.println(message.getRawLine());
    }

    /**
       * Used internally to print notices to the console.
       * @since 1.0
       */
//EDITING
    public void handleNOTICE(IRCConnection connection, String sender, String target, String msg) {
        display("NOTICE from " + sender + " to " + target + ": " + msg);
    }

    /**
     * Used internally to log people in and out.
     * @since 1.0
     */
    public void handleCTCPMSG(IRCConnection connection, String sender, String target, String msg) {
        IRCLine line = new IRCLine(msg);
		String command = line.getNextToken();
        if (command.toLowerCase().equals("version"))
		{
            connection.send("NOTICE "+IRCMessage.getNick(sender) + " :VERSION "+Preferences.versionString+" http://oak.sourceforge.net");
            return;
        }
		if (command.equals("PASSWD"))
		{
			String user = line.getNextToken();
			String opwd = line.getNextToken();
			String npwd = line.getNextToken();
			loginManager.changePassword(sender,user,opwd,npwd,(CommandConnection)connection);
			return;
    	}
        if (command.equals("LOGIN"))
		{
            String userName;
            String password;
            try
			{
                userName = line.getNextToken();
                password = line.getNextToken();
            }
			catch (Exception e)
			{
//               connection.send("NOTICE "+nick + " :LOGIN Login failed.");
                display("Login attempt by: "+sender + " on "+new Date() + " unsuccessfull, incorrect number or format of parameters");
                return;
            }
            OakUser bu = loginManager.login((IRCCommandConnection)connection, sender, userName, password);
            if (bu == null) {
                display("Login attempt by "+sender + " failed "+  new Date());
                //connection.send("NOTICE "+nick + " :LOGIN Login failed.");//no output if not loged into bot.
                return;
            }
            System.out.println("Login successful for "+sender + " "+new Date());
            connection.send("NOTICE "+IRCMessage.getNick(sender) + " :LOGIN Login successfull.");
            return;
        }
        if (command.equals("LOGOUT"))
		{
            loginManager.logout((IRCCommandConnection)connection, sender);
        }
    }


    /**
       * Not used just implemented.
       * @since 1.0       
    public void handleConnect(IRCConnection connection) {}
*/
    /**
       * not used just implemented.
       * @since 1.0
       
    public void handleDisconnect(IRCConnection connection) {
        //display("Disconnected.");
    }
*/
	
    /**
     * Loads a channel plugin with the specified identifier.
     * @since 1.0
     * @param plug the plugin to load
     * @param params the params to pass to the plugin during init
     * @param channel the channel to load the plugin in.
     * @param con the connection to load the plugin on.
     * @param identifier the identifier to load it with.
     */
    public void loadChannelPlugin(ChannelPlugin plug, String id, String[] params,
                  String channel, IRCConnection con, String identifier){
     plug.initChannelPlugin(this,id, params,channel,identifier);
     if(channelPlugins.get(identifier)!= null)return;//throw exception
     con.registerChannelListener(channel,plug);
     channelPlugins.put(identifier, plug);
    }

    /**
     * Unloads a channel plugin
     * @since 1.0
     * @param identifier the identifier of the plugin to unload.
     */
    public void unloadChannelPlugin(String identifier){
     ChannelPlugin plug = (ChannelPlugin)channelPlugins.get(identifier);
     plug.destroy();
     channelPlugins.remove(identifier);
    }

    /**
     * Loads a commandplug into Oak.
     * @since 1.0
     * @param plugin the plugin to load.
     */
    public void loadCommandPlug(CommandPlug plugin){
	commandPlug.loadCommandPlugin(plugin);
    }

    /**
     * Unloads a specified command.
     * @since 1.0
     * @param command the command to unload
     */
     public void unloadCommandPlug(String command){
      commandPlug.unloadCommandPlugin(command);
     }

    /**
       * Loads a plugin into Oak.
       * @since 1.0
       * @param plugin the plugin to load.
       * @param params the parameters to load the plugin with.
       * @param server the identifier of the IRCConnection to load the plugin on.
       */
    public void loadPlugin(OakPlugin plugin, String[] params, IRCConnection con) {
	if(con == null)return;
        serverPlugins.addElement(plugin);
        plugin.initFullServerPlugin(this, params, String.valueOf(serverPlugins.size()),con);
        con.registerIRCListener(plugin);
    }

     /**
       * Unloads a server plugin.
       * @since 1.0
       * @param plugin the plugin to remove.
       * @param server the server identifier it's loaded on.
       */
    public void unloadPlugin(int identifier) {
     Enumeration enum = serverPlugins.elements();
	OakPlugin p2;
	while(enum.hasMoreElements()){
		p2 = (OakPlugin)enum.nextElement();
		if(p2.getIdentifier().equals(String.valueOf(identifier))){
			p2.destroy();
			p2.getConnection().unregisterIRCListener(p2);
			serverPlugins.removeElement(p2);
		}
	}     
    }

 

     /**
       * Returns all server plugins currently loaded.
       * @since 1.0
       * @return An enumeration of OakPlugins.
       */
    public Enumeration getPlugins() {
        return serverPlugins.elements();
    }
    /**
       * Returns all the channel plugins currently loaded.
       * @since 1.0
       * @return An enumeration of ChannelPlugins.
       */
    public Enumeration getChannelPlugins() {
        return channelPlugins.elements();
    }

    /**
     * Returns wether a user is currently logged in.
     * @since 1.0
     **/
    public boolean isLoggedIn(CommandConnection connection, String nick){
     return loginManager.isLoggedIn(connection,nick);
    }


    /**
       * Starts the bot off.
       * @since 1.0
       */
    public static void main(String arg[]) {
        Oak bot = new Oak();
    }


    /**
       * Returns if a nick is on any channel on a specified server.
       * @since 1.0
       * @param connection the server to look on.
       * @param nick the nick to search for.
       */
    public boolean isOnAnyChannel(IRCConnection connection, String nick) {
        Enumeration enum = connection.getChannels();
        while (enum.hasMoreElements()) {
            if (((Channel) enum.nextElement()).isHere(nick)) {
                enum = null;
                return true;
            }
        }
        return false;
    }

    /**
       * Shows a string on the console or holds the string in memory for later
       * showing if in configure mode.
       * @since 1.0
       * @param s the string to show.
       */
    public void display(String s) {
     System.out.println(s);
    }

    //public void display(String s, java.awt.Color color){} //display a message in a color.

    /**
       * Checks access for a nick to a section of the bot.
       * @since 1.0
       * @param privilage the permission requested.
       * @param user the nick of the user.
       * @param connection connection of the user.
       * @return true if the action is granted false otherwise.
       */
    public boolean checkAccess(String privilege, String nick,
            CommandConnection connection) {
        return loginManager.hasAccess(nick, connection, privilege);
    }
    /**
       * Checks access for a nick to a section of the bot.
       * @since 1.0
       * @param privialage the permission requested.
       * @param channel the channel of th epermission or null if it doesn't pertain to a specific channel.
       * @param nick the nick of the user.
       * @param connection the connection the user is on.
       * @param server the hostname of the server the action will occur on.
       */
    public boolean checkAccess(String privilage, String channel, String nick,
            CommandConnection connection, IRCConnection target) {
        return loginManager.hasAccess(nick, connection, target, channel, privilage);
    }

    /**
       * Checks access for a nick to a section of the bot for a possibly
       * non existent IRCConnection. This is used by the SM commands to check
       * a for a users access rights to a possibly disconnected network. Some of
       * the SM commands allow the user to be on one network and adminster another.
       *
       * @sinec 1.0
       * @param privilege the permission requested.
       * @param channel the channel of the permission or null if it doesn't pertain to a specific channel.
       * @param nick the nick of the user.
       * @param connection the  connection the user is currently on.
       * @param network the network the user is tring to administer.
       **/
    public boolean checkAccess(String privilege, String channel, String nick,
            CommandConnection connection, String network) {
        return loginManager.hasAccess(nick, connection, network, channel,
                privilege);
    }

   //I think this is a security risk.
    private BotSecurityManager getSecurityManager() {
        return loginManager;
    }

 public void handleIRCMessage(IRCConnection connection, IRCMessage message){
	if(message.getMsgType() == message.ERR_NICKNAMEINUSE){
		handle433(connection,message);
	}else
		super.handleIRCMessage(connection,message);
 }

    
 private void printLogo(){
  System.out.println("      ::::::          ::");
  System.out.println("    :::::::::         ::");
  System.out.println("   :::#####:::        ::");
  System.out.println("   ::#     #::  ::::  ::   ::");
  System.out.println("  :::       :: :::::: ::  :::");
  System.out.println("  ::#       :::::##:: :: :::#  version "+ Preferences.versionNumString);
  System.out.println("  ::        ::::#  :: :::::#");
  System.out.println("  ::       ::#::   :: ::::::  Copyleft by:");
  System.out.println("  #::     ::: ::   :: ::##::   Marcus Wenzel & ");
  System.out.println("   :::::::::# ::::::: ::  #::  Christiaan Welvaart");
  System.out.println("   ##:::::##  #:::::: ::   ::   Some portions copyleft Marc Dumontier");
  System.out.println("     #####     ###### ##   ##  OpenSource ROCKS!");
 }
}
