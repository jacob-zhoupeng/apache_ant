package org.moxy.oak.plugin;
import org.moxy.oak.*;
import org.moxy.irc.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;
public interface PartyLine2Client{
 public String getName(); //returns the name of this client nick or channel name
 public void sendMessage(int type, String nick, String message);//sends a message
}
