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

package org.moxy.util.config;

public class ChoiceGroupItem extends GroupItem
{
	private String[] values;
	private PrimitiveType type;
	private String[] names;

		public ChoiceGroupItem(String id, String shortDescription, 
								String longDescription, 
								String[] values, String[] names)
	{
		super(id, shortDescription, longDescription);
		this.type = new StringValuesType(values);
		this.values = values;
		this.names = names;
	}
	
	public boolean acceptsValue(String value)
	{
		return type.acceptsValue(value);
	}

	public String[] getValues()
	{
		return values;
	}
	
	public String[] getNames()
	{
		return names;
	}
 }

