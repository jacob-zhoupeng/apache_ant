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

import gnu.regexp.*;

public class RegExpType implements PrimitiveType
{
	private RE regexp;

		RegExpType(String regexp) throws TypeFormatException
	{
		try
		{
			this.regexp = new RE(regexp);
		}
		catch(REException e)
		{
			throw new TypeFormatException("while constructing a RegExpType: " + 
										  e.toString());
		}
	}
	
	public boolean acceptsValue(String string)
	{
		System.out.println("RegExpType.acceptsValue: called with regexp = " + 
							regexp.toString() + " and string = " + string);
		return (regexp.isMatch(string));
	}
}
