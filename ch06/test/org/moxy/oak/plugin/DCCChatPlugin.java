package org.moxy.oak.plugin;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
import java.net.*;
public interface DCCChatPlugin{

 public String getDescription();
 public void init(Oak bot, DCCChatManager manager);
 public void newConnection(String fullNick, IRCConnection origin, Socket s);

}
