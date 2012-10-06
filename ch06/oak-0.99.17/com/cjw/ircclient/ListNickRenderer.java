package com.cjw.ircclient;

import java.awt.*;
import javax.swing.*;
import org.moxy.irc.ListNick;

class ListNickRenderer extends JLabel implements ListCellRenderer {
	final static ImageIcon opIcon = new ImageIcon("green_dot.gif");
	final static ImageIcon voiceIcon = new ImageIcon("yellow_dot.gif");
//	final static ImageIcon regularIcon = new ImageIcon("invisi_dot.gif");

     // This is the only method defined by ListCellRenderer.  We just
     // reconfigure the Jlabel each time we're called.

	public Component getListCellRendererComponent(
		JList list,
		Object value,            // value to display
		int index,               // cell index
		boolean isSelected,      // is the cell selected
		boolean cellHasFocus)    // the list and the cell have the focus
     {
		ListNick nick;

		nick = (ListNick)value;

		setText(nick.getName());
		if (nick.isChanop())
			setIcon(opIcon);
		else if (nick.hasVoice())
			setIcon(voiceIcon);
		else
			setIcon(null);
//		else
//			setIcon(regularIcon);
		if (isSelected)
		{
    		setBackground(list.getSelectionBackground());
    		setForeground(list.getSelectionForeground());
		}
		else
		{
    		setBackground(list.getBackground());
    		setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		return this;
	}
}
