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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
/**
 * This class represents a mode queue for a server connection.
 *
 * @author Matti Dahlbom
 * @see irssibot.core.ModeQueueElement
 */
public class ModeQueue extends Thread
{
    private static final long modeFlushInterval = 1000;

    private ServerConnection connection = null;
    /**
     * A hashtable containing Vector objects as mode queues. Channel
     * name Strings are used as keys.
     */
    private Hashtable queueList = null;
    private boolean alive = true;

    public ModeQueue(ServerConnection connection) 
    {
	super("mode queue");
	this.connection = connection;
	queueList = new Hashtable();
    }

    synchronized public void pushMode(ModeQueueElement element) 
    {
	int index = 0;
	ModeQueueElement cur = null;
	Vector queue = (Vector)queueList.get(element.getChannelName());

	putlog("pushMode(): ");

	/* if no queue, create new one */
	if( queue == null ) {
	    queue = new Vector();
	    queueList.put(element.getChannelName(),queue);
	}

	if( element.getPriority() == ModeQueueElement.PRIORITY_IMMEDIATE ) {
	    //send this one mode 
	} else {
	    /* insert mode to queue by its priority */
	    for( index = 0; index < queue.size(); index++ ) { 
		cur = (ModeQueueElement)queue.elementAt(index);
		if( element.getPriority() >= cur.getPriority() ) {
		    break;
		}
	    }
	}
	queue.insertElementAt(element,index);
    }

    /**
     * Flushes the mode queue to the server
     *
     */
    private void doModeFlush(String channelName,Vector queue)
    {
	String opModeList = "";
	String opTargetList = "";
	boolean opPolarity = true;
	int opModes = 0;
	String banModeList = "";
	String banTargetList = "";
	boolean banPolarity = true;
	int banModes = 0;

	putlog("doModeFlush(): "+channelName+" "+queue.size());

	for( int i = 0; i < queue.size(); i++ ) {
	    ModeQueueElement element = (ModeQueueElement)queue.elementAt(i);
	    switch( element.getMode() ) {
	    case Irc.MODE_OP: 
		if( !opPolarity ) {
		    opPolarity = true;
		    opModeList += "+";
		} else if( opModes == 0 ) {
		    opModeList = "+";
		}
		opModeList += "o";
		opTargetList += element.getTarget()+" ";
		opModes++;
		break;
	    case Irc.MODE_DEOP: 
		if( opPolarity ) {
		    opPolarity = false;
		    opModeList += "-";
		} else if( opModes == 0 ) {
		    opModeList = "-";
		}
		opModeList += "o";
		opTargetList += element.getTarget()+" ";
		opModes++;
		break;
	    case Irc.MODE_VOICE: 
		if( !opPolarity ) {
		    opPolarity = true;
		    opModeList += "+";
		} else if( opModes == 0 ) {
		    opModeList = "+";
		}
		opModeList += "v";
		opTargetList += element.getTarget()+" ";
		opModes++;
		break;
	    case Irc.MODE_DEVOICE: 
		if( opPolarity ) {
		    opPolarity = false;
		    opModeList += "-";
		} else if( opModes == 0 ) {
		    opModeList = "-";
		}
		opModeList += "v";
		opTargetList += element.getTarget()+" ";
		opModes++;
		break;
	    case Irc.MODE_BAN: 
		if( !banPolarity ) {
		    banPolarity = false;
		    banModeList += "+";
		} else if( banModes == 0 ) {
		    banModeList = "+";
		}
		banModeList += "b";
		banTargetList += element.getTarget()+" ";
		banModes++;
		break;
	    case Irc.MODE_UNBAN: 
		if( banPolarity ) {
		    banPolarity = false;
		    banModeList += "-";
		} else if( banModes == 0 ) {
		    banModeList = "-";
		}
		banModeList += "b";
		banTargetList += element.getTarget()+" ";
		banModes++;
		break;
	    default:
		break;
	    }

	    /* push max 3 modes at a time */
	    if( opModes == 3 ) {
		connection.write("MODE "+channelName+" "+opModeList+" "+opTargetList.trim()+"\n");
		opModes = 0;
		opPolarity = true;
		opModeList = "";
		opTargetList = "";
	    }
	    if( banModes == 3 ) {
		connection.write("MODE "+channelName+" "+banModeList+" "+banTargetList.trim()+"\n");
		banModes = 0;
		banPolarity = true;
		banModeList = "";
		banTargetList = "";
	    }
	}
	if( opModes > 0 ) {
	    connection.write("MODE "+channelName+" "+opModeList+" "+opTargetList.trim()+"\n");
	}
	if( banModes > 0 ) {
	    connection.write("MODE "+channelName+" "+banModeList+" "+banTargetList.trim()+"\n");
	}

	/* empty queue */
	queue.clear();
    }

    /**
     * After certain interval flushes the mode queue to server
     *
     */
    public void run()
    {
	Enumeration keys = null;
	String key = null;
	Vector queue = null;	    
	
	putlog("run(): starting..");
	while( alive ) {
	    try {
		sleep(modeFlushInterval);
	    } catch( InterruptedException e ) {}
	    
	    keys = queueList.keys();
	    while( keys.hasMoreElements() ) {
		key = (String)keys.nextElement();
		doModeFlush(key,(Vector)queueList.get(key));
		queueList.remove(key);
	    }
	}
	
	putlog("run(): thread mode queue exiting..");
    }

    public void kill()
    {
	alive = false;
    }

    /** 
     * log printing
     */
    private void putlog(String msg)
    {
	String logMsg = getClass().getName()+": "+msg+"\n";
	System.out.print(logMsg);
    }
}









