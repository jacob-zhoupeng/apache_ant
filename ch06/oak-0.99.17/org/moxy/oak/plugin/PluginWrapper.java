package org.moxy.oak.plugin;
/**
 * A wrapper for plugins to store some exta information about them.
 * This class is strictly for internal use only. Probabbly should
 * change it to protected.
 * @version 1.0
 * @author Marcus Wenzel
 */
/*/
 * Provides extra data to OakPluginManager about a plugin.
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

import org.moxy.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class PluginWrapper {

    private Object plugin = null;
    private String room = null;
    private IRCConnection server = null;

    //Maximum number of plugins that can be loaded are 2,147,483,646
    // that's one less than Integer.MAX_VAL 'cause it's not zero based ;o)
    private int myIdentifier;
    static int sidentifier = 1;//server identifiers
    static int cidentifier = 1;//channel identifier


    /**
     * Makes a channel plugin wrapper.
     * @since 1.0
     * @param plugin the plugin to wrap.
     * @param server the server the plugin was loaded on.
     * @param room the channel the plugin was loaded on.
     */
    public PluginWrapper(Object plugin, IRCConnection server, String room) {
        this.plugin = plugin;
        this.server = server;
        this.room = room;
        if(room == null)
         myIdentifier = sidentifier++;//int identifier and set it as it's own
        else
         myIdentifier = cidentifier++;
    }
    /**
       * Makes a server plugin wrapper
       * @since 1.0
       * @param plugin the plugin to wrap
       * @param server the server the plugin was loaded on.
       */
    public PluginWrapper(OakPlugin plugin, IRCConnection server) {
        this(plugin, server, null);
    }
    /**
       * Returns the plugin.
       * @since 1.0
       * @return The plugin.
       */
    public Object getPlugin() {
        return plugin;
    }
    /**
       * Returns teh server identifier.
       * @since 1.0
       * @return The server identifier.
       */
    public IRCConnection getServer() {
        return server;
    }
    /**
       * Returns the channel.
       * @since 1.0
       * return the channel or null if it's a server plugin
       */
    public String getChannel() {
        return room;
    }

    public int getIdentifier(){return myIdentifier;}

}
