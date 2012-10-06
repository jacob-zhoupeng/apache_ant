package org.moxy.irc;

/**
* Oak outgoing listner
* This listener is designed to catch the messages that the bot
* sends automatcially to the server. This is used for UI purposes,
* and makes it more conveniant to the turning of OAK into a full IRC client
*
* @Author Philip Bettinson
* @version 0.1
**/

 public interface OutgoingListener
 {
      /**
      * Called for all outgoing messages from the Program.
      * NOTE: IF you are using this to write a UI, then
      * PLEASE use this to show ALL text from the Program,
      * otherwise you will find yourself being repeated.
      *
      * @Author Philip Bettinson
      **/
      public void handleOutgoingMessage(IRCConnection connection, IRCMessage message);
 
 } 
