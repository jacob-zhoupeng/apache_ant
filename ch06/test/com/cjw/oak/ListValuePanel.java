/* Copyright (C) 2000 Christiaan Welvaart
   This file is part of the OAK Configuration Library.

   The OAK Configuration Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   The OAK Configuration Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public
   License along with the OAK Configuration Library; see the file COPYING.LIB.  If not,
   write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

package com.cjw.oak;

import java.awt.*;
import java.awt.event.*;
import org.moxy.util.config.*;
import com.cjw.util.*;

class ListValuePanel extends ValuePanel implements ActionListener
{
	List list;
	TextField insertText;
	Button insertButton;
	Button deleteButton;
	Button editButton;
	ListGroupItem elem;
	ConfigurationPanel parent;

		ListValuePanel(ListGroupItem elem, String[] values, ConfigurationPanel parent)
	{
		int i;
		Panel panel;
		
		this.elem = elem;
		this.parent = parent;
		
		this.setLayout(new TopMinimalLayout());
		this.add(new Label(elem.getShortDescription()));
		
		list = new List();
		if (values != null)
			for (i = 0; i < values.length; i++)
				list.add(values[i]);
		this.add(list);
		
		panel = new Panel();
		this.add(panel);
		panel.setLayout(new LeftMinimalLayout());
		insertText = new TextField(30);
		insertButton = new Button("Insert");
		insertButton.addActionListener(this);
		deleteButton = new Button("Delete");
		deleteButton.addActionListener(this);
		panel.add(insertText);
		panel.add(insertButton);
		panel.add(deleteButton);
		if (!elem.getItemClass().equals(""))
		{
			editButton = new Button("Edit...");
			editButton.addActionListener(this);
			panel.add(editButton);
		}
		else
			editButton = null;
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String command;
		
		command = event.getActionCommand();
		if (command.equals("Insert"))
			handleInsert();
		else if (command.equals("Delete"))
			handleDelete();
		else if (command.equals("Edit..."))
			handleEdit();
	}
	
	private void handleInsert()
	{
		String value;
		
		value = insertText.getText();
		if (value.equals(""))
			return;
		list.add(value);
		insertText.setText("");
	}
	
	private void handleDelete()
	{
		int index;
		
		index = list.getSelectedIndex();
		if (index >= 0)
			list.remove(index);
	}
	
	private void handleEdit()
	{
		int index;
		String className;
		
		index = list.getSelectedIndex();
		if (index < 0)
			return;
		
		className = elem.getItemClass();
		if (className.equals("@"))
			className = list.getSelectedItem();
		parent.handleLink(className, elem.getGroupNum(), elem.getID(), Integer.toString(index));
	}
	
	boolean hasValidValue()
	{
		int i;
		
		for (i = 0; i < list.getItemCount(); i++)
		{
			if (!elem.acceptsValue(list.getItem(i)))
				return false;
		}
		return true;
	}
	
	String[] getListValue()
	{
		return list.getItems();
	}
	
	void storeValue(ConfigurationSystem system, String baseID)
	{
		String id;
		
		id = system.constructID(baseID, elem.getID());
		system.setListPreference(id, getListValue());
	}
}
