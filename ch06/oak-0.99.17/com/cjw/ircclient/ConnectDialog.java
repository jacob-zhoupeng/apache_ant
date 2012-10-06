package com.cjw.ircclient;

import java.awt.*;

class ConnectDialog extends Frame
{
	ChatWindow application;
	TextField serverField, portField, nickField;
	Button connectButton, cancelButton;

		ConnectDialog(ChatWindow application)
	{

		this.application = application;
		
		setLayout(new GridLayout(5, 2));
		add(new Label("server:"));
		serverField = new TextField();
		add(serverField);
		serverField.setText("lineone.dal.net");
		add(new Label("port:"));
		portField = new TextField();
		add(portField);
		portField.setText("6668");
		add(new Label("nick:"));
		nickField = new TextField();
		add(nickField);
		nickField.setText("MacSheep");

		connectButton = new Button("Connect");
		add(connectButton);
		cancelButton = new Button("Cancel");
		add(cancelButton);
		setSize(300, 200);
	}
	
	
	public boolean action(Event evt, Object param)
	{
		if (evt.target instanceof Button)
		{
			if (evt.target == connectButton)
				{
					String server, nick;
					int port;
					
					server = serverField.getText();
					nick = nickField.getText();
					port = Integer.parseInt(portField.getText());
					if (server.equals("") || nick.equals("") || port == 0)
					{
						// not much to do here :)
					}
					else
					{
						application.doConnect(server, port, nick);
						hide();
						dispose();
					}
					return true;
				}
			if (evt.target == cancelButton)
			{
				dispose();
				return true;
			}
		}
		return false;
	}

}
