/*
 * $Id: ModeQueueElement.java,v 1.1 2001/03/26 22:29:41 matti Exp $
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

/**
 * This class represents an element in a mode queue for a server connection.
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.1 $
 *
 * @see irssibot.core.ModeQueue
 */
public class ModeQueueElement 
{
    /**
     * Priority value by which the mode is inserted into the queue.
     */
    private int priority = 0;
    /**
     * Mode change as int constant. The values are specified as symbolic constants
     * in class irssibot.core.Irc.
     * 
     * @see irssibot.core.Irc
     */
    private int mode = 0;
    /**
     * Target of mode. This depends on the mode; for o and v, for example,
     * this is a nick. For b,e,I this is a hostmask etc.
     */
    private String target = null;
    /**
     * Name of channel to apply this mode on. 
     */
    private String channelName = null;

    public int getMode() { return mode; }
    public int getPriority() { return priority; }
    public String getTarget() { return target; }
    public String getChannelName() { return channelName; }

    public ModeQueueElement(int mode,int priority,String target,String channelName)
    {
	this.mode = mode;
	this.priority = priority;
	this.target = target;
	this.channelName = channelName;
    }

    /**
     * Modes with IMMEDIATE priority will be not be queued, but instantly
     * written to server.
     */
    public static final int PRIORITY_IMMEDIATE = 999;
    /**
     * Normal priority of modes.
     */
    public static final int PRIORITY_NORMAL = 10;
}














