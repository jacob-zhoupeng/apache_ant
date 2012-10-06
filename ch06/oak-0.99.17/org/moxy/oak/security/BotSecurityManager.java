package org.moxy.oak.security;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.commandplug.*;
import org.moxy.oak.plugin.*;
/*/
 * BotSecurityManager.java
 * Oak, moxy's IRC bot
 *
 * Copyright © 2000  Christiaan Welvaart
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
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * written by Christiaan Welvaart (cjw)
 * welvaart@phys.uu.nl
 *
 * 05-06-2000: mw	added changePassword() method
 * 26-03-2000: mw	deprecated a method.
 * 09-03-2000: cjw	added more add/remove methods
 * 28-02-2000: mw	added the "dry" hasAccess() method to check for permissions
 *               	on a possibly disconnected network.
 * 25-02-2000: mw	added addUser(), removeUser() and setPrivilege()
 * 24-02-2000: mw	added init(Oak)
 * 19-02-2000: cjw	changed the hasAccess methods to never include server (can be obtained from
 *                	the connection, and removed the channel parameter to allow getting access
 *                	status for global privileges
 * 19-02-2000: cjw	added CommandConnection parameter to getUserNameFromNick
 * 19-02-2000: cjw	added CommandConnection parameter to nickChanged, changed nick parameter names
 * 18-02-2000: cjw	start, stolen from OakLoginManager.java
 *
/*/

/*
 * CommandConnection is the one the person is using and will alway verify
 * security for an IRCConnection.
 */

/**
 * An interface for bot security managers. These check privileges for nicks
 * when supplied with the right information.
 * @version 1.0
 * @author Christiaan Welvaart
 * @author Marcus Wenzel (moxy@moxy.org)
 */
//package org.moxy.oak.securitymanager;

public interface BotSecurityManager {

    /**
     * Called just after the default constructor to finish initilizing the bot.
     * If the security manager needs additional information to load from Oak
     * such as a database url it should use Oak's getProperty() method.
     * @since 1.0
     * @param bot The Oak instance that owns the manager.
     */
    public void init(Oak bot);

    /**
     * Checks access for a nick to a section of the bot for a possibly
     * non existent CommandConnection. This is used by the SM commands to check
     * a for a users access rights to a possibly disconnected network. Some of
     * the SM commands allow the user to be on one network and adminster another.
     *
     * @sinec 1.0
     * @param privilege the permission requested.
     * @param channel the channel of the permission or null if it doesn't pertain to a specific channel.
     * @param nick the nick of the user.
     * @param connection the  connection the user is currently on.
     * @param network the network the user is tring to administer.
     **/
    public boolean hasAccess(String nick, CommandConnection connection,
            String network, String channel, String privilege);

    /**
    * Looks up a global permission for a nick regarding some connection.
    * @since 1.0
    * @param privilage the privilage to check access to.
    * @param nick the full nick of the user.
    * @param hostConnection the connection the user is on.
    * @param targetConnection the connection the action will be executed on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String privilege);


    /**
    * Should loop up those securitys that don't have to do with a
    * IRCConnection such as the EXIT command. That command doesn't
    * have a specific target IRCConnection;
    * Looks up a global permission for a nick
    * @since 1.0
    * @param privilege the privilage to check access to.
    * @param nick the fullnick of the user.
    * @param connection the connection the user is on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection connection, String privilege);

    /**
    * Looks up a channel specific permission for a nick regarding some connection.
    * @since 1.0
    * @param privilage the privilage to check access to.
    * @param channel the channel the privilege is for or null if doesn't pertain to a channel.
    * @param nick the full nick of the user.
    * @param hostConnection the connection the user is on.
    * @param targetConnection the connection the action will be executed on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String channel, String privilege);

    /**
    * Attempts to log a user in.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param nick the nick
    * @param username the username used to login
    * @param password the password supplied with the username to check.
    * @return An OakUser repersenting the logged in user if the login
    *    was successfull or null if it failed
    */
    public OakUser login(CommandConnection connection, String nick, String userName, String password);

    /**
    * Logs a user out of the system.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param the nick of the user.
    */
    public void logout(CommandConnection connection, String nick);

    /**
    * Tests to see if a given nick is currently logged in.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param nick the nick of the user.
    * @return true if the user is logged in false otherwise.
    */
    public boolean isLoggedIn(CommandConnection connection, String nick);

    /**
    * Returns the user name for this nick, if logged in through this security manager.
    * If the nick is not registered, null will be returned.
    * @param connection the connection the nick is on
    * @nick the nick that is queried
    * @returns the first username wichthis nick is logged in
    */
    public String getUserNameFromNick(CommandConnection connection, String nick);

    /**
    * Reports a nick change to this security manager. If the user
    * is not logged in, this method doesn't do anything.
    * This method must be called on at least every nick change of
    * a nick that is logged in through this security manager.
    * It is adviced to report all nick changes to all active
    * security managers.
    * @since 1.0
    * @param connection the irc connection oldNick is on
    * @param oldNick the old nick.
    * @param newNick the new nick.
    */
    public void nickChanged(CommandConnection connection, String oldNick, String newNick);


    /**
     * Adds a user to the system with the specified username,
     * password, and nick mask with no privileges. If this call
     * fails it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user name of the new user.
     * @param password The password of the new user.
     * @param mask The mask of the new user.
     **/
    public void addUser(String user, String password, String mask) throws BotSecurityException;

    /**
     * Removes a user from the system. If this method should
     * fail for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to remove from the system.
     **/
    public void removeUser(String user) throws BotSecurityException;

    /**
     * Sets a global privilege for a user. If this method fails
     * for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to set the privilege for.
     * @param privilege The privilege to set.
     * @param setting True to turn set it False to unset it.
     * @param network The network the privilege is for or null if it's for all networks.
     **/
    public void setPrivilege(String user, String privilege, boolean setting,  String network) throws BotSecurityException;

    /**
     * Sets a specific privilege for a user. If this method fails
     * for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to set the privilege for.
     * @param privilege The privilege to set.
     * @param setting True to turn set it False to unset it.
     * @param network The network the privilege is for or null if it's for all networks.
     * @param channel The channel the privilege is for or null if it's for all channels.
     **/
    public void setPrivilege(String user, String privilege, boolean setting, String network, String channel) throws BotSecurityException;

    /**
     * Registers a network with a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The (short) name of the network.
     * @param description A description of the network.
     **/
    public void addNetwork(String name, String description) throws BotSecurityException;

    /**
     * Removes a network from a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * Also removes any servers and channels on this network that were registered.
     * Also removes any privileges for this network and any channels on it.
     * @param name The (short) name of the network that is to be removed.
     **/
    public void removeNetwork(String name) throws BotSecurityException;

    /**
     * Registers a channel on some network with a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The name of the channel to add.
     * @param network The (short) name of the network the channel is on.
     * @param description A description of the channel.
     **/
    public void addChannel(String name, String network,  String description) throws BotSecurityException;

    /**
     * Removes a channel from a security manager
     * Also removes all privileges that are related to this channel
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The name of the channel to remove.
     * @param network The (short) name of the network the channel is on.
     **/
    public void removeChannel(String name, String network) throws BotSecurityException;

    /**
     * Registers a server with a security manager
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param address The IP address (normally in DNS form) of the server
     * @param port The TCP port to connect to.
     * @param network The network this server belongs to.
     * @param description A description of this server.
     **/
    public void addServer(String address, String port, String network,  String description) throws BotSecurityException;

    /**
     * Removes a server from a security manager
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param address The IP address (normally in DNS form) of the server
     * @param port The TCP port to connect to.
     * @param network The network this server belongs to.
     **/
    public void removeServer(String address, String port, String network) throws BotSecurityException;

    /**
     * Changes the password for a user.
     **/
    public void changePassword(String nick, String user, String oldpassword, String newPassword, CommandConnection con);

}
