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
package irssibot.user;

import java.util.*;
/**
 * Represents a user in bot's userbase. Instances of ServerConnection keep 
 * a list of User's.
 *
 * @author Matti Dahlbom
 * @see irssibot.core.ServerConnection
 */
public class User
{
    /* ugly static - todo: remove it */
    private static final long loginExpirationTime = 3600000; // 1 hour

    /* user data */
    private String name = null;
    private String password = null;
    /**
     * This is the host that a dynamic user had when doing login.
     */
    private Host loginHost = null;

    private Hashtable channels = null;
    private Vector hosts = null;
    
    /* private state data */
    private long loginTime = 0;
    private boolean isGlobalAdmin = false;
    private boolean dynamic = false;

    /**
     * Contains UserChannelInfo objects with channel names as keys
     */
    public Hashtable getChannels() { return channels; }
    public Vector getHosts() { return hosts; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public Host getLoginHost() { return loginHost; }
    public boolean isDynamic() { return dynamic; }
    
    public User(String name,String globalFlags)
    {
	channels = new Hashtable();
	hosts = new Vector();

	this.name = name;
	if( globalFlags != null ) {
	    processGlobalFlagsString(globalFlags);
	}
    }

    /**
     * attempts to login a user with password.
     */
    public void doLogin(Host host)
    {
	loginTime = new Date().getTime();
	loginHost = host;
    }

    /**
     * Attempts to login a user with password.
     */
    public void doLogout()
    {
	loginTime = 0;
	loginHost = null;
    }

    /**
     * Checks whether a user has logged in and that the logon has not
     * expired.
     *
     * @return <ul><li>true if logged in<li>false if expired/not logged in</ul>
     */
    public boolean isLoggedIn()
    {
	boolean ret = true;

	/* non-dynamic users need no login. */
	if( dynamic ) {
	    long now = new Date().getTime();
	    if( (now - loginTime) > loginExpirationTime ) {
		/* logon expired */
		ret = false;
	    } 
	}  

	return ret;
    }

    /**
     * Look through hostmasks. If a host with wildcard * found, mark the user dynamic.
     *
     */
    private void checkHosts() 
    {
	for( int i = 0; i < hosts.size(); i++ ) {
	    Host mask = (Host)hosts.elementAt(i);
	    if( mask.isStarnameHost() ) {
		dynamic = true;
		return;
	    }
	}
	dynamic = false;
    }

    /**
     * Takes a Vector of Strings representings host masks as argument, and store
     *
     * the hosts into another Vector as Host objects.
     * @param maskStrings input hosts
     */
    public void addHosts(Vector maskStrings)
    {
	for( int i = 0; i < maskStrings.size(); i++ ) {
	    hosts.add(new Host((String)maskStrings.elementAt(i)));
	}
	
	checkHosts();
    }

    /**
     * Deletes a single hostmask from user.
     *
     * @param host host to be deleted
     * @return <ul><li>true if successful<li>false if not found</ul>
     */
    public boolean delHost(Host host)
    {
	boolean ret = false;

	for( int i = 0; i < hosts.size(); i++ ) {
	    Host mask = (Host)hosts.elementAt(i);
	    if( host.equals(mask) ) {
		hosts.removeElementAt(i);
		ret = true;
	    }
	}
	checkHosts();

	return ret;
    }

    /**
     * adds a single hostmask to user
     *
     * @param host hostmask to add
     */
    public void addHost(Host host)
    {
	hosts.add(host);

	checkHosts();
    }

    public int getNumChannels()
    {
	return channels.size();
    }

    public String getGlobalFlags()
    {
	String ret = "";

	if( isGlobalAdmin ) ret += "A";

	return ret;
    }

    public void setPassword(String password)
    {
	this.password = password;
    }

    /**
     * Checks if user has a matching host. 
     *
     * @param host host to match against
     * @return true if user has a matching host
     */
    public boolean hasMatchingHost(Host host)
    {
	/* go through all hosts */
	for( int i = 0; i < hosts.size(); i++ ) {
	    Host userHost = (Host)hosts.elementAt(i);
	    if( host.matches(userHost) ) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * returns the channel flags string for user on channel.
     * @param channelName name of channel
     * @return flags string
     */
    public String getChannelFlags(String channelName) 
    {
	String ret = null;

	UserChannelInfo chanInfo = (UserChannelInfo)channels.get(channelName);
	if( chanInfo != null ) {
	    /* user has channel defined */
	    ret = chanInfo.getChannelFlags();
	}
	return ret;
    }

    /**
     * checks if user has o flag for channel. dyn users must be logged in.
     * @param channelName name of channel
     * @return true/false
     */
    public boolean isOp(String channelName) 
    {
	boolean ret = false;

	if( isLoggedIn() ) {
	    UserChannelInfo chanInfo = (UserChannelInfo)channels.get(channelName);
	    if( chanInfo != null ) {
		/* user has channel defined */
		ret = chanInfo.isOp();
	    }
	}

	return ret;
    }

    /**
     * checks if user has v flag for channel. dyn users must be logged in.
     * @param channelName name of channel
     * @return true/false
     */
    public boolean isVoice(String channelName) 
    {
	boolean ret = false;

	if( isLoggedIn() ) {
	    UserChannelInfo chanInfo = (UserChannelInfo)channels.get(channelName);
	    if( chanInfo != null ) {
		/* user has channel defined */
		ret = chanInfo.isVoice();
	    }
	}

	return ret;
    }

    /**
     * checks if user has A flag for channel. dyn users must be logged in.
     * @param channelName name of channel
     * @return true/false
     */
    public boolean isChanAdmin(String channelName) 
    {
	boolean ret = false;

	if( isLoggedIn() ) {
	    UserChannelInfo chanInfo = (UserChannelInfo)channels.get(channelName);
	    if( chanInfo != null ) {
		/* user has channel defined */
		ret = chanInfo.isChanAdmin();
	    }
	}

	return ret;
    }

    /**
     *  checks if user is a global admin
     * @return true/false
     */
    public boolean isGlobalAdmin()
    {
	return isGlobalAdmin;
    }


    /**
     * get User info dump as XML in the following form:
     *
     * <p> 
     * <!-- user record begin -->   
     * <user name="username" global-flags="flags" password="password">
     *   <channel name="#chan1" flags="flags" />   
     *   <channel name="#chan2" flags="flags" />
     * 
     *   <hostmasks>
     *     <mask>mask1</mask>
     *	   <mask>mask2</mask>
     *   </hostmasks>
     * </user>   
     * <!-- user record end -->
     * </p>
     * @return XML dump as string
     */
    public String getXML()
    {
	StringBuffer passwordBuffer = new StringBuffer(8);
	String pass = "";
	String ret = "<!-- user record begin -->\n";

	if( (password != null) && (password.length() == 8) ) {
	    /* scramble password */
	    for( int i = 0; i < 8; i++ ) {
		if( (password.charAt(i) >= '0') && (password.charAt(i) <= '9') ) {
		    /* dont touch numerals .. */
		    passwordBuffer.append(password.charAt(i));
		} else {
		    /* scramble others */
		    passwordBuffer.append( (char)(password.charAt(i) + (i+1)) );
		}
	    }
	    pass = passwordBuffer.toString();
	}

	ret += "<user name=\""+name+"\" global-flags=\""+getGlobalFlags()+"\" password=\""+pass+"\">\n";
	Enumeration enum = channels.elements();
	while( enum.hasMoreElements() ) {
	    UserChannelInfo chanInfo = (UserChannelInfo)enum.nextElement();
	    ret += "  <channel name=\""+chanInfo.getChannelName()+"\" flags=\""+chanInfo.getChannelFlags()+"\" />\n";
	}
	ret += "  <hostmasks>\n";
	for( int i = 0; i < hosts.size(); i++ ) {
	    Host host = (Host)hosts.elementAt(i);
	    ret += "    <mask>"+host.toString()+"</mask>\n";
	}
	ret += "  </hostmasks>\n";
	ret += "</user>\n";
	ret += "<!-- user record end -->\n";

	return ret;
    }

    /**
     * tries to remove a UserChannelInfo from channels hash table.
     * @param channelName name of removed channel
     * @return <ul>true if successful<ul>false if no such channel info defined
     */
    public boolean removeChannelInfo(String channelName)
    {
	boolean ret = false;

	if( channels.remove(channelName.toLowerCase()) != null )
	    ret = true;

	return ret;
    }

    /**
     * tries to add UserChannelInfo to channels hash table.
     * @param channelInfo UserChannelInfo
     * @return <ul>false if channelInfo already exists in hash table<ul>true otherwise
     */
    public boolean addChannelInfo(UserChannelInfo channelInfo)
    {
	boolean ret = true;

	/* check if channelInfo already exists in hash table */
	if( channels.containsKey(channelInfo.getChannelName().toLowerCase()) ) {
	    ret = false;
	} else {
	    /* add to hash table */
	    channels.put(channelInfo.getChannelName().toLowerCase(),channelInfo);
	}
	return ret;
    }

    /**
     * finds and returns channel info or null if not specified.
     * @param channel name of channel
     * @return channel info
     */
    public UserChannelInfo getChannelInfo(String channelName)
    {
	return (UserChannelInfo)channels.get(channelName.toLowerCase());
    }

    /**
     * go through global flags string and turn on indicated global flags
     * @param globalFlags global flags string
     */
    public void processGlobalFlagsString(String globalFlags)
    {
	if( globalFlags != null ) {
	    for( int i = 0; i < globalFlags.length(); i++ ) {
		switch( globalFlags.charAt(i) ) {
		case 'A' : /* global admin flag */
		    isGlobalAdmin = true;
		    break;
		}
	    }
	} else {
	    putlog("processGlobalFlagsString(): null globalFlags");
	}
    }

    /** 
     * write a string to log stream
     *
     * @param logMsg string to write to log
     */
    private void putlog(String logMsg) 
    {
        System.out.println(getClass().getName()+": "+logMsg);
    }
}










