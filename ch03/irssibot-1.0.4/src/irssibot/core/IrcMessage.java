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
package irssibot.core;

/* import other IrssiBot packages */
import irssibot.config.*;
import irssibot.util.*;

/**
 * represents a IRC message
 *
 * ##TODO## optimize heavily!
 *
 *
 * @author Matti Dahlbom
 */
public class IrcMessage
{
    /* data */
    public String prefix = null;
    public String command = null;
    public String arguments[] = null;
    public String trailing = null;

    private String rawMessageString = null;

    public IrcMessage(String line)
    {
	String tmp = line.trim();
	boolean isOk = true;
	int spaceIndex;
	int colonIndex;

	if( line.charAt(0) == ':' ) {
	    spaceIndex = tmp.indexOf(' ');

	    if( spaceIndex < 0 ) {
		isOk = false;
	    } else {
		prefix = tmp.substring(1,spaceIndex);
		tmp = tmp.substring(spaceIndex+1);
	    }
	} else {
	    prefix = null;
	}

	if( isOk ) {
	    /* separate command from the message */
	    spaceIndex = tmp.indexOf(' ');
	    if( spaceIndex == -1 ) {
		throw new IllegalArgumentException("bad input: " + line);
	    }

	    command = tmp.substring(0,spaceIndex);
	    tmp = tmp.substring(spaceIndex+1);

	    /* separate argument list & trailing part from the message */
	    colonIndex = tmp.indexOf(':');
	    
	    if( colonIndex == -1 ) {
		/* there is no ':' and therefore no trailing message */
		arguments = StringUtil.separate(tmp,' ');
		trailing = null;
	    } else {
		/* 
		 * make sure the ':' starts a trailing part and not part of an 
		 * IPv6 address. Check the colonIndex is above zero to avoid
		 * problems with commands like ERROR :Closing Link
		 */
		if( colonIndex > 0 ) {
		    while( !((tmp.charAt(colonIndex) == ':') &&
			     (tmp.charAt(colonIndex - 1) == ' ')) &&
			   (colonIndex < tmp.length()) ) {
			colonIndex++;
		    }
		}

		if( colonIndex < tmp.length() ) {
		    /* there is a trailing message */
		    arguments = StringUtil.separate(tmp.substring(0,colonIndex),' ');
		    trailing = tmp.substring(colonIndex+1);
		} else {
		    arguments = StringUtil.separate(tmp,' ');
		    trailing = null;
		}
	    }

	    /* store the original message */
	    rawMessageString = line;
	}
    }

    public String toString()
    {
	return rawMessageString;
    }
}




