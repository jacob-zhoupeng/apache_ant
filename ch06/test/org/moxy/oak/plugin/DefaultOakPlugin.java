package org.moxy.oak.plugin;
/**
 * This class is designed provide a quick way to create
 * an OakPlugin. It provides a default implementation
 * of the handleIRCMessage() method wich calls determins
 * the type of message recieved and places calls to the
 * appropiately named messages.
 * @since 1.0
 * @author Marcus Wenzel
 **/
/*/
 * Provides a default implementation of OakPlugin
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
public abstract class DefaultOakPlugin extends DefaultIRCListener implements OakPlugin {

	String identifier = null;
	IRCConnection connection = null;
	Oak bot = null;

    /**
     * Initilizes the plugin for full server operation, operation across
     * all channels. There will be a third param added later (IRCConnection).
     * @since 1.0
     * @param b the bot the plugin is loaded into.
     * @param params the parameters for the plugin.
     */
    public void initFullServerPlugin(Oak b, String[] params, String identifier, IRCConnection connection){
	this.identifier = identifier;
	this.connection = connection;
	this.bot = b;
    }

   public String getIdentifier(){return identifier;}
   public IRCConnection getConnection(){return connection;}

    /**
       * Called just before the plugin is removed from the system.
       * @since 1.0
       */
    public void destroy() {}
    /**
       * Used to configure the bot via the console.
       * Configuration via console is not implemented yet.
       * @since 1.0
       */
    public void configure() {}

}
