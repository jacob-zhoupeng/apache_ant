package org.moxy.oak.plugin;
/**
 * This is a sample oak plugin.
 * This is a simple 8 ball script that's heavy on the
 * negative side.
 * @version 1.0
 * @author Marcus Wenzel.
 */
/*/
 * A rudementary 8 ball script for Oak
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

import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public class EightBall extends DefaultOakPlugin{  //implements OakChannelPlugin {

    String[] replys = { "Yes", "No", "In your dreams.", "Ha you call that a question?",
    "Don't hold your breath.", "Worse things have been known to happen." };

    /**
     * This will
     * catch all the PRIVMSG's sent to the bot looking for anything
     * that starts with !8 and respond with a random saying.
     * @since 1.0
     */
    public void handlePRIVMSG(IRCConnection connection, IRCMessage message) {
        String s = message.getIRCLine().getRemaining();
        if (!s.startsWith("!8")) {
            return;
        }
        java.util.Enumeration enum = message.getIRCLine().toVector().elements();
        int x = (int)(Math.random() * replys.length);
        while (enum.hasMoreElements()) {
            s = (String) enum.nextElement();
            if (s.startsWith("#") || s.startsWith("&")) {
                connection.sendMsg(s, replys[x]);
                return;
            }
        }
    }

    /**
       * As defined in OakPlugin.
       */
    public void initFullServerPlugin(Oak b, String[] params) {
    }
    /**
       * As defined in OakPlugin.
       */
    public void initAsChannelPlugin(Oak b, String[] params, String channel) {
    }


    /**
       * As defined in OakPlugin.
       */
    public void destroy() {}
    /**
       * As defined in OakPlugin.
       */
    public void configure() {}





}
