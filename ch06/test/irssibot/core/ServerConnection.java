/*
 * $Id: ServerConnection.java,v 1.2 2001/04/10 19:41:40 matti Exp $
 *
 * IrssiBot - An advanced IRC automation ("bot")
 * Copyright (C) 2000 Matti Dahlbom
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * mdahlbom@cc.hut.fi
 */
package irssibot.core;

/* import other IrssiBot packages */
import irssibot.config.*;
import irssibot.util.*;
import irssibot.user.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * represents a connection to an irc server
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.2 $
 */
public class ServerConnection extends Thread
{
    /* pointer to Core object */
    private Core core = null;

    /* socket/connection state data */
    private BufferedInputStream serverIn = null;
    private BufferedReader serverInReader = null;
    private BufferedOutputStream serverOut = null;
    private Socket socket = null;
    private String errorMsg = null;
    private String statusString = null;

    private boolean connectionAlive = false;
    private boolean continueConnecting = true;

    /* misc data */
    private ServerInstanceData instanceData = null;
    private Vector users = null;
    private Vector channelConnects = null;
    private Hashtable channels = null;
    private Host botHost = null;
    private ModeQueue modeQueue = null;

    /* server info data */
    private String serverUserModes = null;
    private String serverChannelModes = null;

    /* state data */
    private String currentServer = null;
    private int currentServerIndex = 0;
    private boolean useAltNick = false;
    private Timer autoSaveTimer = null;
    private boolean userDataChanged = false;
    private boolean connectionReady = false;

    /* event timings */
    private long nickRegainTime = 0;
    private long channelRejoinTime = 0;

    /* callback targets */
    private Nick whoisTargetNick = null;
    private Channel whoTargetChannel = null;

    /**
     * Returns the allowed channel modes for the current server.
     *
     * @return possible channel modes for the server
     */
    public String getServerChannelModes() { return serverChannelModes; }

    public ServerConnection(ThreadGroup group,ServerInstanceData instanceData,Core core)
    {
	super(group,instanceData.getNetwork());

	this.core = core;
	putlog("constructed (\""+instanceData.getNetwork()+"\")");

	this.instanceData = instanceData;
	this.channels = instanceData.getChannels();

	channelConnects = new Vector();

	/* parse user file */
	UserFileParser parser = new UserFileParser(instanceData.getUserFilePath());
	if( parser.parse() ) {
	    users = parser.getUsers();
	} else {
	    users = new Vector();
	}

	/* create & launch a modequeue */
	modeQueue = new ModeQueue(this);
	modeQueue.start();
    }

    public Vector getUsers() { return users; }
    public ServerInstanceData getInstanceData() { return instanceData; }
    public String getConnectionStatus() { return statusString; }
    public Hashtable getChannels() { return channels; }
    public Host getHost() { return botHost; }
    public Vector getChannelConnects() { return channelConnects; }
    public ModuleHandler getModuleHandler() { return core.getModuleHandler(); }
    public ModeQueue getModeQueue() { return modeQueue; }

    public void addUser(User user)
    {
	users.add(user);
    }

    /**
     * Deletes a user from bots user record.
     *
     * @param user user to delete
     * @param <ul><li>true if successful<li>false if not found</ul>
     */
    public boolean delUser(User user) 
    {
	boolean ret = false;

	if( (user != null) && users.contains(user) ) {
	    ret = users.remove(user);
	}
	
	return ret;
    }

    /**
     * Broadcasts a message to all channels.
     *
     * @param message message to broadcast
     */
    public void channelBroadcast(String message) 
    {
	Enumeration enum = channels.elements();
	while( enum.hasMoreElements() ) {
	    Channel channel = (Channel)enum.nextElement();
	    write("PRIVMSG " + channel.getChannelName() + " :[BROADCAST] " + message + "\n");
	}
    }

    /**
     * Adds a channel connect
     *
     * @param sourceChannel channel to forward from
     * @param destinationNetwork network (ServerConnection) to forward to
     * @param destinationChannel channel on destination network to forward to
     * @param null if successful, otherwise error message
     */
    public String addChannelConnect(String sourceChannel,ServerConnection destinationNetwork,
				    String destinationChannel)
    {
	if( !channels.containsKey(sourceChannel) )
	    return new String("invalid source channel");

	if( !destinationNetwork.getChannels().containsKey(destinationChannel) )
	    return new String("invalid destination channel");

	channelConnects.add(new ChannelConnect(sourceChannel,destinationNetwork,destinationChannel));

	return null;
    }

    /**
     * Removes all channel connects
     *
     */
    public void removeChannelConnects()
    {
	channelConnects.clear();
    }

    /**
     * Finds a User by exact (in-casesensitive) name.
     * 
     * @param name name of user as specified in config file / bots user record.
     * @return User object, or null if not found
     */    
    public User findUser(String name)
    {
	/* go through all users */
	for( int i = 0; i < users.size(); i++ ) {
	    User user = (User)users.elementAt(i);
	    if( user.getName().equalsIgnoreCase(name) ) {
		return user;
	    }
	}
	return null;
    }

    /**
     * Finds a User by host. user must be logged in.
     *
     * @param host host 
     * @return User object, or null if not found or not logged in.
     * @see irssibot.user.User#isLoggedIn()
     */
    public User findUser(Host host)
    {
	Vector hosts = null;

	/* go through all users */
	for( int i = 0; i < users.size(); i++ ) {
	    User user = (User)users.elementAt(i);
	    if( user.hasMatchingHost(host) && user.isLoggedIn() ) {
		if( user.isDynamic() ) {
		    if( host.equals(user.getLoginHost()) ) return user;
		} else {
		    return user;
		}
	    }
	}
	return null;
    }

    /**
     * finds a user matching given host in bot's user record. need not
     * be logged in. the returned User should not be used in any action
     * requiring login, but merely in inspection.
     *
     * @param host host 
     * @return User
     * @see irssibot.user.User#isLoggedIn
     */
    private User findMatchingUser(Host host)
    {
	Vector hosts = null;

	/* go through all users */
	for( int i = 0; i < users.size(); i++ ) {
	    User user = (User)users.elementAt(i);
	    hosts = user.getHosts();
	    /* go through all user's hosts */
	    for( int j = 0; j < hosts.size(); j++ ) {
		Host userHost = (Host)hosts.elementAt(j);
		if( host.matches(userHost) ) {
		    return user;
		}
	    }
	}
	return null;
    }

    /**
     * finds a Channel matching given name or null if not found
     *
     * @param name name of channel
     * @return Channel/null
     */
    public Channel findChannel(String name)
    {
	if( name != null )
	    return (Channel)channels.get(name.toLowerCase());
	else 
	    return null;
    }

    /**
     * attempts to connect to an irc server
     *
     * @param server address in form of ip:port:password
     *               if no port/password given, using defaults 6667/null.
     */
    private void connect(String addr)
    {
	boolean isOk = true;
	String ip = null;
	String pass = null;
	int port = 6667;

	/* mark instance as not connected */
	currentServer = null;
	statusString = "not connected";
	connectionReady = false;

	String addrData[] = StringUtil.separate(addr,':');

	if( addrData.length < 1 ) {
	    putlog("Bad server address");
	    isOk = false;
	} else {
	    if( addrData.length > 2 ) pass = addrData[2];
	    if( addrData.length > 1 ) {
		try {
		    port = Integer.parseInt(addrData[1]);
		} catch( NumberFormatException ne ) {
		    /* bad port value; die */
		    putlog("connect(): invalid port. NumberFormatException: "+ne.getMessage());
		    isOk = false;
		    continueConnecting = false;
		}
	    }
	    ip = addrData[0];
	}

	/* if server address is well-formed, attempt to connect */
	if( isOk ) {
	    /* connect to irc server */
	    currentServer = addr;
	    statusString = "connecting to "+addr;
	    putlog(statusString);
	    try {
		socket = new Socket(ip,port);
		serverIn = new BufferedInputStream(socket.getInputStream());
		serverInReader = new BufferedReader(new InputStreamReader(serverIn));
		serverOut = new BufferedOutputStream(socket.getOutputStream());
		socket.setSoTimeout(500);
	    } catch( IOException ie ) {
		putlog(ie.getClass().getName()+": "+ie.getMessage());
		
		isOk = false;
		currentServer = null;
	    }
	    
	    if( isOk ) {
		putlog("connected, sending client data to server..");

		/* send data about bot/client */
		if( (pass != null) && !pass.equals("") ) {
		    write("PASS "+pass+"\n");
		}
		if( !useAltNick ) {
		    putlog("using nick \""+instanceData.getBotNick()+"\"");
		    write("NICK "+instanceData.getBotNick()+"\n");
		} else {
		    putlog("using alt nick "+instanceData.getBotAltNick()+"\"");
		    write("NICK "+instanceData.getBotAltNick()+"\n"); 
		}

		//##TODO## fix these damn 'irssibot' and 'hut' :-)
		write("USER irssibot hut "+ip+" :"+instanceData.getRealName()+"\n");

		connectionAlive = true;
		currentServer = addr;
	    } 
	} 
    } 

    /**
     * Sets the ServerConnection's status string.
     *
     * @param status New status string
     */
    private void setStatusString(String statusString) 
    {
	this.statusString = statusString;
    }

    /**
     * Handle RPL_USERHOST reply message: 
     *
     * <ul><li>Finish up connecting
     *     <li>Parse bot host from message
     *     <li>Join channels
     * </ul>
     *
     * @param message the RPL_USERHOST message
     */
    private void doUserHost(IrcMessage message)
    {
	setStatusString(statusString = "connected to "+currentServer);

	/* parse bot host */
	int equalsIndex = message.trailing.indexOf('=');
	String parsedNick = message.trailing.substring(0,equalsIndex);
	
	/* remove possible preceding '*' */
	if( parsedNick.endsWith("*") ) {
	    parsedNick = parsedNick.substring(0,(parsedNick.length() - 1));
	}
	String parsedHost = message.trailing.substring(equalsIndex + 2);
	
	botHost = new Host(parsedNick+"!"+parsedHost);

	/* ensure we get actual hostname and not IP (EFNet hack) */
	try {
	    InetAddress address = InetAddress.getByName(botHost.getHost());
	    botHost.setHost(address.getHostName());
	} catch ( UnknownHostException e ) {
	    putlog("doUserHost(): got UnknownHostException: " + e.getMessage());
	}
	    
	putlog("bot host from RPL_USERHOST: " + botHost.toString());

	/* join channels */
	Enumeration enum = instanceData.getChannels().elements();
	while( enum.hasMoreElements() ) {
	    Channel channel = (Channel)enum.nextElement();
	    channel.setServerConnection(this);
	    channel.setServerChannelModes(serverChannelModes);

	    putlog("joining channel "+channel.getChannelName()+"..");
	    if( channel.getChannelKey() != null ) {
		write("JOIN " + channel.getChannelName() + " " + channel.getChannelKey() + "\n");
	    } else {
		write("JOIN " + channel.getChannelName() + "\n");
	    }
	}
	connectionReady = true;	
    }

    /**
     * Returns XML representation of this server connection to be written to configuration file.
     *
     * @return XML 
     */
    public String getXML()
    {
	String ret = "";

	ret += "  <server-instance network=\""+instanceData.getNetwork()+"\">\n";
	ret += "    <bot-info nick=\""+instanceData.getBotNick()+"\" altnick=\""+instanceData.getBotAltNick()+"\" ";
	ret += "realname=\""+instanceData.getRealName()+"\" />\n";
	ret += "    <user-file path=\""+instanceData.getUserFilePath()+"\" />\n";
	ret += "    <server-list>\n";
	
	Vector v = instanceData.getServerList();
	for( int i = 0; i < v.size(); i++ ) {
	    String addr = (String)v.elementAt(i);
	    ret += "      <address>"+addr+"</address>\n";
	}

	ret += "    </server-list>\n";
	ret += "    <channel-list>\n";
	
	Enumeration keys = channels.keys();
	while( keys.hasMoreElements() ) {
	    String key = (String)keys.nextElement();
	    Channel channel = (Channel)channels.get(key);
	    ret += "      <channel name=\""+channel.getChannelName()+"\" key=\"";
	    if( channel.getChannelKey() != null ) {
		ret += channel.getChannelKey();
	    }
	    String fmodes = "";
	    if( channel.getPosModes() != null ) {
		fmodes += "+" + channel.getPosModes();
	    }
	    if( channel.getNegModes() != null ) {
		fmodes += "-" + channel.getNegModes();
	    }
	    ret += "\" forcedmodes=\"" + fmodes + "\" />\n";
	}
	ret += "    </channel-list>\n";
	ret += "  </server-instance>\n";

	return ret;
    }

    /**
     * run()
     * 
     */
    public void run()
    {
	String msg = null;
	int msgLen = 0;

	currentServerIndex = 0;

	while( continueConnecting ) {
	    connectionAlive = false;

	    while( !connectionAlive ) {
		String address = (String)instanceData.getServerList().elementAt(currentServerIndex++);
		if( currentServerIndex >= instanceData.getServerList().size() ) {
		    currentServerIndex = 0;
		}
		connect(address);
	    }

	    /* init timer variables */
	    nickRegainTime = new Date().getTime();
	    channelRejoinTime = new Date().getTime();

	    /* the main loop */
	    while( continueConnecting && connectionAlive ) {
		/* read server messages */
		try {
		    msg = serverInReader.readLine();
		    if( msg != null ) {
			System.out.println(instanceData.getNetwork()+": "+msg);
			IrcMessage message = new IrcMessage(msg);
			
			processServerMessage(message);
		    } else {
			/* connection to server closed (QUIT) */
			connectionAlive = false;
			continueConnecting = false;
		    }
		} catch( IOException ie ) {}
		
		

		/* check user file write -timer event */
		if( (autoSaveTimer == null) || !autoSaveTimer.isAlive() ) {
		    /* write user file && prompt modulehandler to save module states */
		    if( autoSaveTimer != null ) {
			writeUserFile();
			core.saveModuleStates(false);
		    }
		    /* (re)set timer */
		    autoSaveTimer = new Timer(300);
		    autoSaveTimer.start();
		}	

		/* handle misc timed events */
		if( connectionReady && continueConnecting ) {
		    long now = new Date().getTime();

		    /* every 15 seconds, attempt to regain bot nick */
		    if( (now - nickRegainTime) >= 15000 ) {
			if( !instanceData.getBotNick().equalsIgnoreCase(botHost.getNick()) ) {
			    write("NICK "+instanceData.getBotNick()+"\n");
			}
			nickRegainTime = now;
		    }
		    
		    /* every 15 seconds, attempt to rejoin channels if not on them */
		    if( (now - channelRejoinTime) >= 15000 ) {
			Enumeration elements = channels.elements();
			while( elements.hasMoreElements() ) {
			    Channel channel = (Channel)elements.nextElement();
			    if( !channel.isJoined() ) {
				if( channel.getChannelKey() == null ) {
				    putlog("run(): attempting to rejoin channel "+channel.getChannelName());
				    write("JOIN "+channel.getChannelName()+"\n");
				} else {
				    putlog("run(): attempting to rejoin channel "+channel.getChannelName() +
					   " with key "+channel.getChannelKey());
				    write("JOIN "+channel.getChannelName()+" "+channel.getChannelKey()+"\n");
				}
			    }
			}
			channelRejoinTime = now;
		    }
		}
	    }
	    
	    /* thread dying - close socket and clean up */
	    try { 
		socket.close(); 
		serverIn = null; 
		serverOut = null; 
	    } catch( IOException ie ) { 
		putlog("error closing socket: "+ie.getMessage());
	    }	
	}

	if( errorMsg != null )
	    putlog("ERROR: "+errorMsg);
	putlog("Thread \""+getName()+"\"  exiting..");

	currentServer = null;
	statusString = "not connected";

	if( autoSaveTimer != null ) {
	    autoSaveTimer.kill();
	}

	modeQueue.kill();
    }

    /**
     * process message from server
     * @param message message to be processed 
     */
    private void processServerMessage(IrcMessage message)
    { 
	Channel channel = null;

	if( message.command.equals("PRIVMSG") ) {
	    /* PRIVMSG */
	    processPrivmsg(message);

	    /* also send the message to modules */
	    //    core.sendToModules(message,this);
	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	} else if( message.command.equals("ERROR") ) {
	    /* ERROR: close connection */
	    connectionAlive = false;
	    continueConnecting = true;
	    errorMsg = message.trailing;
	} else if( message.command.equals("PING") ) {
	    /* PING from server */
	    write("PONG "+message.trailing+"\n");
	} else if( message.command.equals("MODE") ) {
	    /* MODE */
	    if( message.arguments[0] != null ) {
		channel = findChannel(message.arguments[0]);
		
		if( channel != null ) {
		    /* handle channel modes */
		    channel.onMode(message);
		}
	    }

	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	} else if( message.command.equals("JOIN") ) {
	    /* JOIN */
	    String chanName = null;
	    if( message.trailing != null )
		chanName = message.trailing;
	    else 
		chanName = message.arguments[0].toLowerCase();

	    channel = findChannel(chanName);

	    /* if channel not found, create new one */
	    if( channel == null ) {
		channel = new Channel(chanName,null,null,this);
		channels.put(chanName,channel);
	    }
	    /* Channel.onJoin() handles sending to modules */
	    channel.onJoin(message);
	} else if( message.command.equals(Irc.RPL_CHANNELMODEIS) ) {
	    /* channel MODE reply. send to correct Channel */
	    channel = findChannel(message.arguments[1]);

	    if( channel != null ) {
		channel.onChannelModeIs(message);
	    } 
	} else if( message.command.equals(Irc.RPL_WHOREPLY) ) {
	    /* WHO reply. send to correct Channel */
	    channel = findChannel(message.arguments[1]);

	    if( channel != null )
		channel.processWhoReply(message);
	    else 
		processWhoReply(message);
	} else if( message.command.equals("PART") ) {
	    /* PART */
	    channel = findChannel(message.arguments[0]);
	    if( channel != null ) {
		channel.onPart(message);

		/* send the message to modules */
		core.getModuleHandler().forwardMessage(message,this);
		/* also send the message to modules */
//		core.sendToModules(message,this);
	    } else
		putlog("NULL channel: "+message.arguments[0]);

	} else if( message.command.equals("TOPIC") ) {
	    /* TOPIC change */
	    channel = findChannel(message.arguments[0]);
	    channel.onTopic(message);

	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	    /* also send the message to modules */
	//    core.sendToModules(message,this);

	} else if( message.command.equals("KICK") ) {
	    /* KICK */
	    channel = findChannel(message.arguments[0]);
	    channel.onKick(message);

	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	    /* also send the message to modules */
//	    core.sendToModules(message,this);
	} else if( message.command.equals("QUIT") ) {
	    /* QUIT */
	    processQuit(message);

	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	    /* also send the message to modules */
//	    core.sendToModules(message,this);
	} else if( message.command.equals("NICK") ) {
	    /* NICK change */
	    processNick(message);

	    /* send the message to modules */
	    core.getModuleHandler().forwardMessage(message,this);
	    /* also send the message to modules */
 	//    core.sendToModules(message,this);
	} else if( message.command.equals(Irc.RPL_TOPIC) ) {
	    /* TOPIC msg */
	    channel = findChannel(message.arguments[1]);
	    channel.onTopicMsg(message);
	} else if( message.command.equals(Irc.RPL_BANLIST) ) {
	    /* RPL_BANLIST */
	    channel = findChannel(message.arguments[1]);
	    channel.onBanListMsg(message);
	} else if( message.command.equals(Irc.RPL_ENDOFBANLIST) ) {
	    /* RPL_ENDOFBANLIST */
	    channel = findChannel(message.arguments[1]);
	    channel.onEndOfBanListMsg(message);
	} else if( message.command.equals(Irc.RPL_ENDOFWHO) ) {
	    /* RPL_ENDOFWHO */
	    channel = findChannel(message.arguments[1]);
	    channel.onEndOfWhoMsg(message);
	} else if( message.command.equals(Irc.ERR_NICKNAMEINUSE)    || 
		   message.command.equals(Irc.ERR_ERRONEUSNICKNAME) || 
		   message.command.equals("437") ) {
	    /* toggle the use of altnick */
	    useAltNick = !useAltNick;
	} else if( message.command.equals("004") ) {
	    /* 
	       parse allowed user/channel modes out of 004:
	       :irc2.fi.quakenet.eu.org 004 TuneX server u2.10 dioswkgX biklmnopstv
	    */
	    serverUserModes = message.arguments[3];
	    serverChannelModes = "";

	    /* filter out modes o,b,v,k */
	    for( int i = 0; i < message.arguments[4].length(); i++ ) {
		if( message.arguments[4].charAt(i) != 'o' &&
		    message.arguments[4].charAt(i) != 'v' &&
		    message.arguments[4].charAt(i) != 'k' &&
		    message.arguments[4].charAt(i) != 'b' ) {
		    serverChannelModes += message.arguments[4].charAt(i);
		}
	    }
	} else if( message.command.equals(Irc.RPL_WELCOME) ) {
	    /* server WELCOME; send USERHOST <botnick> */
	    putlog("sending 'USERHOST "+message.arguments[0]+"'");
	    write("USERHOST "+message.arguments[0]+"\n");
	} else if( message.command.equals(Irc.RPL_USERHOST) ) {
	    doUserHost(message);
	}
    }

    /**
     * pricess PRIVMSGs to either channel or to self (MSG)
     *  IRCNet: :dreami!^matti@777-team.org PRIVMSG ankybot :heissan botti
     *  IRCNet: :dreami!^matti@777-team.org PRIVMSG #777-team :hm... terve vaan
     * @param message message
     */
    private void processPrivmsg(IrcMessage message)
    {
	String source = null;
	boolean isCoreCall = false;
	Host host = new Host(message.prefix);

	if( message.trailing.equals("VERSION") ) {
	    /* CTCP VERSION */
	    String nick = message.prefix.substring(0,message.prefix.indexOf('!'));
	    write("NOTICE "+nick+" :VERSION "+Core.botVersion+"\n");
	} else {
	    /* look for source (message to channel or to bot) */
	    if( message.arguments[0].equals(botHost.getNick()) ) {
		source = host.getNick();
		if( message.trailing.startsWith("core->") ) {
		    isCoreCall = true;
		}
	    } else {
		source = message.arguments[0];
		if( message.trailing.startsWith("!core->") ) {
		    /* alter message so that preceding ! is removed */
		    message.trailing = message.trailing.substring(1);
		    isCoreCall = true;
		}
	    }
	    
	    /* look for core calls */
	    if( isCoreCall ) {
		/* has to be global admin to call core */
		User user = findUser(host);
		if( (user != null) && user.isGlobalAdmin() ) {
		    String ret = core.processCoreCall(message,this,source);
		    if( ret != null )
			write("PRIVMSG "+source+" :core replied: "+ret+"\n");
		}
	    } else {
		/* a non-core call */
		if( message.arguments[0].equals(botHost.getNick()) ) {
		    /* handle special commands login, logout */
		    String args[] = StringUtil.separate(message.trailing,' ');
		    if( (args != null) && (args.length >= 2) ) {
			if( args[0].equals("login") ) {
			    User user = findMatchingUser(host);
			    if( user != null ) {
				/* look for password */
				if( args[1].equals(user.getPassword()) ) {
				    user.doLogin(host);
				    write("PRIVMSG "+source+" :Logged in as "+user.getLoginHost().toString() +
					  ". Login will expire in 60 minutes.\n");
				    doMaintain();
				    putlog("login ok for "+user.getName());
				} else {
				    putlog("login failed for "+user.getName());
				}
			    }
			} else if( args[0].equals("logout") ) {
			    User user = findMatchingUser(host);
			    if( user != null ) {
				/* look for password */
				if( args[1].equals(user.getPassword()) ) {
				    user.doLogout();
				    write("PRIVMSG "+source+" :Logged out.\n");
				} else
				    putlog("logout failed: "+args[1]+" "+user.getPassword());
			    }
			}
		    }
		} else {
		    /* forward to all channel connects */
		    for( int i = 0; i < channelConnects.size(); i++ ) {
			ChannelConnect connect = (ChannelConnect)channelConnects.elementAt(i);
			if( message.arguments[0].equals(connect.sourceChannel) ) {
			    Channel chan = (Channel)connect.destinationNetwork.getChannels().get(connect.destinationChannel);
			    if( chan.isJoined() ) {
				String msg = "<"+host.getNick()+"> "+message.trailing;
				connect.destinationNetwork.write("PRIVMSG "+connect.destinationChannel+" :"+msg+"\n");
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * sends a NICK message to all channels
     * @param message message to send
     */
    private void processNick(IrcMessage message)
    {
	Host host = new Host(message.prefix);
	
	/* see if self changing nick */
	if( botHost.getNick().equals(host.getNick()) ) {
	    putlog("changed nick to "+message.trailing);
	    botHost.setNick(message.trailing);
	} 

	Enumeration enum = channels.elements();
	while( enum.hasMoreElements() ) {
	    Channel channel = (Channel)enum.nextElement();
	    /* if nick is on channel, invoke onNick() */
	    if( channel.findNick(host.getNick()) != null ) {
		channel.onNick(message);
	    }
	}
    }

    /**
     * sends a QUIT message to all channels
     * @param message message to send
     */
    private void processQuit(IrcMessage message)
    {
	Enumeration enum = channels.elements();
	while( enum.hasMoreElements() ) {
	    Channel channel = (Channel)enum.nextElement();
	    if( channel.isJoined() ) {
		channel.onQuit(message);
	    }
	}
    }

    /**
     * Adds a channel to bots channels hash and makes bot join the channel.
     *
     * @param channelName name of channel
     * @return true if successful, false if channel already define for this server connection
     */
    public boolean addChannel(String channelName,String channelKey)
    {
	boolean ret = false;
	
	/* check channel name */
	if( channelName.charAt(0) == '#' ) {
	    if( !channels.containsKey(channelName.toLowerCase()) ) {
		Channel channel = new Channel(channelName,channelKey,null,this);
		if( channelKey != null ) 
		    write("JOIN "+channel.getChannelName()+" "+channel.getChannelKey()+"\n");
		else
		    write("JOIN "+channel.getChannelName()+"\n");
		
		channels.put(channel.getChannelName(),channel);
		ret = true;
	    }
	}
	return ret;
    }

    /**
     * Removes a named channel from bots channels hash and makes the
     * bot leave the channel.
     *
     * @param channel Channel object of channel to remove
     * @return true on success, false if no such channel was found
     */
    public boolean delChannel(Channel channel)
    {
	boolean ret = false;
	
	if( channels.containsKey(channel.getChannelName()) ) {
	    write("PART "+channel.getChannelName()+"\n");;
	    channels.remove(channel.getChannelName());
	    ret = true;
	}
	return ret;
    }

    /**
     * process command coming from console input
     * @param command raw unformatted command string from console
     */
    public void processConsoleCommand(String command)
    {
	if( command.startsWith("chaninfo") ) {
	    Enumeration enum = channels.elements();
	    while( enum.hasMoreElements() ) {
		Channel chan = (Channel)enum.nextElement();
		chan.showInfo();
	    }
	} else {
	    write(command+"\n");
	}
    }

    /**
     * process WHOIS reply 
     * <i>not in user for the moment</i>
     * @param message WHOIS message
     */
    private void processWhoisReply(IrcMessage message)
    {
	putlog("processWhoisReply() - no action");
    }

    /**
     * process WHO reply 
     * <i>not in user for the moment</i>
     * @param message WHO message
     */
    private void processWhoReply(IrcMessage message)
    {
	putlog("processWhoReply() - no action");
    }

    /**
     * callback registration function for WHOIS replies
     * @param nick target of callback
     */
    public void registerWhoisTargetNick(Nick nick)
    {
	whoisTargetNick = nick;
    }

    /**
     * QUIT from IRC server and finish execution.
     * @param quitMessage message to send to IRC server as quit message
     */
    public void quit(String quitMessage)
    {
	write("QUIT :"+quitMessage+"\n");
	continueConnecting = false;
	connectionAlive = false;
    }

    /**
     * Calls doMaintain() for all channels
     *
     */
    public void doMaintain() 
    {
	Enumeration enum = instanceData.getChannels().elements();
	while( enum.hasMoreElements() ) {
	    Channel channel = (Channel)enum.nextElement();
	    channel.doMaintain();
	}
    }

    /**
     * Notifies ServerConnection that its user data has
     * been changed and it should rewrite the user file soon.
     *
     */
    public void notifyUserDataChanged()
    {
	userDataChanged = true;
	doMaintain();
    }

    /**
     * writes user file to disk
     */
    public void writeUserFile()
    {
	String ret = null;
	boolean isOk = true;
	FileWriter fileWriter = null;

	if( userDataChanged ) {
	    putlog("writing userfile..");

	    ret = "<users-file>\n";
	    for( int i = 0; i < users.size(); i++ ) {
		User user = (User)users.elementAt(i);
		ret += user.getXML()+"\n";
	    }
	    ret += "</users-file>\n";
	    
	    File            destFile;
	    FileWriter      destFileWriter;
	    
	    destFile = new File(instanceData.getUserFilePath());
	
	    try { 
		destFileWriter = new FileWriter(destFile);
		destFileWriter.write(ret,0,(int)ret.length());
		destFileWriter.flush();
		destFileWriter.close();
	    } catch( IOException e ) {
		e.printStackTrace();
		isOk = false;
	    }
	    userDataChanged = false;
	}
    }	

    /**
     * Write bytes/string to server. This function will be replaced by a bursting
     * one to avoid excess flood.
     *
     * @param str string to write
     */
    synchronized public void write(String str)
    {
	if( (str != null) && (serverOut != null) ) {
	    try {
		serverOut.write(str.getBytes());
		serverOut.flush();
	    } catch( IOException e ) {
		e.printStackTrace();
	    }
	} else {
	    // handle error
	}
    } 

    private void putlog(String msg)
    {
	String logMsg = getClass().getName()+" ["+getName()+"]: "+msg+"\n";
	System.out.print(logMsg);
    }
}








