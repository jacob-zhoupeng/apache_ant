package com.cjw.ircclient;

import java.awt.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.*;
import org.moxy.irc.*;

class IRCPanel extends JPanel implements ChannelListener
{
	IRCConnection connection;
	Client application;
	ChatWindow window;
	String channel = null;
	JTextArea textarea;
	JScrollPane pane;
	JList nicklist;
	int highlightLevel;
	JButton topic;
	
	boolean hasNewMessage;
	
	static final public int CMD_JOIN = 1;
	static final public int CMD_ME = 2;
	static final public int CMD_PART = 3;
	static final public int CMD_PRIVMSG = 4;
	static final public int CMD_NICK = 5;
	
	static final private String[] commandStrings =
		{
			"JOIN", "ME", "PART", "MSG", "NICK"
		};
	
	static final private Hashtable commandHasher;
	
	static
		{
			int i;
			
			commandHasher = new Hashtable();
			for (i = 0; i < commandStrings.length; i++)
				commandHasher.put(commandStrings[i], new Integer(i + 1));
		}
	
	static private int getID(String command)
	{
		return ((Integer)commandHasher.get(command.toUpperCase())).intValue();
	}
	
	String getNick()
	{
		if (connection ==  null)
			return "";
		else
			return connection.getNick();
	}
	
	ListNick[] getNickList()
	{
		if (connection == null || channel == null)
			return null;
		return connection.getChannel(channel).getNickList();
	}
	
	ChatWindow getParentWindow()
	{
		return window;
	}
	
	boolean isTaken()
	{
		return channel != null;
	}
	
	private void updateNickList()
	{
		ListNick[] nicks;
		
		nicks = getNickList();
		if (nicks == null)
			return;
		sun.misc.Sort.quicksort(nicks, new ListNick(null));
		nicklist.setListData(nicks);
	}
	
		IRCPanel(IRCConnection connection, Client application, ChatWindow window)
	{
		JSplitPane splitter;
		JPanel sidePanel;
		JScrollPane nickScroller;
		
		this.connection = connection;
		this.application = application;
		this.window = window;
		
		highlightLevel = 0;
		
		// top level: a split pane that divides the text area ant the nicklist panel
		setLayout(new BorderLayout());
		
		topic = new JButton();
		add(topic, BorderLayout.NORTH);
		
		
	
		// the text area itself
		textarea = new JTextArea();
		textarea.setEditable(false);
		textarea.setLineWrap(true);
		
		// a scroll pane for the text area
		pane = new JScrollPane(textarea);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		splitter.add(pane, JSplitPane.LEFT);
		
		// side panel with nicklist
		sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
//		splitter.add(sidePanel, JSplitPane.RIGHT);
		
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, pane, sidePanel);
		add(splitter, BorderLayout.CENTER);
		
		// nicklist
		nicklist = new JList();
		nicklist.setCellRenderer(new ListNickRenderer());
		nicklist.setSelectionBackground(new Color(0, 0, 177));
		
		// a scroll pane for the nicklist
		nickScroller = new JScrollPane(nicklist);
		nickScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sidePanel.add(nickScroller, BorderLayout.CENTER);
		
		splitter.setDividerLocation(0.83);
	}
	
	void showString(String s)
	{
		JScrollBar x_scrollbar, y_scrollbar;
		boolean isAtMaximum;
		
		x_scrollbar = pane.getHorizontalScrollBar();
		y_scrollbar = pane.getVerticalScrollBar();
		isAtMaximum = (y_scrollbar.getValue() >= y_scrollbar.getMaximum() - y_scrollbar.getVisibleAmount());
		
		textarea.append(s + "\n");
		
		if (true)
		{
			Rectangle rect;
			//System.out.println("IRCPanel::showString: setting scrollbar value");
			//scrollbar.setValue(scrollbar.getMaximum() - scrollbar.getVisibleAmount());
//			rect = textarea.getBounds();
			rect = new Rectangle();
//			System.out.println("IRCPanel::ShowString: original rect = " + rect);
			rect.x = x_scrollbar.getMaximum() - x_scrollbar.getVisibleAmount();
			rect.y = y_scrollbar.getMaximum() - y_scrollbar.getVisibleAmount();
			rect.width = x_scrollbar.getVisibleAmount();
			rect.height = y_scrollbar.getVisibleAmount();
			if (rect.x < 0)
				rect.x = 0;
			if (rect.y < 0)
				rect.y = 0;
//			System.out.println("IRCPanel::ShowString: rect = " + rect);
			textarea.scrollRectToVisible(rect);
		}
		
		setHighlightState(100);
	}
	
	void doInput(IRCLine line)
	{
		String command;
		String target = null;
	
		if (line.getRemaining().charAt(0) != '/')
		{
			command = "MSG";
			target = channel;
		}
		else
		{
			command = line.getNextToken().substring(1);
//			command = command.substring(1);
		}
		
		switch(getID(command))
		{
			case CMD_PRIVMSG:
				if (target == null)
				{
					target = line.getNextToken();
					showString(">" + target + "< " + line.getRemaining());
				}
				else
					handleMessage(connection.getNick(), line);
				connection.sendPrivMsg(target, line.getRemaining());
//				showString("<" + connection.getNick() + "> " + line.getRemaining());
				break;
			case CMD_ME:
				connection.sendAction(channel, line.getRemaining());
				handleAction(connection.getNick(), line);
				break;
			case CMD_JOIN:
				application.doJoin(line.getNextToken(), connection, this);
				break;
			case CMD_NICK:
				connection.changeNick(line.getRemaining());
				break;
			case CMD_PART:
				if (channel == null)
					break;
				reset(true);
				showString("You left " + channel + ".");
				break;
		}
	}		

	// part of ChannelListener!
	public void reset()
	{
	}

	private void reset(boolean needToPart)
	{
		if (channel == null)
			return;
		connection.unregisterChannelListener(channel, this);
		if (needToPart)
			connection.partChannel(channel);
		channel = null;
		window.titleChanged(this);
	}
		
	
	String getChannelName()
	{
		return channel;
	}
	
	void setChannelName(String newChannel)
	{
		channel = newChannel;
		window.setTabName(this, channel);
	}
	
	public boolean isFocusTraversable()
	{
		return false;
	}
	
	
	
	public void init(IRCConnection connection)
	{
	}
	
	public void initialNickList(Vector /* ListNick */ nicks)
	{
		updateNickList();
	}
	
	public void initialTopic(String topic)
	{
		this.topic.setText(topic);
	}
	
	public void initialOpTopic(boolean mode)
	{
	}
	
	public void initialNoExtMsg(boolean mode)
	{
	}
	
	public void initialSecret(boolean mode)
	{
	}

	public void initialInviteOnly(boolean mode)
	{
	}

	public void initialPrivate(boolean mode)
	{
	}
	
	public void initialModerated(boolean mode)
	{
	}
	
	public void initialLimit(boolean mode, int limit)
	{
	}
	
	public void initialKey(boolean modek, String key)
	{
	}
	
	public void initialBan(Vector /* <String> */ bans)
	{
	}
	
	

	public void setInviteOnly(boolean b, String chanop)
	{
	}

	public void setPrivate(boolean b, String chanop)
	{
	}

	public void setSecret(boolean b, String chanop)
	{
	}

	public void setModerated(boolean b, String chanop)
	{
	}

	public void setNoExtMsg(boolean b, String chanop)
	{
	}

	public void setOpTopic(boolean b, String chanop)
	{
	}

	public void setKey(String key, String chanop)
	{
	}

	public void setLimit(int limit, String chanop)
	{
	}

	public void ban(String mask, boolean mode, String chanop)
	{
		
	}

	public void setOtherMode(char mode, boolean type, String chanop)
	{
	}

	public void setTopic(String topic, String chanop)
	{
		this.topic.setText(topic);
		showString("--- " + chanop + " has set the topic to \"" + topic + "\"");
	}

/*
	public void setUrl(String url)
	{
	}
*/

	public void join(String name, String ident, String host)
	{
		showString("--> " + name + " (" + ident + "@" + 
					host + ") has joined " + channel);
		updateNickList();
	}

	public void part(String name, String ident, String host, String msg)
	{
		String postfix;
		
		if (msg.equals(""))
			postfix = "";
		else
			postfix = " (" + msg + ")";
	
		showString("<-- " + name + " (" + ident + "@" + 
					host + ") has left " + channel + postfix);
		updateNickList();
	}

	public void quit(String name, String ident, String host, String msg)
	{
		showString("<-- " + name + " has quit (" + msg + ")");
		updateNickList();
	}

	public void nickChange(String oldName, String newName)
	{
		if (oldName.equalsIgnoreCase(connection.getNick()))
			showString("--- You are now known as " + newName);
		else
			showString("--- " + oldName + " is now known as " + newName);
		
		window.nickChanged(this);
		updateNickList();
	}

	public void kick(String name, String reason, String chanop)
	{
		if (name.equalsIgnoreCase(getNick()))
		{
			showString("-- You have been kicked from " + channel + " by " + chanop + " (" + reason + ")");
			reset(false);
		}
		else
			showString("<-- " + chanop + " has kicked " + name + " from " + channel + " (" + reason + ")");
		updateNickList();
	}

	public void op(String name, boolean mode, String chanop)
	{
		if (mode)
			showString("-- " + chanop + " gives channel operator status to " + name);
		else
			showString("-- " + chanop + " removes channel operator status from " + name);
		if (name.equalsIgnoreCase(getNick()))
			window.nickChanged(this);
		updateNickList();
	}

	public void voice(String name, boolean mode, String chanop)
	{
		if (mode)
			showString("-- " + chanop + " gives voice to " + name);
		else
			showString("-- " + chanop + " removes voice from " + name);
		if (name.equalsIgnoreCase(getNick()))
			window.nickChanged(this);
		updateNickList();
	}

	public void handleMessage(String sender, IRCLine message)
	{
		showString("<" + IRCMessage.getNick(sender) + "> " + message.getRemaining());
		
		if (message.getRemaining().toLowerCase().indexOf(getNick().toLowerCase()) >= 0)
			setHighlightState(200);
	}
	
	public void handleAction(String sender, IRCLine message)
	{
		showString("* " + IRCMessage.getNick(sender) + " " + message.getRemaining());
		
		if (message.getRemaining().toLowerCase().indexOf(getNick().toLowerCase()) >= 0)
			setHighlightState(200);
	}
	
	
	
	
	
	boolean isOpped()
	{
		if (connection == null || channel == null)
			return false;
		return connection.getChannel(channel).isOp(getNick());
	}
	
	boolean isVoiced()
	{
		if (connection == null || channel == null)
			return false;
		return connection.getChannel(channel).isVoice(getNick());
	}
	
	private void setHighlightState(int level)
	{
		if (level > highlightLevel)
		{
			highlightLevel = level;
			window.somethingHappened(this, level);
		}
	}
	
	public void resetHighlightState()
	{
		highlightLevel = 0;
	}
}
