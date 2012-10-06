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

public class ConfigurationPanel extends Frame implements ActionListener
{
	ConfigurationSystem system;
	GroupItem[] elems;
	ValuePanel[] panels;
	String id;
	Button okButton, cancelButton, backButton;
	Label message;

	public ConfigurationPanel(ConfigurationSystem system, GroupItem[] elems, String id)
	{
		int i;
		Panel buttonPanel;
		Panel elemsPanel;
		
		this.system = system;
		this.elems = elems;
		this.id = id;
		this.setLayout(new BorderLayout());
		elemsPanel = new Panel();
		panels = new ValuePanel[elems.length];
		elemsPanel.setLayout(new LeftMinimalLayout());
		for (i = 0; i < elems.length; i++)
		{
			panels[i] = addWidget(elems[i]);
			elemsPanel.add(panels[i]);
		}
		this.add(elemsPanel, BorderLayout.CENTER);
		
		buttonPanel = new Panel();
		buttonPanel.setLayout(new FlowLayout());
		okButton = new Button("Apply");
		cancelButton = new Button("Close");
		backButton = new Button("Back");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		backButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(backButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		message = new Label();
		this.add(message, BorderLayout.NORTH);
		
		this.show();
		this.setSize(this.getPreferredSize());
	}
	
	private ValuePanel addWidget(GroupItem elem)
	{
		if (elem instanceof SimpleGroupItem)
			return new SimpleValuePanel((SimpleGroupItem)elem, 
					system.getPreference(system.constructID(id, elem.getID())));
		else if (elem instanceof ChoiceGroupItem)
			return new ChoiceValuePanel((ChoiceGroupItem)elem, 
					system.getPreference(system.constructID(id, elem.getID())));
		else if (elem instanceof LinkGroupItem)
			return new LinkValuePanel((LinkGroupItem)elem, this);
		else if (elem instanceof ListGroupItem)
			return new ListValuePanel((ListGroupItem)elem, 
					system.getListPreference(system.constructID(id, elem.getID())), 
					this);
		else
			return null;
	}
	
	void handleLink(String classname, int page, String name)
	{
		String newID;
		GroupItem[] newElems;
		
		newID = system.constructID(id, name);
		newElems = system.getGroup(classname, page, newID);
		if (newID == null)
		{
			System.out.println("ConfigurationPanel::handleLink (" +
								classname + ") -> newID = null");
			return;
		}
		if (newElems == null)
		{
			System.out.println("ConfigurationPanel::handleLink (" +
								classname + ") -> newElems = null");
			return;
		}
		new ConfigurationPanel(system, newElems, newID);
	}
	
	void handleLink(String classname, int page, String name, String subID)
	{
		handleLink(classname, page, system.constructID(name, subID));
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String command;
		
		command = event.getActionCommand();
		if (command.equals("Apply"))
			handleApply();
		else if (command.equals("Close"))
			handleClose();
		else if (command.equals("Back"))
			handleBack();
	}
	
	private void handleApply()
	{
		int i;
		
		for (i = 0; i < panels.length; i++)
			if (!panels[i].hasValidValue())
			{
				showTypeErrorDialog(elems[i].getShortDescription());
				return;
			}
		for (i = 0; i < panels.length; i++)
			panels[i].storeValue(system, id);
		message.setText("");
	}
	
	private void handleClose()
	{
		dispose();
	}
	
	private void handleBack()
	{
		String newID;
		
		newID = system.getPreviousID(id);
	}
	
	private void showTypeErrorDialog(String description)
	{
//		new ErrorDialog("You did not supply a valid value for the " + description);
		message.setText("You did not supply a valid value for the " + description);
	}
}
