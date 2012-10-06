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
import org.moxy.util.config.*;

class SimpleValuePanel extends ValuePanel
{
	TextField textfield;
	SimpleGroupItem elem;

		SimpleValuePanel(SimpleGroupItem elem, String value)
	{
		this.elem = elem;
		this.setLayout(new FlowLayout());
		this.add(new Label(elem.getShortDescription()));
		if (value == null)
			value = "";
		textfield = new TextField(value, 30);
		this.add(textfield);
	}
	
	String getValue()
	{
		return textfield.getText();
	}
	
	boolean hasValidValue()
	{
		return elem.acceptsValue(getValue());
	}
	
	void storeValue(ConfigurationSystem system, String baseID)
	{
		String id;
		
		id = system.constructID(baseID, elem.getID());
		system.setPreference(id, getValue());
	}
}
