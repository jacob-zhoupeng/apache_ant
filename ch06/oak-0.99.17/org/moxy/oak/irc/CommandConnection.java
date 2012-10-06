package org.moxy.oak.irc;
public interface CommandConnection {

 /*
   Sends a reply to a command
   IRCConnection would use a ctcpReply
   telnet or DCC would use a private message
 */
 //used by MANY MANY files.
 public void sendReply(String lnick, String message);

}
