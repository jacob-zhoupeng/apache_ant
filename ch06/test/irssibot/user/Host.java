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

import irssibot.util.*;
/**
 * represent a nick!ident@host combination, or simply a 'hostmask'
 *
 * @author Matti Dahlbom
 */
public class Host
{
    private String nick = null;
    private String ident = null;
    private String originalIdent = null;
    private String host = null;

    private boolean starnameHost = false;
    private boolean isMask = false;
    private boolean wellformed = false;

    public String getNick() { return nick; }
    public String getIdent() { return originalIdent; }
    public String getHost() { return host; }
    public boolean isWellformed() { return wellformed; }

    public void setNick(String nick) { this.nick = nick; }
    public void setHost(String host) { this.host = host; }
    
    public Host(String hostString)
    {
	int exclamationIndex =  hostString.indexOf('!');
	int atIndex =  hostString.indexOf('@');

	/* look for presence of obligatory '@' */
	if( atIndex == -1 ) {
	    putlog("Host(): bad host string ("+hostString+")");
	} else {
	    if( exclamationIndex == -1 ) {
		exclamationIndex = 1;
		hostString = "*!"+hostString;
		atIndex += 2;
	    }
	    
	    /* check if hoststring represents a mask */
	    if( hostString.indexOf('*') != -1 ) {
		isMask = true;
	    }

	    /* separate parts */
	    nick = hostString.substring(0,exclamationIndex);
	    ident = hostString.substring(exclamationIndex+1,atIndex);
	    originalIdent = ident;
	    host = hostString.substring(atIndex+1);

	    /* check for wildcard * in hostname */
	    if( host.indexOf('*') != -1 ) {
		starnameHost = true;
	    }

	    /* remove possible preceding '~','-','^','=' characters */
	    char first = ident.charAt(0);
	    if( (first == '~') || (first == '-') || (first == '^') || (first == '=') ) {
		ident = ident.substring(1);
	    }
	    
	    wellformed = true;
	}
    }

    /**
     * checks wildcard presence in host
     *
     * @return <ul>
     *           <li>true if wildcards in host -part
     *           <li>false if not
     *         </ul>
     */
    public boolean isStarnameHost() 
    {
	return starnameHost;
    }

    /**
     * returns a string representation of the Host
     * as nick!ident@host
     * @return host as string
     */
    public String toString()
    {
	return (nick+"!"+originalIdent+"@"+host);
    }

    /**
     * Compare this Host to another. The two hosts must match exactly (incasesensitive).
     * @param mask host to compare against
     * @return <ul>true if equal<ul>false if not
     */
    public boolean equals(Host other)
    {
	if( (other == null) || !wellformed || !other.isWellformed() ) 
	    return false;
	if( !nick.equalsIgnoreCase(other.nick) )
	    return false;
	if( !ident.equalsIgnoreCase(other.ident) )
	    return false;
	if( !host.equalsIgnoreCase(other.host) )
	    return false;
	return true;
    }

    /**
     * compare this Host to a mask
     * @param mask mask to compare against
     * @return <ul>true if match
     *         <ul>false if no match or this Host is a mask
     */
    public boolean matches(Host mask)
    {
	boolean ret = false;

	if( wellformed && mask.isWellformed() ) {
	    if( !isMask ) {
		/* wild match nicks, idents and host parts */
		if( StringUtil.wildmatch(mask.nick,nick) &&
		    StringUtil.wildmatch(mask.ident,ident) &&
		    StringUtil.wildmatch(mask.host,host) )
		    ret = true;
	    }
	}

	return ret;
    }

    private void putlog(String msg)
    {
	String logMsg = getClass().getName()+": "+msg+"\n";
	System.out.print(logMsg);
    }
}









