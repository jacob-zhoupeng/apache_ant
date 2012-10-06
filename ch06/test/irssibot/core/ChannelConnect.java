/*
 * $Id: ChannelConnect.java,v 1.2 2001/04/10 19:41:40 matti Exp $
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
 */
package irssibot.core;

/**
 * Represents a unidirectional 'connection' between two IRC channels
 * the bot forwards things said on source channel to the destination channel
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.2 $
 */
public class ChannelConnect
{
    public String sourceChannel = null;

    public ServerConnection destinationNetwork = null;
    public String destinationChannel = null;

    public ChannelConnect(String sourceChannel,ServerConnection destinationNetwork,String destinationChannel)
    {
	this.sourceChannel = sourceChannel;
	this.destinationNetwork = destinationNetwork;
	this.destinationChannel = destinationChannel;
    }
}
