package com.cjw.ircclient;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import org.moxy.irc.*;

class ChatWindow extends JFrame implements ActionListener, KeyListener, ChangeListener
{
	Vector /* ChatPanel */ panels;
	JTextField inputLine;
	JTabbedPane pane;
	Client application;
	JLabel nickLabel;
	IRCPanel lastSelectedTab;

		ChatWindow(Client application)
	{
		JPanel bottomPanel;
//		JPanel sidePanel;
	
		this.application = application;
		setSize(600, 400);
		initJMenus();
		getContentPane().setLayout(new BorderLayout());
		panels = new Vector();
		lastSelectedTab = null;
		
		pane = new JTabbedPane();
		getContentPane().add(pane, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		nickLabel = new JLabel();
		bottomPanel.add(nickLabel, BorderLayout.WEST);
		
		inputLine = new JTextField(120);
		bottomPanel.add(inputLine, BorderLayout.CENTER);
		
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		inputLine.addKeyListener(this);
		pane.addChangeListener(this);
	}
	
	private void initJMenus()
	{
		JMenuBar menubar;
		JMenu menu;
		JMenuItem item;
		
		menubar = new JMenuBar();
		
		menu = new JMenu("File");
		item = new JMenuItem("New Connection...");
		item.addActionListener(this);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Close");
		item.addActionListener(this);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Preferences...");
		item.addActionListener(this);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Quit");
		item.addActionListener(this);
		menu.add(item);
		menubar.add(menu);
		
		menu = new JMenu("Edit");
		item = new JMenuItem("Undo");
		item.addActionListener(this);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Cut");
		item.addActionListener(this);
		menu.add(item);
		item = new JMenuItem("Copy");
		item.addActionListener(this);
		menu.add(item);
		item = new JMenuItem("Paste");
		item.addActionListener(this);
		menu.add(item);
		menubar.add(menu);
		
		this.setJMenuBar(menubar);
	}
	
	public void doConnect(String server, int port, String nick)
	{
		application.doConnect(server, port, nick, this);
	}
	
	public IRCPanel createPanel(String name)
	{
		return createPanel(name, null);
	}
	
	public IRCPanel createPanel(String name, IRCConnection connection)
	{
		IRCPanel panel;
		
		panel = new IRCPanel(connection, application, this);
		
		panels.addElement(panel);
		// add to tabbedpane
		pane.addTab(name, panel);
		
		return panel;
	}
	
	void setTabName(IRCPanel panel, String name)
	{
		int index;
		
		index = pane.indexOfComponent(panel);
		if (index < 0)
			return;
		
		pane.setTitleAt(index, name);
	}
	
	private void updateUserNick()
	{
		IRCPanel panel;
		
		panel = (IRCPanel)pane.getSelectedComponent();
		
		nickLabel.setText(panel.getNick());
		if (panel.isOpped())
			nickLabel.setIcon(new ImageIcon("green_dot.gif"));
		else if (panel.isVoiced())
			nickLabel.setIcon(new ImageIcon("yellow_dot.gif"));
		else
			nickLabel.setIcon(null);
	}
	
	void nickChanged(IRCPanel panel)
	{
		if (panel == pane.getSelectedComponent())
			updateUserNick();
	}
	
	public void stateChanged(ChangeEvent event)
	{
		int index;
	
		updateUserNick();
		
		index = pane.getSelectedIndex();
		pane.setForegroundAt(index, Color.black);
		
		if (lastSelectedTab != null)
			lastSelectedTab.resetHighlightState();
		lastSelectedTab = (IRCPanel)pane.getSelectedComponent();
	}
	
	public void titleChanged(IRCPanel panel)
	{
		String name;
		
		name = panel.getChannelName();
		if (name == null)
			setTabName(panel, "none");
		else
			setTabName(panel, name);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String command;
	
		command = event.getActionCommand();
		
		System.out.println("action: " + command);
		
		if (command.equals("New Connection..."))
			handleNewConnection();
		else if (command.equals("Close"))
			handleClose();
		else if (command.equals("Preferences..."))
			handlePreferences();
		else if (command.equals("Quit"))
			handleQuit();
	}
	
		
	public void keyTyped(KeyEvent evt)
	{
	
	}
	
	public void keyReleased(KeyEvent evt)
	{
	
	}
	
	public void keyPressed(KeyEvent evt)
	{
		String line;
	
		if (evt.getKeyCode() == KeyEvent.VK_ENTER)
		{
			line = inputLine.getText();
			if (line.equals(""))
				return;
			inputLine.setText("");
			((IRCPanel)pane.getSelectedComponent()).doInput(new IRCLine(line));
		}
	}
	
	
	void somethingHappened(IRCPanel target, int level)
	{
		int index;
		Color color;
		
		if (target == pane.getSelectedComponent())
			return;
		
		index = pane.indexOfComponent(target);
		if (index < 0)
			return;
		
		switch(level)
		{
			case 0:
				color = Color.black;
				break;
			case 100:
				color = Color.red;
				break;
			case 200:
				color = Color.blue;
				break;
			default:
				color = Color.black;
		}
		
		pane.setForegroundAt(index, color);
	}

	
	private void handleNewConnection()
	{
		(new ConnectDialog(this)).show();
	}

	private void handleClose()
	{

	}

	private void handlePreferences()
	{

	}

	private void handleQuit()
	{
		System.exit(0);
	}
}
