//WARNING THIS FILE WAS NOT COMPILED
package org.moxy.oak.plugin;
import org.moxy.irc.*;
import org.moxy.oak.*;
public interface ChannelPlugin extends org.moxy.irc.ChannelListener{

 public void initChannelPlugin(Oak bot, String id, String[] args, String channel, String identifier);
 public IRCConnection getServer();
 public String getChannel();
 public String getIdentifier();
 public void destroy();
 public void configure();

}
