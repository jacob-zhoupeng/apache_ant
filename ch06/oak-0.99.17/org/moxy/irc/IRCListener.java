package org.moxy.irc;
/**
 * An IRCListener is an extension of IRCConnection. It's
 * methods are called for anything that an IRC server may
 * send.
 * @since 1.0
 * @author Marcus Wenzel
 */
/*/
 * Allows extensions to IRCConnection.
 * Copyright (C) 2000 Marcus Wenzel
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



public interface IRCListener {

    /**
     * Called for all incomming messages from the IRC server.
     * Use IRCMessage's getType() method to determine the type of
     * message.
     * @since 1.0
     * @param IRCConnection The connection the message orignated from.
     * @param IRCMessage The message recieved.
     */
    public void handleIRCMessage(IRCConnection connection, IRCMessage message);

    /**
     * Since there's no command to signify a connection has been established
     * in the IRC this method has been created for the implementing class
     * to be notified when the IRCConnection has established a connection
     * to the IRC server.
     * @since 1.0
     * @param connection the originating IRCConnection.
     */
    public void handleConnect(IRCConnection connection);

    /**
     * Since there's no command to sinify a disconnection from an IRC server
     * this method has been created for the implementing class to be notified
     * when the IRCConnection has lost communication with the IRC server.
     * @since 1.0
     * @param connection the originating IRCConnection.
     */
    public void handleDisconnect(IRCConnection connection);


}
