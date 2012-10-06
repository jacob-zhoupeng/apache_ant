/*
 * $Id: TopicTools.java,v 1.1.1.1 2001/03/26 10:57:42 matti Exp $
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

import java.util.*;
import java.text.SimpleDateFormat;

/* imports */
import irssibot.core.*;
import irssibot.user.*;
import irssibot.util.*;

/**
 * Represents an topic entry in topic list.
 */
class TopicEntry 
{
    private String content = null;
    private String author = null;
    private Date timeStamp = null;
    private String dateFormatString = null;
   
    public TopicEntry(String content,String author,Date timeStamp,String dateFormatString)
    {
	this.timeStamp = timeStamp;
	this.content = content;
	this.dateFormatString = dateFormatString;
	
	if( author != null ) 
	    this.author = author;
	else
	    this.author = "Unknown";
    }

    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public Date getTimeStamp() { return timeStamp; }

    public TopicEntry(String content,String author,String dateFormatString) {
	this(content,author,new Date(),dateFormatString);
    }

    public String toString()
    {
	String ret = null;

	SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
	ret = content+" ("+sdf.format(timeStamp)+", "+author+")";

	return ret;
    }
}

/**
 * Implements a series of utilities to edit channel topic.
 *
 * @author Matti Dahlbom 
 * @version $Name:  $ $Revision: 1.1.1.1 $
 */
public class TopicTools /*implements Module */extends AbstractModule
{
    /* statics */
    private static String moduleInfo = "Topic Tools 1.0.0 for IrssiBot";

    private Hashtable topicStore = null;
    private boolean hasChanged = false;
    private String dateFormatString = null;

    /* per-request temp data */
    private Host botHost = null;
    private Host host = null;
    private String source = null;
    private ServerConnection caller = null;

    public TopicTools()
    {
    }

    public boolean onLoad(Properties state,Core core)
    {
	topicStore = new Hashtable();

	dateFormatString = core.getDateFormatString();
	
	if( state != null ) 
	    loadInitialState(state,core);
	else
	    getAllTopics(core);
	
	return true;
    }

    /**
     * Initializes the module from the given state.
     *
     * @param state initial state
     */
    private void loadInitialState(Properties state,Core core)
    {
	Vector v = core.getServerInstances();
	ServerConnection connection = null;
	Enumeration channels = null;
	Channel channel = null;
	String key = null;
	String tmp = null;
	int numTopics ;
	Date timestamp = null;

	/* go through the server connections */
	for( int i = 0; i < v.size(); i++ ) {
	    connection = (ServerConnection)v.elementAt(i);
	    channels = connection.getChannels().elements();

	    /* go through channels for this server connection */
	    while( channels.hasMoreElements() ) {
		channel = (Channel)channels.nextElement();
		key = connection.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		
		/* get the number of topic entries for this channel */
		tmp = state.getProperty(key+"-numTopics");
		if( tmp != null ) {
		    try {
			numTopics = Integer.parseInt(tmp);
		    } catch( NumberFormatException e ) {
			numTopics = -1;
		    }
		    if( numTopics > 0 ) {
			TopicEntry values[] = new TopicEntry[numTopics];

			/* get all topic entries for channel */
			for( int j = 0; j < numTopics; j++ ) {
			    String content = state.getProperty(key+"-topic"+j);
			    String author = state.getProperty(key+"-author"+j);
			    String timeStampStr = state.getProperty(key+"-timestamp"+j);
			    try {
				timestamp = new Date(Long.parseLong(timeStampStr));
			    } catch( NumberFormatException e ) { 
				timestamp = new Date();
			    }
			    
			    values[j] = new TopicEntry(content,author,timestamp,dateFormatString);
			}
			topicStore.put(key,values);
		    }
		}
	    }
	}
    }
			
    /**
     * Gets (same as !tg) topics of all channels of all server connections.
     *
     * @param core Core instance
     */
    private void getAllTopics(Core core)
    {
	Vector v = core.getServerInstances();
	source = null;
	Enumeration channels = null;
	Channel channel = null;

	for( int i = 0; i < v.size(); i++ ) {
	    caller = (ServerConnection)v.elementAt(i);
	    channels = caller.getChannels().elements();

	    while( channels.hasMoreElements() ) {
		channel = (Channel)channels.nextElement();
		commandTG(null,null,null,channel);
	    }
	}
    }

    public void onUnload()
    {
	topicStore = null;
    }

    public Properties getState() 
    { 
	Properties props = null;

	if( hasChanged ) {
	    hasChanged = false;
	    props = constructState();
	} 
	return props;
    }
    
    /**
     * Constructs a Properties object representing the state of this module.
     * Stored in the Properties for each channel of each server connection:<ul>
     * <li>number of channel topics as network-channel-numTopics
     * <li>each topic entry as network-channel-topicN (N = {0,1,2,..,numTopics-1})
     * <li>time stamp of each topic entry as network-channel-timestampN (see above)
     * <li>author of each topic entry as network-channel-authorN (see above)</ul>
     * 
     * @return the constructed Properties object
     */
    private Properties constructState()
    {
	Enumeration keys = topicStore.keys();
	Properties props = new Properties();
	String key = null;
	TopicEntry value[] = null;

	while( keys.hasMoreElements() ) {
	    key = (String)keys.nextElement();
	    value = (TopicEntry[])topicStore.get(key);

	    props.put(key+"-numTopics",String.valueOf(value.length));
	    for( int i = 0; i < value.length; i++ ) {
		props.put(key+"-topic"+i,value[i].getContent());
		props.put(key+"-timestamp"+i,String.valueOf(value[i].getTimeStamp().getTime()));
		props.put(key+"-author"+i,value[i].getAuthor());
	    }
	}
	return props;
    }

    /**
     * returns a module info string 
     */
    public String getModuleInfo()
    {
	return moduleInfo;
    }

    /**
     * combine topics and send channel topic to server
     *
     * @param value entries of topic
     * @param channel to set topic on
     */
    private void constructTopic(TopicEntry value[],Channel channel)
    {
	String sep = "";
	String topic = "";

	/* do nothing if not op on channel */
	if( channel.isOp() && value != null ) {
	    for( int i = 0; i < value.length; i++ ) {
		topic += sep + value[i].getContent();
		sep = " | ";
	    }
	    
	    caller.write("TOPIC "+channel.getChannelName()+" :"+topic+"\n");
	}
    }

    /**
     * deletes a topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTD(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;
	TopicEntry newValue[] = null;
	int index = -1;
	boolean isOk = true;

	/* construct hash key from server instance name and channel name */
	key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();

	if( topicStore.containsKey(key) ) {
	    /* get & remove old value */
	    value = (TopicEntry[])topicStore.get(key);
	    if( value.length < 1 ) {
		/* already null topic */
		isOk = false;
	    } else {
		newValue = new TopicEntry[value.length - 1];
	    }
	    
	    if( isOk && (args != null) && (args.length == 1) && (args[0] != null)) {
		try {
		    index = Integer.parseInt(args[0]);
		} catch( NumberFormatException e ) {
		    /* bad argument */
		    isOk = false;
		}
		
		if( isOk && (index > -1) ) {
		    System.out.println(index);

		    if( index >= value.length )
			index = value.length - 1;

		    if( index < 0 )
			index = 0;
		    
		    for( int i = 0,j = 0; i < value.length; i++ ) {
			if( i != index ) {
			    newValue[j] = value[i];
			    j++;
			} else {
			    write("deleted topic #"+i+": "+value[i].toString());
			}
		    }
		    topicStore.put(key,newValue);
		    constructTopic(newValue,channel);
		    hasChanged = true;
		}
	    }
	}
    }   

    /**
     * lists all topics
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTL(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;
	TopicEntry newValue[] = null;

	/* construct hash key from server instance name and channel name */
	key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();

	if( topicStore.containsKey(key) ) {
	    value = (TopicEntry[])topicStore.get(key);

	    for( int i = 0; i < value.length; i++ ) {
		write("topic #"+i+": "+value[i].toString());
	    }
	}
    }   

    /**
     * inserts a new topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTI(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;
	TopicEntry newValue[] = null;
	int index = 0;
	boolean isOk = true;

	if( (args != null) && args.length >= 2 ) {
	    try {
		index = Integer.parseInt(args[0]);
	    } catch( NumberFormatException e ) { 
		/* bad arguments */
		isOk = false;
	    }

	    if( isOk ) {
		/* construct hash key from server instance name and channel name */
		key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		
		if( topicStore.containsKey(key) ) {
		    value = (TopicEntry[])topicStore.get(key);

		    if( (index >= 0) && (index < value.length) ) {
			newValue = new TopicEntry[value.length+1];
			
			for( int i = 0,j = 0; i < newValue.length; i++ ) {
			    if( i == index ) {
				/* insert new topic here */
				newValue[i] = new TopicEntry(StringUtil.join(args,1),invoker.getName(),dateFormatString);
			    } else {
				newValue[i] = value[j++];
			    }
			}
			topicStore.put(key,newValue);
			constructTopic(newValue,channel);
			hasChanged = true;
		    }
		}
	    }
	}				
    }

    /**
     * swaps two topics
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTS(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;
	TopicEntry tmp = null;
	int index1 = 0;
	int index2 = 0;
	boolean isOk = true;

	if( (args != null) && args.length == 2 ) {
	    try {
		index1 = Integer.parseInt(args[0]);
		index2 = Integer.parseInt(args[1]);
	    } catch( NumberFormatException e ) { 
		/* bad arguments */
		isOk = false;
	    }

	    if( isOk ) {
		/* construct hash key from server instance name and channel name */
		key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
		
		if( topicStore.containsKey(key) ) {
		    value = (TopicEntry[])topicStore.get(key);

		    if( (index1 >= 0) && (index1 < value.length) &&
			(index2 >= 0) && (index2 < value.length) ) {
			/* swap topics */
			tmp = value[index1];
			value[index1] = value[index2];
			value[index2] = tmp;
	    
			/* write back to topic store */
			topicStore.put(key,value);
			hasChanged = true;
			
			constructTopic(value,channel);
		    }
		}
	    }
	}
    }

    /**
     * sets a new topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTOPIC(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;

	if( (args != null) && args.length > 0 ) {
	    /* construct hash key from server instance name and channel name */
	    key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	    
	    /* add new topic to store as single-entry array */
	    value = new TopicEntry[1];
	    value[0] = new TopicEntry(StringUtil.join(args),invoker.getName(),dateFormatString);
	    topicStore.put(key,value);

	    constructTopic(value,channel);
	}
    }

    /**
     * appends a new topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTA(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;
	TopicEntry newValue[] = null;

	if( (args != null) && (args.length > 0) ) {
	    /* construct hash key from server instance name and channel name */
	    key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	    
	    if( topicStore.containsKey(key) ) {
		value = (TopicEntry[])topicStore.get(key);
		newValue = new TopicEntry[value.length+1];
		
		/* copy existing entries */
		for( int i = 0; i < value.length; i++ ) 
		    newValue[i] = value[i];

		/* setup new entry and replace to topic store */
		newValue[value.length] = new TopicEntry(StringUtil.join(args),invoker.getName(),dateFormatString);
		topicStore.put(key,newValue);
		hasChanged = true;
		
		constructTopic(newValue,channel);
	    }
	}
    }   

    /**
     * Edits a topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTE(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;

	if( (args != null) && (args.length >= 2) ) {
	    /* construct hash key from server instance name and channel name */
	    key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	    
	    if( topicStore.containsKey(key) ) {
		value = (TopicEntry[])topicStore.get(key);

		int i = -1;
		try {
		    i = Integer.parseInt(args[0]);
		} catch( NumberFormatException e ) {
		    return;
		}

		if( (i >= 0) && (i < value.length) ) {
		    value[i] = new TopicEntry(StringUtil.join(args,1),invoker.getName(),dateFormatString);

		    topicStore.put(key,value);
		    hasChanged = true;
		    
		    constructTopic(value,channel);
		}
	    }
	}
    }   

    /**
     * sets (refreshes) channel topic from store
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTR(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;

	/* construct hash key from server instance name and channel name */
	key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();

	if( topicStore.containsKey(key) ) {
	    value = (TopicEntry[])topicStore.get(key);
	    constructTopic(value,channel);
	} 
    }

    /**
     * gets the current channel topic
     *
     * @param host host of invoker
     * @param invoker invoking User
     * @param args arguments of command
     * @param channel target channel
     */
    private void commandTG(Host host,User invoker,String args[],Channel channel)
    {
	String key = null;
	TopicEntry value[] = null;

	/* construct hash key from server instance name and channel name */
	key = caller.getInstanceData().getNetwork()+"-"+channel.getChannelName();
	
	if( topicStore.contains(key) ) {
	    /* remove existing topic */
	    topicStore.remove(key);
	}

	/* add new topic to store as single-entry array */
	if( channel.getChannelTopic() == null ) {
	    /* on a null topic, remove the topic entries completely */
	    topicStore.remove(key);
	} else {
	    write("Getting topic: "+channel.getChannelTopic());
	    value = new TopicEntry[1];
	    value[0] = new TopicEntry(channel.getChannelTopic(),null,dateFormatString);
	    topicStore.put(key,value);
	    hasChanged = true;
	}
    }

    /**
     * Processes command message. assuming valid channel argument.
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
		if( cmd.equals("ta") ) {
		    commandTA(host,user,args,channel);
		} else if( cmd.equals("td") ) {
		    commandTD(host,user,args,channel);
		} else if( cmd.equals("tl") ) {
		    commandTL(host,user,args,channel);
		} else if( cmd.equals("tg") ) {
		    commandTG(host,user,args,channel);
		} else if( cmd.equals("ti") ) {
		    commandTI(host,user,args,channel);
		} else if( cmd.equals("ts") ) {
		    commandTS(host,user,args,channel);
		} else if( cmd.equals("te") ) {
		    commandTE(host,user,args,channel);
		} else if( cmd.equals("topic") ) {
		    commandTOPIC(host,user,args,channel);
		} else if( cmd.equals("tr") ) {
		    /* dont bother to call if not op */
		    if( channel.isOp() ) {
			commandTR(host,user,args,channel);
		    }
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
	if( source != null ) {
	    caller.write("PRIVMSG "+source+" :"+message+"\n");
	}
    }
}








