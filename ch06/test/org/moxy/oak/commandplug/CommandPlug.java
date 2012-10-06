/**
 * Shows what a commandplug should look like.
 **/
package org.moxy.oak.commandplug;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.security.*;
public interface CommandPlug{

 /**
  * set's the bot, security manager, and OakCommand variables
  * if the plug uses them.
  * @since 1.0
  */
 public void init(Oak b, BotSecurityManager bsm, OakCommand oc);
 /**
  * Returns all the commands this commandplug handles.
  * @since 1.0
  */
 public String[] getCommands();
 /**
  * Tells the commandplug to handle the command.
  * @since 1.0
  */
 public void doCommand(CommandConnection connection, String fromLongNick, 
			String command, String params);
 /**
  * Returns the help group this plugin belongs to.
  * @since 1.0
  */
 public String getGroup();
 /**
  * Returns a one line description of the specified command.
  * @since 1.0
  **/
 public String getShortHelpDescription(String command);
 /**
  * Returns a more indepth help about how to use the specified command.
  * @since 1.0
  **/
 public String[] getLongHelpDescription(String command);
}
