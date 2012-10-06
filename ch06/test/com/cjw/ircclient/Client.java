package com.cjw.ircclient;

import java.util.Vector;
import java.util.Hashtable;
import org.moxy.irc.*;

class Client implements ConsoleListener
{
	Vector /* <IRCConnection> */ connections;
	Vector /* <IRCPanel> */ consoles;
	Hashtable joinListeners;
//	IRCPanel console;
	ConnectionsWindow connectionsWindow;

		Client()
	{
		ChatWindow window;
	
		connections = new Vector();
		consoles = new Vector();
		joinListeners = new Hashtable();
	
		window = new ChatWindow(this);
		window.show();		
	}
	
	public void doConnect(String server, int port, String nick, ChatWindow window)
	{
		IRCConnection connection;
		IRCPanel console;
		
		connection = new IRCConnection(server, port, nick, "KIRK user");
		if (connection == null)
			return;
		
		connections.addElement(connection);
		connection.registerConsoleListener(this);
		console = window.createPanel(connection.getServer(), connection);
		consoles.addElement(console);
		
		try
		{
			connection.connect();
		}
		catch (Exception e)
		{
			// do something
		}
	}
	
	public void doJoin(String channel, IRCConnection connection, IRCPanel window)
	{
		if (connection == null)
			connection = (IRCConnection)connections.elementAt(0);
	
		connection.joinChannel(channel);
		joinListeners.put(channel, window);
	}
	
	public void handleConsoleMsg(IRCMessage message)
	{
		int i;
		StringBuffer buffer;
		IRCLine line;
		IRCPanel console;
		int index;

		index = connections.indexOf(message.getConnection());
		if (index < 0)
			console = null;
		else
			console = (IRCPanel)consoles.elementAt(index);
		
		line = message.getIRCLine();
	
		switch(message.getMsgType())
		{
			
			
			case IRCMessage.MSG_JOIN:
				IRCPanel panel;
				IRCPanel window;
				String channel;
				IRCConnection connection;
				
				System.out.println("Client::handleConsoleMsg called with JOIN");
				
				channel = message.getTarget();
				connection = message.getConnection();
				window = (IRCPanel)joinListeners.get(channel);
				if (window == null)
				{
					System.out.println("Client::handleConsoleMessage: JOIN with unknown channel: " + channel);
					break;
				}
				if (window.isTaken())
					panel = window.getParentWindow().createPanel(channel, connection);
				else
				{
					panel = window;
				}
				connection.registerChannelListener(channel, panel);
				panel.setChannelName(channel);
				break;
			case IRCMessage.MSG_NOTICE:
				console.showString("--- " + message.getIRCLine().getRemaining());
				break;
			default:
				buffer = new StringBuffer();
				buffer.append(message.getType() + " ");
				for (i = 0; i < message.getParamCount(); i++)
					buffer.append((String)message.getParam(i) + " ");
				buffer.append(message.getIRCLine().getRemaining());
				console.showString(buffer.toString());
		}
	}

}
