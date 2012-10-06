package org.moxy.irc;
/**
 * This class is designed provide a quick way to create
 * an IRCListener. It provides a default implementation
 * of the handleIRCMessage() method wich calls determins
 * the type of message recieved and places calls to the
 * appropiately named messages.
 * @since 1.0
 * @author Marcus Wenzel
 **/
/*/
 * Provides a default implementation of IRCListener
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

public abstract class DefaultIRCListener implements IRCListener {
    /**
     * Called for all incomming messages from the IRC server.
     * This analyzes the IRCMessage looking for some commonly handled
     * commands calling specific methods.
     * @since 1.0
     * @param IRCConnection The connection the message orignated from.
     * @param IRCMessage The message recieved.
     */

    public void handleIRCMessage(IRCConnection connection, IRCMessage message)
	{
		switch(message.getMsgType())
		{
        	case IRCMessage.MSG_JOIN:
            	handleJOIN(connection, message.getSender(), message.getTarget());
            	break;
        	case IRCMessage.MSG_PRIVMSG:
            	handlePRIVMSG(connection, message.getSender(), message.getTarget(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_CTCP:
            	handleCTCPMSG(connection, message.getSender(), message.getTarget(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_PART:
            	handlePART(connection, message.getSender(), message.getTarget(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_QUIT:
            	handleQUIT(connection, message.getSender(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_NICK:
            	handleNICK(connection, message.getSender(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_KICK:
            	handleKICK(connection, message.getSender(), message.getTarget(), message.getParam(2), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_NOTICE:
            	handleNOTICE(connection, message.getSender(), message.getTarget(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_TOPIC:
            	handleTOPIC(connection, message.getSender(), message.getTarget(), message.getIRCLine().getRemaining());
            	break;
        	case IRCMessage.MSG_INVITE:
            	handleINVITE(connection, message.getSender(), message.getTarget(), message.getParam(2));
            	break;
        	case IRCMessage.MSG_MODE:
				String modes;
				String sender = IRCMessage.getNick(message.getSender());
	 			String target = message.getTarget();
	  			try
				{
					modes = message.getParam(2);
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					modes = target;
					target = sender;
				}
	  			int params = 3;
	  			char curMode;
	  			boolean pm = false;//default for channel modes
	  			for(int x = 0; x < modes.length(); x++)
				{
					curMode = modes.charAt(x);
					if(curMode == '+')
					{
						pm = true;
						continue;
					}
					if(curMode == '-')
					{
						pm = false;
						continue;
					}
					switch(curMode)
					{
						case 'l':
						case 'k':
							if(!pm)
								break;
						case 'o': 
						case 'v': 
							handleMODE(connection, sender, target, curMode, pm, message.getParam(params++));
							break;
						default:
							handleMODE(connection, sender, target, curMode, pm, null);
					}
	  			}
            	break;
        	default:
	        	handleOther(connection, message);
		}
    }
    protected void handleOther(IRCConnection connection, IRCMessage message) {}
    protected void handleJOIN(IRCConnection connection, String nick, String channel) {}
    protected void handlePRIVMSG(IRCConnection connection, String sender, String target, String msg) {}
    protected void handleCTCPMSG(IRCConnection connection, String sender, String target, String msg) {}
    protected void handlePART(IRCConnection connection, String nick, String channel, String msg) {}
    protected void handleQUIT(IRCConnection connection, String nick, String msg) {}
    protected void handleNICK(IRCConnection connection, String nick, String newName) {}
    protected void handleNOTICE(IRCConnection connection, String sender, String target, String msg) {}
    protected void handleTOPIC(IRCConnection connection, String oper, String channel, String topic) {}
    protected void handleINVITE(IRCConnection connection, String sender, String nick, String channel) {}
    protected void handleMODE(IRCConnection connection, String moder, String target, char mode, boolean on, String param) {}
    protected void handleKICK(IRCConnection connection, String kicker, String channel, String nick, String msg) {}

    /**
       * Since there's no command to signify a connection has been established
       * in the IRC this method has been created for the implementing class
       * to be notified when the IRCConnection has established a connection
       * to the IRC server.
       * @since 1.0
       * @param connection the originating IRCConnection.
       */
    public void handleConnect(IRCConnection connection) {}

    /**
       * Since there's no command to sinify a disconnection from an IRC server
       * this method has been created for the implementing class to be notified
       * when the IRCConnection has lost communication with the IRC server.
       * @since 1.0
       * @param connection the originating IRCConnection.
       */
    public void handleDisconnect(IRCConnection connection) {}


}
