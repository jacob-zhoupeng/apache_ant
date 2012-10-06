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

import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

/* XML (dom) parser support */
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;

import irssibot.core.*;
import irssibot.util.*;
/**
 * configuration file parser
 *
 * @author Matti Dahlbom
 */
public class ConfigParser 
{
    /* public data */
    public Vector initialModules = null;

    /* private data */
    private boolean isOk = true;
    private Document document = null;

    private String dbHostName = null;
    private String dbHostPort = null;
    private String dbUserName = null;
    private String dbUserPassword = null;

    public String getDBHostName() { return dbHostName; }
    public String getDBHostPort() { return dbHostPort; }
    public String getDBUserName() { return dbUserName; }
    public String getDBUserPassword() { return dbUserPassword; }

    private String dateFormatString = null;

    public String getDateFormatString() { return dateFormatString; }
    
    private Vector serverInstances = null;

    /**
     * default constructor
     * merely read the config file from disk and store the document object
     */
    public ConfigParser()
    {
	serverInstances = new Vector();

	isOk = readConfigFile();
    }

    /**
     * @return instance data as a Vector
     */
    public Vector getInstanceData()
    {
	return serverInstances;
    }

    /**
     * invoke recursive parsing
     */
    public void parse()
    {
	if( (document != null) && isOk ) 
	    recursiveParse(document);
	else
	    putlog("document="+document+", isOk="+isOk);
    }

    /**
     * prints the whole XML document into a PrintStream. use System.out as argument
     * to print on std out / screen. used for debugging purposes and to 
     * write out the document as an XML file on disk.
     * @param out target printstream
     */
    public void print(PrintStream out)
    {
        if( (document != null) && (out != null) )
            printDOMTree(out,document);
    }

    /**
     * read config XML file from disk. document will hold the parsed 
     * config data.
     * @return true is successful
     */
    private boolean readConfigFile()
    {
	boolean ret = true;
	
        try {
            DOMParser parser = new DOMParser();
            parser.parse(Core.configFilePath);
            document = parser.getDocument();
        } catch( Exception e ) {
            putlog("Caught exception: "+e.getMessage());
            document = null;
	    ret = false;
        }
	return ret;
    }	

    /**
     * Reads database information 
     * 
     * @param node database node
     */
    private void handleDatabaseTag(Node node)
    {
	/* handle child nodes */
	NodeList children = node.getChildNodes();
	if (children != null) {
	    for( int i = 0; i < children.getLength(); i++ ) {
		Node child = children.item(i);
		
		/* handle <host> */
		if( child.getNodeName().equalsIgnoreCase("host") ) {
		    NamedNodeMap attrs = child.getAttributes();
		    for (int n = 0; n < attrs.getLength(); n++) {
			if( attrs.item(n).getNodeName().equalsIgnoreCase("hostname") ) {
			    dbHostName = attrs.item(n).getNodeValue();
			} else if( attrs.item(n).getNodeName().equalsIgnoreCase("hostport") ) {
			    dbHostPort = attrs.item(n).getNodeValue();
			}
		    } 
		} 
		
		/* handle <user> */
		if( child.getNodeName().equalsIgnoreCase("user") ) {
		    NamedNodeMap attrs = child.getAttributes();
		    for (int n = 0; n < attrs.getLength(); n++) {
			if( attrs.item(n).getNodeName().equalsIgnoreCase("name") ) {
			    dbUserName = attrs.item(n).getNodeValue();
			} else if( attrs.item(n).getNodeName().equalsIgnoreCase("password") ) {
			    dbUserPassword = attrs.item(n).getNodeValue();
			}
		    } 
		} 
	    }
	}
    }

    /**
     * Parse general information 
     * 
     * @param node server-instance node
     */
    private void handleGeneralTag(Node node)
    {    
	/* handle child nodes */
	NodeList children = node.getChildNodes();
	if (children != null) {
	    for( int i = 0; i < children.getLength(); i++ ) {
		Node child = children.item(i);
	
		/* handle <server-list> */
		if( child.getNodeName().equalsIgnoreCase("dateformat") ) {
		    dateFormatString = child.getChildNodes().item(0).getNodeValue();
		}
	    }
	}

	/* apply default values */
	if( dateFormatString == null || dateFormatString.equals("") ) {
	    dateFormatString = "dd'.'MM HH':'mm";
	}
    }
		    
    /**
     * Create and init a new serverinstance
     * 
     * @param node server-instance node
     */
    private void handleServerInstanceTag(Node node)
    {
	String network = null;
	String userFilePath = null;
	String botNick = null;
	String botAltNick = null;
	String realName = null;
	Vector serverList = null;
	Hashtable channels = null;

	/* read attributes */
	NamedNodeMap attrs = node.getAttributes();
	for( int i = 0; i < attrs.getLength(); i++ ) {
	    Node attr = attrs.item(i);	

	    if( attr.getNodeName().equalsIgnoreCase("network") ) {
		network = attr.getNodeValue();
	    }
	}

	/* handle child nodes */
	NodeList children = node.getChildNodes();
	if (children != null) {
	    for( int i = 0; i < children.getLength(); i++ ) {
		Node child = children.item(i);

		/* handle <server-list> */
		if( child.getNodeName().equalsIgnoreCase("server-list") ) {
		    serverList = XMLUtil.getListElements(child,"address");
		} 

		/* handle <user-file> */
		if( child.getNodeName().equalsIgnoreCase("user-file") ) {
		    /* this node has no children, but attributes */
		    NamedNodeMap userattrs = child.getAttributes();
		    for (int n = 0; n < userattrs.getLength(); n++) {
			if( userattrs.item(n).getNodeName().equalsIgnoreCase("path") ) {
			    userFilePath = userattrs.item(n).getNodeValue();
			}
		    }
		}

		/* handle <bot-info> */
		if( child.getNodeName().equalsIgnoreCase("bot-info") ) {
		    /* this node has no children, but attributes */
		    NamedNodeMap userattrs = child.getAttributes();
		    for (int n = 0; n < userattrs.getLength(); n++) {
			if( userattrs.item(n).getNodeName().equalsIgnoreCase("nick") ) {
			    botNick = userattrs.item(n).getNodeValue();
			}
			if( userattrs.item(n).getNodeName().equalsIgnoreCase("altnick") ) {
			    botAltNick = userattrs.item(n).getNodeValue();
			}
			if( userattrs.item(n).getNodeName().equalsIgnoreCase("realname") ) {
			    realName = userattrs.item(n).getNodeValue();
			}
		    }
		}

		/* handle <channel-list> */
		if( child.getNodeName().equalsIgnoreCase("channel-list") ) {
		    channels = handleChannelListTag(child,network);
		}
	    }
	}
    
	if( (network != null) && (botNick != null ) && 
	    (channels != null) && (channels.size() > 0) ) {
	    serverInstances.add(new ServerInstanceData(network,userFilePath,botNick,botAltNick,
						       realName,serverList,channels));
	}
    }

    /**
     * Add new channel to server instance
     *
     * @param node server-instance node
     * @return channel list as a Hashtable
     */
    private Hashtable handleChannelListTag(Node node,String instanceName)
    {
	Hashtable tmp = new Hashtable();
	String channelName = null;
	String channelKey = null;
	String forceModes = null;

	NodeList children = node.getChildNodes();
	if (children != null) {
	    for( int i = 0; i < children.getLength(); i++ ) {
		Node child = children.item(i);
		
		/* handle <channel> */
		if( child.getNodeName().equalsIgnoreCase("channel") ) {
		    /* handle attributes */
		    NamedNodeMap chanattrs = child.getAttributes();
		    for( int n = 0; n < chanattrs.getLength(); n++ ) {
			if( chanattrs.item(n).getNodeName().equalsIgnoreCase("name") ) {
			    channelName = chanattrs.item(n).getNodeValue();
			} else if( chanattrs.item(n).getNodeName().equalsIgnoreCase("forcedmodes") ) {
			    forceModes = chanattrs.item(n).getNodeValue();
			    if( forceModes.equals("") ) forceModes = null;
			} else if( chanattrs.item(n).getNodeName().equalsIgnoreCase("key") ) {
			    channelKey = chanattrs.item(n).getNodeValue();
			    if( channelKey.equals("") ) channelKey = null;
			}
		    }    

		    /* add channel to hash table if it does not exist there yet */
		    if( !tmp.containsKey(channelName) ) {
			tmp.put(channelName,new Channel(channelName,channelKey,forceModes,null));
		    } else {
			putlog("channel "+channelName+" already exists for instance "+instanceName);
		    }
		}
	    }
	}
	return tmp;
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
		/* handle <server-instance> */
		if( node.getNodeName().equalsIgnoreCase("server-instance") ) {
		    handleServerInstanceTag(node);
		} else if( node.getNodeName().equalsIgnoreCase("module-list") ) {
		    /* handle <module-list> */
		    initialModules = XMLUtil.getListElements(node,"module");
		} else if( node.getNodeName().equalsIgnoreCase("database") ) {
		    /* handle <database> */
		    handleDatabaseTag(node);
		} else if( node.getNodeName().equalsIgnoreCase("general") ) {
		    /* handle <database> */
		    handleGeneralTag(node);
		} 

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
    }
    
    /**
     * recursive traversing of the document tree, printing out 
     * nodes.
     * @param out target printstream
     * @param node current node
     */
    private void printDOMTree(PrintStream out,Node node) 
    {
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: 
                /* print the document element */
                out.println("<?xml version=\"1.0\" ?>");
                printDOMTree(out,((Document)node).getDocumentElement());
                break;
        
            case Node.ELEMENT_NODE: 
                /* print element with attributes */
                out.print("<");
                out.print(node.getNodeName());

                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
                }
                out.print(">");
                    
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++)
                        printDOMTree(out,children.item(i));
                }
                break;

              case Node.ENTITY_REFERENCE_NODE: 
                /* handle entity reference nodes */
                out.print("&");
                out.print(node.getNodeName());
                out.print(";");
                break;
                
            case Node.CDATA_SECTION_NODE: 
                /* print cdata sections */
                out.print("<![CDATA[");
                out.print(node.getNodeValue());
                out.print("]]>");
                break;

            case Node.TEXT_NODE: 
                /* print text */
                out.print(node.getNodeValue());
                break;
                                
            case Node.PROCESSING_INSTRUCTION_NODE: 
                /* print processing instruction */
                out.print("<?");
                out.print(node.getNodeName());
                String data = node.getNodeValue();
                out.print(" ");
                out.print(data);
                out.print("?>");
                break;
        }
        if (type == Node.ELEMENT_NODE) {
            out.print("</");
            out.print(node.getNodeName());
            out.print('>');
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






