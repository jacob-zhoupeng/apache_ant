package org.moxy.oak.plugin;
/**
 * This is a sample oak plugin.
 * This plugin looks for "baaa" to be said on
 * the #java channel and will respond with a "baaaa".
 * It won't respond if the word
 * sheep is anywhere on the same line, even in the
 * nick.
 * Not a real great example plugin but it was our
 * first. Can be loaded as an OakPlugin but won't work
 * as one.
 * @version 1.0
 * @author Marcus Wenzel.
 */
/*/
 * A simple yet fun plugin for Oak.
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
public class First extends DefaultOakPlugin{


    /**
     * This will catch all the PRIVMSG's sent to the bot, weeds out anything
     * that's got the string sheep anywhere in it or not from #java.
     * Looks to see if there's an index of the string baaa and replys
     * baaa if there is.
     * @since 1.0
     */
    public void handlePRIVMSG(IRCConnection connection, IRCMessage message) {
        if (message.getSender().indexOf("sheep") > -1) {
            return;
        }
        if (message.getTarget().equals("#java") && message.getIRCLine().getRemaining().indexOf("baaa") > -1){
            connection.send("PRIVMSG #java :baaaa");
        }
    }


    /**
       * As defined in OakPlugin.
       */
    public void destroy() { }
    /**
       * As defined in OakPlugin.
       */
    public void configure() {}





}
