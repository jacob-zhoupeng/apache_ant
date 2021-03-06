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

class IntValuesType implements PrimitiveType
{
	private int[] values;

		IntValuesType(int[] values)
	{
		this.values = values;
	}
	
	public boolean acceptsValue(String string)
	{
		int i, stringIntValue;
		
		try
		{
			stringIntValue = Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		for (i = 0; i < values.length; i++)
			if (stringIntValue == values[i])
				return true;
		return false;
	}
}
