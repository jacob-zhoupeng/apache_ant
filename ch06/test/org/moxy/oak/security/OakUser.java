package org.moxy.oak.security;
import org.moxy.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;

/**
 * This class repersents a user logged into Oak.
 * This class is part of the BotSecurityManager interface.
 * @version 1.0
 * @author Marcus Wenzel
 */
/*/
 * Used by BotSecurityManagers to pass information about logged in users to Oak
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

//package org.moxy.oak.securitymanager;
public class OakUser {

    private CommandConnection connection; //server the nick's connected to.
    private String nick; //current irc nick
    private String uname; //bot username
    private Object key; // locking key;
    private boolean specialPowers = false;

    public OakUser(CommandConnection connection, String uname, String nick, Object key, boolean specialPowers) {
        this.nick = nick;
        this.uname = uname;
        this.key = key;
        this.connection = connection;
        this.specialPowers = specialPowers;
    }


    /**
       * @since 1.0
       * @param connection the IRCConnection the nick is on.
       * @param uname the username of the user.
       * @param nick the nick of the user.
       * @param key the key to change the nick by
       */
    public OakUser(CommandConnection connection, String uname, String nick, Object key) {
        this.nick = nick;
        this.uname = uname;
        this.key = key;
        this.connection = connection;
    }

    /**
       * @deprecated
       * Plugins can obtain a handle on the login manager the
       * key is to lock the plugin's from changing the nick and allowing
       * access to a remote user that shouldn't have it.
       * @param connection the IRCConnection of the user.
       * @param nick The full nick of the user.
       * @param userName the username of the user.
       */
    public OakUser(CommandConnection connection, String nick, String userName) {
        this.connection = connection;
        this.nick = nick;
        this.uname = userName;
        this.key = null;
    }


    /**
       * @since 1.0
       * @return The CommandConnection the user is on.
       */
    public CommandConnection getConnection() {
        return connection;
    }
    /**
       * Returns the nick of the user.
       * @since 1.0
       * @return the nick of the user.
       */
    public String getNick() {
        return nick;
    }
    /**
       * Returns the username of the user.
       * @since 1.0
       * @return The user name of the user.
       */
    public String getUserName() {
        return uname;
    }
    /**
       * Changes the nick for the user.
       * @sicne 1.0
       * @param nick the new nick.
       * @param key the key to change the nick with. This is done for security reasons.
       */
    public void setNick(String nick, Object key) {
        if (this.key == key) {
            this.nick = nick;
            return;
        }
        throw new IllegalArgumentException("Wrong key");
    }

    /**
       * Riped from BotUser ;o)
       * Matches the IRCConnection and the nick.
       * @param connection
       * @param nick
       * @return True if connection and nick equal the internally held connection and nick variables
       */
    public boolean match(CommandConnection connection, String nick) {
        return connection == this.connection && nick.equalsIgnoreCase(this.nick);
    }

    /**
       * @deprecated
       * CommandPlugs can get a handle on this class so the locking mechinism
       * should be used.
       * @param nick The new nick.
       */
    public void setNick(String nick) {
        if (nick.indexOf("!") > -1) {
            this.nick = nick;
            return;
        }
        this.nick = nick + this.nick.substring(this.nick.indexOf("!"));
    }

    public final boolean isOwner() {
        return specialPowers;
    }

}
