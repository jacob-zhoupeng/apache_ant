/*/
 * An interface for objects listening to what happens in an IRC channel.
 *  Copyright (C) 2000  Christiaan Welvaart
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.moxy.irc;

import java.util.Vector;

/**
 * An interface for objects that want to receive all messages and administrative changes
 * for a particular channels (or a number of channels). Registering is done in IRCConnection.
 * 
 * @author: Christiaan Welvaart
 * Date: 20-04-2000
 */

public interface ChannelListener{

	/**
	 * This method is called before any other method of this channellistener. 
	 * @param connection the IRCConnection this channel is attached to.
	 */
	public void init(IRCConnection connection);


	/**
	 * Sets the name list with a space delimited string and @
	 * used to designate ops and + to designate voices before the nicks.
	 * @since 1.0
	 * @param nicks a Vector of ListNick objects that represents the 
	 *        users in the channel while joining, including this 
	 *        IRC client.
	 */
	public void initialNickList(Vector /* ListNick */ nicks);
	
	/**
	 * Sets the topic for the channel on join.
	 * @param topic the initial topic
	 */
	public void initialTopic(String topic);
	
	/**
	 * Sets "topic can only bet set by chanops" for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialOpTopic(boolean mode);
	
	/**
	 * Sets "people not on this channel can't send messages to it" for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialNoExtMsg(boolean mode);
	
	/**
	 * Sets secret mode for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialSecret(boolean mode);

	/**
	 * Sets invite only mode for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialInviteOnly(boolean mode);

	/**
	 * Sets private mode for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialPrivate(boolean mode);
	
	/**
	 * Sets moderated mode for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialModerated(boolean mode);
	
	/**
	 * Sets user limit for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialLimit(boolean mode, int limit);
	
	/**
	 * Sets join key for the channel on join.
	 * @param mode true=turn channelmode on, false=turn off
	 */
	public void initialKey(boolean modek, String key);
	
	/**
	 * Sets ban list for the channel on join.
	 * @param masks a Vector of Strings representing the list of banmasks
	 */
	public void initialBan(Vector /* String */ masks);



	/**
	 * Sets wether the room is invite only or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setInviteOnly(boolean b, String chanop);

	/**
	 * Sets wether the room is private or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setPrivate(boolean b, String chanop);

	/**
	 * Sets wether the room is secret or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setSecret(boolean b, String chanop);

	/**
	 * Sets wether the room is moderated or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setModerated(boolean b, String chanop);

	/**
	 * Sets wether external messages are allowed or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setNoExtMsg(boolean b, String chanop);

	/**
	 * Sets wether only ops can change the topic or not.
	 * @since 1.0
	 * @param b true to set false to unset.
	 * @param chanop the person or server setting this mode
	 */
    public void setOpTopic(boolean b, String chanop);

	/**
	 * Sets the value of the key for the room.
	 * When the key is removed, the key parameter will be null.
	 * @since 1.0
	 * @param k the key.
	 * @param chanop the person or server setting this key
	 **/
    public void setKey(String key, String chanop);

	/**
	 * Sets the limit of the number of nicks in the channel
	 * at one time. Limit = 0 means user limit is off.
	 * @since 1.0
	 * @param l max the number of nicks.
	 * @param chanop the person or server setting this limit
	 */
    public void setLimit(int limit, String chanop);

	/**
	 * Adds a nickmask to the ban list of the channel.
	 * @since 1.0
	 * @param mask the nick mask.
	 * @param chanop the person or server doing this ban
	 */
    public void ban(String mask, boolean mode, String chanop);

	/**
	 * Sets a specified mode.
	 * @since 1.0
	 * @param mode the mode to set.
	 * @param type true to set it on or false to set it off.
	 * @param chanop the person or server setting this mode
	 */
    public void setOtherMode(char mode, boolean type, String chanop);

	/**
	 * Sets the topic for the room.
	 * @param topic the new topic
	 * @param chanop the person or server setting this topic
	 **/
    public void setTopic(String topic, String chanop);

	/**
	 * Sets the URL for the channel.
	 * @since 1.0
	 * @param url the url of the channel.
	 */
//    public void setUrl(String url);
 
	/**
	 * Adds a nick to the channel.
	 * @since 1.0
	 * @param name the nick of the person joining.
	 * @param ident the ident name of the person.
	 * @param host the hostname of the person
	 */
    public void join(String name, String ident, String host);

	/**
	 * Removes a nick from the channel.
	 * @since 1.0
	 * @param name the nick of the person parting.
	 * @param ident the ident name of the person.
	 * @param host the hostname of the person.
	 * @msg the part message (can be empty)
	 */
    public void part(String name, String ident, String host, String msg);

	/**
	 * Removes a nick from the channel.
	 * @since 1.0
	 * @param name the nick of the person quitting.
	 * @param ident the ident name of the person.
	 * @param host the hostname of the person.
	 * @msg the quit message (can be empty)
	 */
    public void quit(String name, String ident, String host, String msg);

	/**
	 * Changes the nick of a nick.
	 * @since 1.0
	 * @param oldName the old nick.
	 * @param newName the new nick.
	 */
    public void nickChange(String oldName, String newName);

	/**
	 * Removes a nick from the channel.
	 * @since 1.0
	 * @param name the nick being kicked.
	 * @param reason the kick reason.
	 * @param chanop the person or server doing this kick.
	 */
    public void kick(String name, String reason, String chanop);

	/**
	 * Ops or Deops a nick in the channel.
	 * @since 1.0
	 * @param name the nick to op or deop.
	 * @param mode true to op false to deop.
	 * @param chanop the person or server doing this op/deop.
	 */
    public void op(String name, boolean mode, String chanop);

	/**
	 * Voices or devoices a nick in the channel.
	 * @since 1.0
	 * @param name the nick to voice or devoice.
	 * @param mode true to voice false to devoice.
	 * @param chanop the person or server doing this voice/devoice.
	 */
    public void voice(String name, boolean mode, String chanop);

	/**
	 * Signifies a regular message sent to this channel.
	 * @param sender the person or server sending this message
	 * @param message the message content
	 */
	public void handleMessage(String sender, IRCLine message);
	
	/**
	 * Signifies an action message sent to this channel.
	 * @param sender the person or server sending this message
	 * @param message the action content
	 */
	public void handleAction(String sender, IRCLine message);


}
