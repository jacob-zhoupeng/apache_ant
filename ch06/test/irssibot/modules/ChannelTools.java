/*
 * $Id: ChannelTools.java,v 1.1.1.1 2001/03/26 10:57:42 matti Exp $
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
package irssibot.modules;

import irssibot.core.*;
import irssibot.user.*;
import irssibot.util.StringUtil;

import java.util.*;

/**
 * This class contains channel variables such as ban times etc
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.1.1.1 $
 */
class ChanVars 
{
    /**
     * Ban time for bk in minutes
     */
    private int banTime;
    /**
     * Ban time for auto-rejoin ban in minutes (NOT YET IMPLEMENTED)
     */
    private int rejoinBanTime;

    public ChanVars(int banTime,int rejoinBanTime)
    {
	this.banTime = banTime;
	this.rejoinBanTime = rejoinBanTime;
    }

    /**
     * Default constructor
     */
    public ChanVars()
    {
	this(30,5);
    }

    /**
     * Parses a value string. The values are space-separated.
     *
     * @param varstr String containing the values
     */
    static ChanVars parse(String varstr)
    {
	ChanVars vars = null;
	String parts[] = StringUtil.separate(varstr,' ');
	if( (parts != null) && (parts.length == 2) ) {
	    int banTime = 0;
	    int rejoinBanTime = 0;
	    boolean ok = true;

	    try {
		banTime = Integer.parseInt(parts[0]);
		rejoinBanTime = Integer.parseInt(parts[1]);
	    } catch( NumberFormatException e ) {
		ok = false;
	    }

	    if( ok ) {
		vars = new ChanVars(banTime,rejoinBanTime);
	    }
	}
	return vars;
    }

    public int getBanTime() { return banTime; }
    public int getRejoinBanTime() { return rejoinBanTime; }
}    

/**
 * This class handles removing of bans after a certain period of time.
 *
 * @author Matti Dahlbom
 */
class BanHandler extends irssibot.core.Timer
{
    private String banMask = null;
    private ServerConnection connection = null;
    private String channelName = null;

    public BanHandler(int banTime,String banMask,ServerConnection connection,String channelName) 
    {
	/* to seconds */
	super(banTime * 60);
	this.banMask = banMask;
	this.connection = connection;
	this.channelName = channelName;
	start();
    }

    public void run()
    {
	doRun();
	connection.write("MODE "+channelName+" -b "+banMask+"\n");
    }
}

/**
 * This module has basic functionality for maintaining/protecting 
 * IRC channels from privmsg/join/nick flood, clone attacks,
 * massdeops and such.
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.1.1.1 $
 */
public class ChannelTools extends AbstractModule
{
    /* statics */
    private static String moduleInfo = "Channel Tools 1.0.1 for IrssiBot";

    /**
     * This Hashtable contains Vector objects containing banmasks as String.
     * The keys are of form "network-#channel" for each channel.
     */
    private Hashtable permbanList = null;
    /**
     * This Hashtable contains a ChanVars object for each channel.
     * The keys are of form "network-#channel" for each channel.
     */
    private Hashtable chanvars = null;
    private boolean changed = false;

    /* per-request temp data */
    private Host host = null;
    private String source = null;
    private ServerConnection caller = null;

    private ChanVars getVars(String channelName)
    {
	String key = caller.getInstanceData().getNetwork()+"-"+channelName;
	return (ChanVars)chanvars.get(key);
    }

    public Properties getState()
    {
	Properties props = null;
	String key = null;
	Vector v = null;
	String mask = null;

	if( changed ) {
	    props = new Properties();

	    /* add permbans to state */
	    Enumeration keys = permbanList.keys();
	    while( keys.hasMoreElements() ) {
		key = (String)keys.nextElement();
		v = (Vector)permbanList.get(key);

		for( int i = 0; i < v.size(); i++ ) {
		    mask = (String)v.elementAt(i);
		    props.setProperty(key+"-permban"+i,mask);
		}
	    }

	    /* add chanvars to state */
	    keys = chanvars.keys();
	    while( keys.hasMoreElements() ) {
		key = (String)keys.nextElement();
		ChanVars vars = (ChanVars)chanvars.get(key);
		String value = vars.getBanTime()+" "+vars.getRejoinBanTime();
		props.setProperty(key+"-chanvars",value);
	    }
	    changed = false;
	} 
	
	return props;
    }

    private void loadInitialState(Properties state,Core core) 
    {
	Vector instances = core.getServerInstances();	
	permbanList = new Hashtable();

	/* go through the server connections */
	for( int i = 0; i < instances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)instances.elementAt(i);
	    Enumeration channels = connection.getChannels().elements();

	    /* go through channels for this server connection */
	    while( channels.hasMoreElements() ) {
		Channel channel = (Channel)channels.nextElement();
		String key = connection.getInstanceData().getNetwork()+"-"+channel.getChannelName();

		Vector v = new Vector();

		/* go through list for bans matching current connection/channel */
		Enumeration bans = state.keys();
		while( bans.hasMoreElements() ) {
		    String maskKey = (String)bans.nextElement();
		    if( maskKey.startsWith(key+"-permban") ) {
			v.add(state.getProperty(maskKey));
		    }
		}

		/* add permbans to hash */
		if( v.size() > 0 ) {
		    permbanList.put(key,v);
		}

		/* handle chanvars */
		String varstr = state.getProperty(key+"-chanvars");
		ChanVars vars = ChanVars.parse(varstr);
		if( vars != null ) {
		    chanvars.put(key,vars);
		}
	    }
	}
    }

    /**
     * Returns a module info string 
     *
     * @return module info string
     */
    public String getModuleInfo()
    {
	return moduleInfo;
    }

    public boolean onLoad(Properties state,Core core) 
    {
	permbanList = new Hashtable();
	chanvars = new Hashtable();

	if( state != null ) {
	    loadInitialState(state,core);
	}
	return true;
    }

    public void onUnload() 
    {
	permbanList = null;
    }

    /**
     * Kicks a user
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandK(Host host,User invoker,String args[],Channel channel)
    {
	/* dont bk self */
	if( channel.isOp() && (args != null) && (args.length >= 1)) {
	    Nick nick = channel.findNick(args[0]);
	    if( (nick != null) && !nick.getHost().equals(caller.getHost()) ) {
		User user = caller.findUser(nick.getHost());
		/* dont kick channel admins/ops */
		if( (user == null) || !(user.isGlobalAdmin() || 
					user.isChanAdmin(channel.getChannelName()) || 
					user.isOp(channel.getChannelName())) ) {
		    String kickMsg = StringUtil.join(args,1);
		    if( kickMsg == null ) {
			caller.write("KICK "+channel.getChannelName()+" "+nick.getHost().getNick()+"\n");
		    } else {
			caller.write("KICK "+channel.getChannelName()+" "+nick.getHost().getNick()+" :"+kickMsg+"\n");
		    }
		}
	    }
	}
    }

    /**
     * Bans & kicks a user
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandBK(Host host,User invoker,String args[],Channel channel)
    {
	/* dont bk self */
	if( channel.isOp() && (args != null) && (args.length >= 1)) {
	    if( invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) || 
		invoker.isOp(channel.getChannelName()) ) {

		/* find the Nick to be kicked */
		Nick nick = channel.findNick(args[0]);
		if( (nick != null) && !nick.getHost().equals(caller.getHost()) ) {
		    User user = caller.findUser(nick.getHost());
		    /* dont kick channel admins/ops */
		    if( (user == null) || !(user.isGlobalAdmin() || 
					    user.isChanAdmin(channel.getChannelName()) || 
					    user.isOp(channel.getChannelName())) ) {
			String kickMsg = StringUtil.join(args,1);
			String banMask = "*!"+nick.getHost().getIdent()+"@"+nick.getHost().getHost();
			
			caller.write("MODE "+channel.getChannelName()+" +b "+banMask+"\n");
			if( kickMsg == null ) {
			    caller.write("KICK "+channel.getChannelName()+" "+nick.getHost().getNick()+"\n");
			} else {
			    caller.write("KICK "+channel.getChannelName()+" "+nick.getHost().getNick()+" :"+kickMsg+"\n");
			}
			new BanHandler(getVars(channel.getChannelName()).getBanTime(),banMask,caller,channel.getChannelName());
		    }
		}
	    }
	}
    }

    /**
     * Processes add-commands for this module
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandAdd(Host host,User invoker,String args[],Channel channel)
    {
	/* all commands require at least channel op */
	if( (args != null) && (args.length > 0) && 
	    (invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) ||
	     invoker.isOp(channel.getChannelName())) ) {
	    /* select subcommand */
	    if( args[0].equals("permban") && (args.length >= 2) ) {
		String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		Vector v = (Vector)permbanList.get(key);
		if( v == null ) { v = new Vector(); }
		
		String banMask = args[1].trim().toLowerCase();

		/* if nick part missing, add manually */
		if( banMask.indexOf('!') == -1 ) {
		    banMask = "*!"+banMask;
		}

		if( v.contains(banMask) ) {
		    write("Host "+banMask+" is already on my permban list for "+channel.getChannelName()+".");
		} else {
		    v.add(banMask);
		    permbanList.put(key,v);
		    changed = true;

		    caller.write("MODE "+channel.getChannelName()+" +b "+banMask+"\n");
		    write("Added new permban "+banMask+" to "+channel.getChannelName()+".");
		}
	    } else if( args[0].equals("channel") && (args.length >= 2) ) {
		String channelName = args[1];
		String channelKey = null;

		if( invoker.isGlobalAdmin() ) {
		    if( args.length >= 3 ) {
			channelKey = args[2];
		    }
		    if( caller.addChannel(channelName,channelKey) ) {
			write("Added channel "+channelName.toLowerCase()+".");
		    } else {
			write("Channel "+channelName.toLowerCase()+" already exists.");
		    }
		}
	    }
	}
    }

    /**
     * Processes del-commands for this module
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandDel(Host host,User invoker,String args[],Channel channel)
    {
	/* all commands require at least channel op */
	if( (args != null) && (args.length > 0) && 
	    (invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) ||
	     invoker.isOp(channel.getChannelName())) ) {
	    /* select subcommand */
	    if( args[0].equals("permban") && (args.length >= 2) ) {
		String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		Vector v = (Vector)permbanList.get(key);

		if( v == null ) {
		    v = new Vector();
		}
		
		String banMask = args[1].trim().toLowerCase();
		if( v.remove(banMask) ) {
		    permbanList.put(key,v);
		    changed = true;

		    caller.write("MODE "+channel.getChannelName()+" -b "+banMask+"\n");
		    write("Removed permban "+banMask+" from "+channel.getChannelName()+".");
		}
	    } else if( args[0].equals("channel") && (args.length >= 2) ) {
		if( invoker.isGlobalAdmin() ) {
		    Channel chan = caller.findChannel(args[1]);
		    if( chan != null ) {
			if( caller.delChannel(chan) ) {
			    write("Channel "+chan.getChannelName()+" removed.");
			}
		    }
		}
	    } else if( args[0].equals("chankey") ) {
		channel.setChannelKey(null);
		write("Removed channel key from "+channel.getChannelName()+".");
	    }
	}
    }
 
    /**
     * Processes list-commands for this module
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandList(Host host,User invoker,String args[],Channel channel)
    {
	/* all commands require at least channel op */
	if( (args != null) && (args.length > 0) && 
	    (invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) ||
	     invoker.isOp(channel.getChannelName())) ) {
	    /* select subcommand */
	    if( args[0].equals("permbans") ) {
		String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		Vector v = (Vector)permbanList.get(key);
		if( v != null ) {
		    for( int i = 0; i < v.size(); i++ ) {
			String banMask = (String)v.elementAt(i);
			write(channel.getChannelName()+" permban: "+banMask);
		    }
		}
	    } else if( args[0].equals("chanvars") ) {
		String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();		
		ChanVars vars = (ChanVars)chanvars.get(key);
		if( vars != null ) {
		    write(channel.getChannelName()+" chanvars: "+vars.getBanTime()+" "+vars.getRejoinBanTime());
		}
	    } else if( args[0].equals("fmodes") ) {
		String ret = "Forced channel modes for " + channel.getChannelName() + ": ";
		if( channel.getPosModes() != null ) {
		    ret += "+" + channel.getPosModes();
		}
		if( channel.getNegModes() != null ) {
		    ret += "-" + channel.getNegModes();
		}
		write(ret);
	    } else if( args[0].equals("chankey") ) {
		if( channel.getChannelKey() == null ) 
		    write("No channel key set.");
		else
		    write("Channel key is: '"+channel.getChannelKey()+"'.");
	    }
	}
    }

    /**
     * Processes edit-commands for this module
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandEdit(Host host,User invoker,String args[],Channel channel)
    {
	/* all commands require at least channel op */
	if( (args != null) && (args.length > 0) && 
	    (invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) ||
	     invoker.isOp(channel.getChannelName())) ) {
	    /* select subcommand */
	    if( args[0].equals("chanvars") ) {
		String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		ChanVars vars = ChanVars.parse(StringUtil.join(args,1));
		if( vars != null ) {
		    chanvars.put(key,vars);
		    write("Edited chanvars for "+channel.getChannelName()+".");
		    changed = true;
		}
	    } else if( args[0].equals("chankey") ) {
		if( args.length == 2 && args[1] != null ) {
		    channel.setChannelKey(args[1]);
		    write("Set channel key for "+channel.getChannelName()+" to '"+args[1]+"'.");
		}
	    } else if( args[0].equals("fmodes") ) {
		if( invoker.isChanAdmin(channel.getChannelName()) || invoker.isGlobalAdmin() ) {
		    if( args.length > 1 ) {
			channel.parseForcedModes(args[1]);

			String ret = "Forced channel modes for " + 
			    channel.getChannelName() + " are now: ";

			if( channel.getPosModes() != null ) {
			    ret += "+" + channel.getPosModes();
			}
			if( channel.getNegModes() != null ) {
			    ret += "-" + channel.getNegModes();
			}
			write(ret);
		    }
		}
	    } else if( args[0].equals("botnick") ) {
		if( invoker.isGlobalAdmin() ) {
		    if( ((args.length == 2) || (args.length == 3)) && args[1] != null && !args[1].equals("") ) {
			caller.write("NICK "+args[1]+"\n");
			caller.getInstanceData().setBotNick(args[1]);
			
			String msg = "Set botnick to '"+args[1]+"'";
			if( args.length == 3 && args[2] != null && !args[2].equals("") ) {
			    caller.getInstanceData().setBotAltNick(args[2]);
			    msg += " and altnick to '"+args[2]+"'";
			}
			write(msg);
		    }
		}
	    }
	}
    }

    /**
     * Tries to join channel in args[0] with key in args[1] if given. 
     * The channel must exist in bots record.
     *
     * @param host Host of invoker
     * @param invoker User object of invoker
     * @param args command arguments
     * @param channel Channel where command takes place
     */
    private void commandJoin(Host host,User invoker,String args[],Channel channel)
    {
	/* requires global admin*/
	if( (args != null) && ((args.length == 1) || (args.length == 2)) && invoker.isGlobalAdmin() ) {
	    Channel chan = caller.findChannel(args[0]);
	    if( chan == null ) {
		write("No such channel "+args[0]+". Use add channel first.");
	    } else {
		if( !chan.isJoined() ) {
		    if( args.length == 2 ) {
			chan.setChannelKey(args[1]);
			write("Set channel key for "+chan.getChannelName()+" to '"+args[1]+"'.");
			caller.write("JOIN "+chan.getChannelName()+" "+args[1]+"\n");			
		    } else {
			caller.write("JOIN "+chan.getChannelName()+"\n");
		    }
		}
	    }
	}
    }
	    
    /**
     * Process command message. assuming valid channel argument.
     *
     * @param msg command msg string
     * @param channel valid channel name
     */
    private void processCmdMsg(Host host,String cmd,Channel channel,String args[]) 
    {
	User user = caller.findUser(host);

	/* all commands require user in bot */
	if( (user != null) && channel.isJoined() ) {
	    if( cmd.equals("k") ) {
		commandK(host,user,args,channel);
	    } else if( cmd.equals("bk") ) {
		commandBK(host,user,args,channel);
	    } else if( cmd.equals("add") ) {
		commandAdd(host,user,args,channel);
	    } else if( cmd.equals("list") ) {
		commandList(host,user,args,channel);
	    } else if( cmd.equals("del") ) {
		commandDel(host,user,args,channel);
	    } else if( cmd.equals("edit") ) {
		commandEdit(host,user,args,channel);
	    } else if( cmd.equals("join") ) {
		commandJoin(host,user,args,channel);
	    }
	}
    }

    /**
     * Handles -b MODEs. Re-bans any hostmasks that are defined in the
     * permban list.
     *
     * @param host Host of the unbanner
     * @param channel Channel on which the mode was set
     * @param target unbanned hostmask
     */
    private void doUnBan(Host host,Channel channel,String target)
    {
	String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	Vector v = (Vector)permbanList.get(key);
	    
	if( v != null ) {
	    for( int j = 0; j < v.size(); j++ ) {
		String banMask = (String)v.elementAt(j);
		if( banMask.equalsIgnoreCase(target) ) {
		    ModeQueueElement element = 
			new ModeQueueElement(Irc.MODE_BAN,ModeQueueElement.PRIORITY_NORMAL,
					     banMask,channel.getChannelName());
		    caller.getModeQueue().pushMode(element);
		    
		    if( host.isWellformed() ) {
			caller.write("NOTICE "+host.getNick()+" :Do not unban "+banMask+", "+
				     "it is in my permban list.\n");
		    }
		}
	    }
	}
    }

    /**
     * Handles MODEs
     *
     * @param message the MODE IrcMessage
     */
    private void doMode(IrcMessage message) 
    {
	if( message.arguments.length < 3 ) {
	    return;
	}

	String modeString = message.arguments[1];
	int index = 0;
	boolean polarity = true;
	Channel channel = caller.findChannel(message.arguments[0]);
	Host host = new Host(message.prefix);

	for( int i = 0; i < modeString.length(); i++ ) {
	    switch( modeString.charAt(i) ) {
	    case '-':   
		polarity = false;
		break;
	    case '+':
		polarity = true;
		break;
	    case 'o':
	    case 'v':
	    case 'I':
	    case 'e':
	    case 'k':
		index++;
		break;
	    case 'l':
		if( polarity ) { index++; }
		break;
	    case 'b':
		if( !polarity ) {
		    doUnBan(host,channel,message.arguments[index+2]);
		}
		index++;
		break;
	    }
	}	    
    }	

    /**
     * Handles JOINs when the bot itself joins a channel.
     *
     * @param channel the Channel bot joins on
     */
    private void doSelfJoin(Channel channel)
    {
	Vector v = channel.getBanList();
	String key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	Vector bans = (Vector)permbanList.get(key);

	/* if permbans not already banned, ban them */
	if( (v != null) && (bans != null) ) {
	    for( int i = 0; i < bans.size(); i++ ) {
		String permban = (String)bans.elementAt(i);
		boolean found = false;

		for( int j = 0; j < v.size(); j++ ) {
		    String mask = (String)v.elementAt(j);
		    if( permban.equalsIgnoreCase(mask) ) {
			found = true;
		    }
		}

		if( !found ) {
		    ModeQueueElement element = 
			new ModeQueueElement(Irc.MODE_BAN,ModeQueueElement.PRIORITY_NORMAL,
					     permban,channel.getChannelName());
		    caller.getModeQueue().pushMode(element);
		}
	    }
	} 

	/* if no ChanVars for channels exist, set default ones */
	key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	if( !chanvars.containsKey(key) ) {
	    chanvars.put(key,new ChanVars());
	    changed = true;
	}
    }

    /**
     * Handles JOINs
     *
     * @param message PRIVMSG IrcMessage to process
     */
    private void doJoin(IrcMessage message) 
    {
	Channel channel = caller.findChannel(message.trailing);
	Host host = new Host(message.prefix);

	if( host.getNick().equals(caller.getHost().getNick()) ) {
	    doSelfJoin(channel);
	}
    }

    /**
     * Handles PRIVMSGs 
     *
     * @param message PRIVMSG IrcMessage to process
     */
    private void doPrivmsg(IrcMessage message) 
    {
	Host host = new Host(message.prefix);
	Channel channel = null;
	String args[] = null;
	String cmd = null;

	if( message.arguments[0].equals(caller.getHost().getNick()) ) {
	    /* PRIVMSG to bot */
	    this.source = host.getNick();
	    args = StringUtil.separate(message.trailing,' ');
	    if( (args != null) && (args.length >= 2) ) {
		channel = caller.findChannel(args[1]);
		cmd = args[0];
		args = StringUtil.range(args,2);
	    }
	} else {
	    /* PRIVMSG to channel */
	    channel = caller.findChannel(message.arguments[0]);
	    this.source = message.arguments[0];
	    if( (message.trailing.charAt(0) == '!') &&
		(message.trailing.length() > 1) ) {
		args = StringUtil.separate(message.trailing.substring(1),' ');
		if( args != null ) {
		    cmd = args[0];
		    args = StringUtil.range(args,1);
		}
	    }
	}

	if( (channel != null) && (cmd != null) ) {
	    processCmdMsg(host,cmd,channel,args);
	}
    }

    /**
     * Processes incoming IrcMessages from a ServerConnection. Sets instance 
     * variable caller to refer to the calling ServerConnection.
     *
     * @param message IrcMessage to process
     * @param serverConnection invoking ServerConnection
     */
    protected void processMessage(IrcMessage message,ServerConnection serverConnection)
    {
	this.caller = serverConnection;

	if( message.command.equals("PRIVMSG") ) {
	    if( (message.trailing != null) && 
		(message.trailing.length() > 0) ) {
		doPrivmsg(message);
	    }
	} else if( message.command.equals("JOIN") ) {
	    doJoin(message);
	} else if( message.command.equals("MODE") ) {
	    doMode(message);
	}

	/* set per-request vars to null */
	this.caller = null;
	this.source = null;
    }

    /**
     * Sends message to source (channel/user)
     *
     * @param message message to send
     * @exception IllegalStateException thrown if source param was null
     */
    private void write(String message)
    {
	if( source == null ) {
	    throw new IllegalStateException("ChannelTools.write(): source == null!");
	}
	caller.write("PRIVMSG "+source+" :"+message+"\n");
    }
}


















