/*
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

/* imports */
import irssibot.core.*;
import irssibot.user.*;
import irssibot.util.*;

import java.util.Vector;
import java.util.Properties;

public class UserTools extends AbstractModule
{
    /* statics */
    private static String moduleInfo = "User Tools 1.0.0 for IrssiBot";

    /* per-request temp data */
    private Host host = null;
    private String source = null;
    private ServerConnection caller = null;

    private boolean isOk = true;

    public Properties getState()
    {
	return null;
    }

    /**
     * returns a module info string 
     */
    public String getModuleInfo()
    {
	return moduleInfo;
    }

    public boolean onLoad(Properties state,Core core)
    {
	return true;
    }

    public void onUnload()
    {
    }

    /**
     * list command 
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandList(Host host,User invoker,String args[],Channel channel)
    {
	Host hostMask = null;
	String flagsStr = null;
	Vector hosts = null;
	String dyn = null;

	if( (args != null) && (args.length > 0) ) {
	    if( args[0].equals("users") ) {
		Vector users = caller.getUsers();
		
		if( args.length > 1 ) {
		    /* list user details. */
		    User user = caller.findUser(args[1]);

		    if( (user != null) && (invoker != null)  ) {
			hosts = user.getHosts();
			/* need to be user himself or global/chan admin. */
			if( (user == invoker) || invoker.isChanAdmin(channel.getChannelName()) || 
			    invoker.isGlobalAdmin() ) {
			    String status = "";

			    if( user.isChanAdmin(channel.getChannelName()) )
				status = " (channel admin)";
			    else if( user.isOp(channel.getChannelName()) )
				status = " (channel op)";
			    flagsStr = ", flags="+user.getChannelFlags(channel.getChannelName());
			    flagsStr += " (global flags="+user.getGlobalFlags()+")";
			    write(channel.getChannelName()+" user "+user.getName()+status+flagsStr);

			    /* list host masks */
			    for( int i = 0; i < hosts.size(); i++ ) {
				hostMask = (Host)hosts.elementAt(i);
				if( hostMask.isStarnameHost() ) 
				    dyn = " <dynamic>";
				else 
				    dyn = "";
				
				write("hostmask: "+hostMask.toString()+dyn);
			    }
			}
		    }
		} else {
		    String userListStr = "";

		    /* list users for channel */
		    for( int i = 0; i < users.size(); i++ ) {
			User user = (User)users.elementAt(i);
			
			if( user.getChannels().containsKey(channel.getChannelName()) ) {
			    if( user.isChanAdmin(channel.getChannelName()) )
				userListStr += "*"+user.getName()+" ";
			    else if( user.isOp(channel.getChannelName()) )
				userListStr += "@"+user.getName()+" ";
			    else if( user.isVoice(channel.getChannelName()) )
				userListStr += "+"+user.getName()+" ";
			    else
				userListStr += user.getName()+" ";
			}
		    }
		    write(channel.getChannelName()+" users: "+userListStr);
		}
	    }
	}
    }

    /**
     * add command 
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandAdd(Host host,User invoker,String args[],Channel channel)
    {
	String notice = "";

	if( args.length < 1 ) {
	    return;
	}

	if( args[0].equals("hostmask") ) {
	    User user = caller.findUser(args[1]);
	    
	    if( user != null ) {
		if( (invoker == user) || invoker.isChanAdmin(channel.getChannelName()) ||
		    invoker.isGlobalAdmin() ) {
		    Host newHost = new Host(args[2]);
		    /* if valid hostmask, add to hosts list for user */
		    if( newHost.getNick() != null ) {
			user.addHost(newHost);
			/*
			  if( newHost.isStarnameHost() && !user.isDynamic() ) {
			  notice = " (notice: adding a wildcarded host to a non-dynamic user makes him dynamic.)";
			  }
			*/
			write("Added new host "+newHost.toString()+" to user "+user.getName()+"."+notice);
			caller.notifyUserDataChanged();
		    } else {
			write("Invalid hostmask "+args[2]);
		    }
		}
	    }
	} else if( args[0].equals("user") ) {
	    /* must have user name as 2nd argument */
	    if( args.length >= 2 ) {
		if( invoker.isChanAdmin(channel.getChannelName()) || invoker.isGlobalAdmin() ) {
		    String globalFlags = null;

		    User user = new User(args[1],globalFlags);
		    caller.addUser(user);

		    write("Added user "+user.getName()+".");
		    caller.notifyUserDataChanged();
		}
	    }
	}
    }

    /**
     * edit command. edits<br><ul>flags<ul>globalflags<ul>user password
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandEdit(Host host,User invoker,String args[],Channel channel)
    {
	if( args.length < 3 ) {
	    return;
	}

	if( invoker.isGlobalAdmin() || invoker.isChanAdmin(channel.getChannelName()) ) {
	    User user = caller.findUser(args[1]);
	    if( user != null ) {
		String flagsStr = args[2];

		/* handle flags changes for channel */
		if( args[0].equals("flags") ) {
		    UserChannelInfo chanInfo = user.getChannelInfo(channel.getChannelName()); 
		    if( chanInfo == null ) {
			/* create new info */
			chanInfo = new UserChannelInfo(channel.getChannelName(),flagsStr);
			user.addChannelInfo(chanInfo);
		    } else {
			/* adjust existing info */
			chanInfo.processChannelFlagsString(flagsStr);
		    }
		    write("Channel flags are now: "+user.getChannelFlags(channel.getChannelName())+".");
		    caller.notifyUserDataChanged();
		} else if( args[0].equals("globalflags") ) {
		    /* handle global flags */
		    if( invoker.isGlobalAdmin() ) {
			user.processGlobalFlagsString(flagsStr);
			write("Global flags are now: "+user.getGlobalFlags()+".");
			caller.notifyUserDataChanged();
		    }
		}
	    }
	}

	/* handle password editing */
	if( args[0].equals("password") ) {
	    if( args.length >= 3 ) {
		/* need to be self or global/channel admin */
		User user = caller.findUser(args[1]);
		if( user != null ) {
		    if( (user == invoker) || invoker.isGlobalAdmin() ) {
			if( args[2].length() != 8 ) {
			    write("Password length must be 8 characters.");
			} else {
			    user.setPassword(args[2]);
			    write("Changed password for user "+user.getName()+".");
			    caller.notifyUserDataChanged();
			}
		    }
		}
	    }
	}
    }

    /**
     * del command 
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandDel(Host host,User invoker,String args[],Channel channel)
    {
	if( args.length < 2 ) {
	    return;
	}
	
	if( args[0].equals("user") ) {
	    User user = caller.findUser(args[1]);
	    if( user != null ) {
		String userName = user.getName();
		/* if global admin, remove whole user */
		if( invoker.isGlobalAdmin() ) {
		    if( caller.delUser(user) ) {
			write("Deleted user "+userName);
			caller.notifyUserDataChanged();
		    }
		} else if( invoker.isChanAdmin(channel.getChannelName()) ) {
		    /* chan admin; only remove channel info from user */
		    if( user.removeChannelInfo(channel.getChannelName()) ) {
			/* if no channels remain for user, delete whole user */
			if( user.getNumChannels() < 1 )
			    caller.delUser(user);
			write("Deleted user "+userName);
			caller.notifyUserDataChanged();
		    }
		}
	    }
	} else if( args[0].equals("hostmask") ) {
	    if( args.length >= 3 ) {
		User user = caller.findUser(args[1]);
		if( user != null ) {
		    Host hostMask = new Host(args[2]);
		    if( hostMask.getNick() != null ) {
			String maskStr = hostMask.toString();

			if( user.delHost(hostMask) ) {
			    write("Deleted hostmask "+maskStr+" from user "+user.getName()+".");
			    caller.notifyUserDataChanged();
			}
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
	if( user != null ) {
	    /* bot needs to be ON channel */
	    if( channel.isJoined() ) {
		/* select command */
		if( cmd.equals("list") ) {
		    if( user.isOp(channel.getChannelName()) || user.isChanAdmin(channel.getChannelName()) ||
			user.isGlobalAdmin() ) {
			commandList(host,user,args,channel);
		    }
		} else if( cmd.equals("add") ) {
		    commandAdd(host,user,args,channel);
		} else if( cmd.equals("edit") ) {
		    commandEdit(host,user,args,channel);
		} else if( cmd.equals("del") ) {
		    commandDel(host,user,args,channel);
		}
	    }
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
     * Processes incoming IrcMessages from a ServerConnection
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
	}

	/* set per-request vars to null */
	this.caller = null;
	this.source = null;
    }

    /**
     * Sends message to source (channel/user)
     *
     * @param message message to send
     */
    private void write(String message)
    {
	caller.write("PRIVMSG "+source+" :"+message+"\n");
    }
}










