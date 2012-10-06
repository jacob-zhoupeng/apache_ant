package org.moxy.oak.plugin;
/**
 * Implementing classes can be loaded into Oak.
 * OakPlugin's recieve ALL the messages recieved
 * from one server. If the plugin you're going to
 * code only needs to listen for channel messages
 * implement OakChannelPlugin.<br>
 * FYI: The command to load an OakPlugin in Oak
 * is similar to:
 * <code>
 *  /ctcp BOTNICK LOAD PluginName
 * </code>
 * Other parameters can be defined but that's the
 * basic form.
 * @version 1.0
 * @author Marcus Wenzel
 */
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.irc.*;
import org.moxy.oak.security.*;
public interface OakPlugin extends IRCListener {

    /**
     * Initilizes the plugin for full server operation, operation across
     * all channels. There will be a third param added later (IRCConnection).
     * @since 1.0
     * @param b the bot the plugin is loaded into.
     * @param params the parameters for the plugin.
     */
    public void initFullServerPlugin(Oak b, String[] params, String identifier, IRCConnection connection);
 
    /**
     * Returns the identifier of the plugin
     */
    public String getIdentifier();

    /**
     * Returns the IRCConnection the plugin was loaded on.
     **/
    public IRCConnection getConnection();

    /**
     * Called just before the plugin is removed from the system.
     * @since 1.0
     */
    public void destroy();
    /**
     * Used to configure the bot via the console.
     * Configuration via console is not implemented yet.
     * @since 1.0
     */
    public void configure();

}
