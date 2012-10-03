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
package irssibot.config;

/* import other IrssiBot packages */
import irssibot.core.*;
import irssibot.util.*;
import irssibot.user.*;

import java.util.Vector;

/* XML (dom) parser support */
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;

/**
 * tools for parsing an user file
 *
 * @author Matti Dahlbom
 */
public class UserFileParser
{
    private String userFilePath = null;
    private Document document = null;
    private Vector users = null;

    public UserFileParser(String userFilePath)
    {
	this.userFilePath = userFilePath;
	users = new Vector();

	readUserFile();
    }

    /**
     * invoke recursive parsing
     */
    public boolean parse()
    {
	if( document != null ) {
	    recursiveParse(document);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * get users Vector
     * @return users
     */
    public Vector getUsers()
    {
	return users;
    }

    /**
     * read XML user file from disk. document will hold the parsed 
     * config data.
     * @return true is successful
     */
    private boolean readUserFile()
    {
	boolean ret = true;
	
        try {
            DOMParser parser = new DOMParser();
            parser.parse(userFilePath);
            document = parser.getDocument();
        } catch( Exception e ) {
            putlog("Caught exception: "+e.getMessage());
            document = null;
	    ret = false;
        }
	return ret;
    }

    /**
     * handle node <user> (represents a set of user settings)
     * a sample <user> tag would look like this:
     * <user name="username" global-flags="flags" password="password">
     *   <channel name="#chan1" flags="flags" />   
     *   <channel name="#chan2" flags="flags" />
     * 
     *   <hostmasks>
     *     <mask>mask1</mask>
     *	   <mask>mask2</mask>
     *   </hostmasks>
     * </user>   
     * @param node user node
     */
    private void handleUserTag(Node node)
    {
	String userName = null;
	String password = null;
	String userGlobalFlags = "";
	String chanName = null;
	String chanFlags = null;
	StringBuffer passwordBuffer = new StringBuffer(8);
	User user = null;

	/* read attributes */
	NamedNodeMap attrs = node.getAttributes();
	for( int i = 0; i < attrs.getLength(); i++ ) {
	    Node attr = attrs.item(i);	

	    if( attr.getNodeName().equalsIgnoreCase("global-flags") ) {
		userGlobalFlags = attr.getNodeValue();
	    } else if( attr.getNodeName().equalsIgnoreCase("password") ) {
		password = attr.getNodeValue();
	    } else if( attr.getNodeName().equalsIgnoreCase("name") ) {
		userName = attr.getNodeValue();
	    }
	}

	/* password must be exactly 8 chars long */
	if( password != null ) {
	    if( password.length() != 8 )
		password = null;

	    if( password != null ) {
		/* 
		   unscramble password. the formula is: dont touch numerals. all other character
		   values are incremented by their position in password string, that is by [1..8]
		 */
		for( int i = 0; i < 8; i++ ) {
		    if( (password.charAt(i) >= '0') && (password.charAt(i) <= '9') ) {
			/* dont touch numerals .. */
			passwordBuffer.append(password.charAt(i));
		    } else {
			/* scramble others */
			passwordBuffer.append( (char)(password.charAt(i) - (i+1)) );
		    }
		}
	    }
	    password = passwordBuffer.toString();
	} 

	/* name -attribute must exist */
	if( (userName == null) || userName.equals("") ) {
	    putlog("handleUserTag(): no name -attribute in <user> tag");
	} else {
	    user = new User(userName,userGlobalFlags);
	    user.setPassword(password);

	    NodeList children = node.getChildNodes();
	    if( children != null ) {
		for( int i = 0; i < children.getLength(); i++ ) {
		    Node child = children.item(i);
		    
		    /* handle <channel> */
		    if( child.getNodeName().equalsIgnoreCase("channel") ) {
			handleChannelTag(child,user);
		    }

		    /* handle <hostmasks> */
		    if( child.getNodeName().equalsIgnoreCase("hostmasks") ) {
			user.addHosts(XMLUtil.getListElements(child,"mask"));
		    }
		}
	    }
	    /* add to Vector */
	    users.add(user);
	}
    }
    
    /**
     * called from handleUserTag() to manage a <channel> -tag (represents a channel
     * for which there are attributes defined on the user)
     * @param node channel -node
     * @param user current User instance (from handleUserTag())
     */
    private void handleChannelTag(Node node,User user)
    {
	String chanName = null;
	String chanFlags = null;
	
	NamedNodeMap chanAttrs = node.getAttributes();
	if( chanAttrs != null ) {
	    for( int j = 0; j < chanAttrs.getLength(); j++ ) {
		Node chanAttr = chanAttrs.item(j);
		
		if( chanAttr.getNodeName().equalsIgnoreCase("name") ) {
		    chanName = chanAttr.getNodeValue();
		} else if( chanAttr.getNodeName().equalsIgnoreCase("flags") ) {
		    chanFlags = chanAttr.getNodeValue();
		}
	    }
	    /* name -attribute must exist */
	    if( (chanName == null) || (chanName.equals("")) ) {
		putlog("handleChannelTag(): no name -attribute in <channel> tag");
	    } else {
		UserChannelInfo channelInfo = new UserChannelInfo(chanName,chanFlags);
		if( !user.addChannelInfo(channelInfo) ) {
		    putlog("handleChannelTag(): channel "+chanName+" already defined for user "+user.getName());
		}				    
	    }
	}
    } 

    /**
     * go through the document and parse data
     * @param node current node
     */
    private void recursiveParse(Node node)
    {
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: 
                recursiveParse(((Document)node).getDocumentElement());
                break;
            case Node.ELEMENT_NODE: 
		/* handle <user> */
		if( node.getNodeName().equalsIgnoreCase("user") )
		    handleUserTag(node);

		/* recurse */
		NodeList children = node.getChildNodes();
		if( children != null ) {
		    for( int i = 0; i < children.getLength(); i++ ) {
			recursiveParse(children.item(i));
		    }
		} 
                break;
	    case Node.ENTITY_REFERENCE_NODE: 
		/* handle entity reference nodes */
                break;
            case Node.CDATA_SECTION_NODE: 
                /* print cdata sections */
                break;
            case Node.TEXT_NODE: 
                break;
            case Node.PROCESSING_INSTRUCTION_NODE: 
                break;
        }
	/* by now the recursive call is completed; handle end tags for ELEMENT_NODEs */
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
