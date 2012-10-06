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

import org.moxy.util.ParseUtils;

public class TypeFactory
{
	static final int TYPE_INT = 0;
	static final int TYPE_CHAR = 1;
	static final int TYPE_STRING = 2;
	static final int TYPE_ID = 3;
	static final int TYPE_FLOAT = 4;
	static final int TYPE_BOOLEAN = 5;
	static final int TYPE_URL = 6;
	static final int TYPE_REGEXP = 7;

	static private final String[] base_types = 
	{
		"int", "char", "string", "id", "float", "boolean", "url", "regexp"
	};
	
	static final private char[] whitespace = {' '};
	static final private String ID_START_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	static final private String ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
	
	
	public static PrimitiveType getType(String type) throws TypeFormatException
	{
		int i, base_type = -1;
		int length = -1;
		PrimitiveType[] complextypes;
		String[] lists;
		
		// get base type
		for (i = 0; i < base_types.length; i++)
			if (type.startsWith(base_types[i]))
			{
				base_type = i;
				break;
			}
		if (i >= base_types.length)
			throw new TypeFormatException("No valid basetype given.");
		
		type = type.substring(base_types[base_type].length());
		
		if (type.length() > 0)
			switch(type.charAt(0))
			{
				case '(':
					if (base_type != TYPE_STRING)
						throw new TypeFormatException("() can only be used with a string");
					if (type.charAt(type.length() - 1) != ')')
						throw new TypeFormatException("expected ) at end");
					type = type.substring(1, type.length() - 1);
					try
					{
						length = Integer.parseInt(type);
					}
					catch(Exception e)
					{
						throw new TypeFormatException(e.toString());
					}
					return new StringType(length);
					// break;
				case '<':
				case '[':
				case '{':					
					lists = ParseUtils.parseList(type, '+', whitespace);
					
					if (lists.length <= 0)
						throw new TypeFormatException("empty list followed " + type.charAt(0));
					if (lists.length > 1)
						return parseComplexType(base_type, type);
					
					complextypes = new PrimitiveType[lists.length];
					for (i = 0; i < lists.length; i++)
						complextypes[i] = parseComplexType(base_type, lists[i]);
					
					return new UnionType(complextypes);
				case '/':
					String regexp;
					
					if (base_type != TYPE_REGEXP)
						throw new TypeFormatException("/.../ can only be used with a regular expression");
					if (type.charAt(type.length() - 1) != '/')
						throw new TypeFormatException("Unexpected error in regular expression");
					regexp = type.substring(1, type.length() - 1);
					if (regexp.length() < 1)
						throw new TypeFormatException("Empty regular expression");
					return new RegExpType(regexp);
				default:
					throw new TypeFormatException("Garbage after type identifier.");
			}
		
		switch(base_type)
		{
			case TYPE_INT:
				return new IntType();
			case TYPE_CHAR:
				return new CharType();
			case TYPE_STRING:
				return new StringType();
			case TYPE_ID:
				return new IDType();
			case TYPE_FLOAT:
				return new FloatType();
			case TYPE_BOOLEAN:
				return new BooleanType();
			case TYPE_URL:
				return new URLType();
			case TYPE_REGEXP:
				throw new TypeFormatException("no /.../ after regexp keyword");
		}
		
		return null;
	}
	
	private static PrimitiveType parseComplexType(int base_type, String type) throws TypeFormatException
	{
		char start, end;
		String[] listelems;
		Object[] values;
		int[] intValues = null;
		char[] charValues = null;
		String[] stringValues = null;
		double[] floatValues = null;
		boolean[] booleanValues = null;
		int i;
		
		start = type.charAt(0);
		end = type.charAt(type.length() - 1);
		type = type.substring(1, type.length() - 1);
		listelems = ParseUtils.parseList(type, ',', whitespace);
		values = new Object[listelems.length];
		
		for (i = 0; i < listelems.length; i++)
			values[i] = parseSimpleValue(base_type, listelems[i]);
		
		switch(base_type)
		{
			case TYPE_INT:
				intValues = new int[listelems.length];
				for (i = 0; i < listelems.length; i++)
					intValues[i] = ((Integer)values[i]).intValue();
				break;
			case TYPE_CHAR:
				charValues = new char[listelems.length];
				for (i = 0; i < listelems.length; i++)
					charValues[i] = ((Character)values[i]).charValue();
				break;
			case TYPE_STRING:
			case TYPE_ID:
			case TYPE_URL:
				stringValues = new String[listelems.length];
				for (i = 0; i < listelems.length; i++)
					stringValues[i] = values[i].toString();
				break;
			case TYPE_FLOAT:
				floatValues = new double[listelems.length];
				for (i = 0; i < listelems.length; i++)
					floatValues[i] = ((Double)values[i]).doubleValue();
			case TYPE_BOOLEAN:
				booleanValues = new boolean[listelems.length];
				for (i = 0; i < listelems.length; i++)
					booleanValues[i] = ((Boolean)values[i]).booleanValue();
		}
		
		switch(start)
		{
			case '<':
				if (values.length != 2 || base_type != TYPE_FLOAT || end != '>')
					throw new TypeFormatException("TypeFactory::parseComplexType: error #1");
				return new FloatOpenRangeType(floatValues[0], floatValues[1]);
			case '[':
				if (values.length != 2 || 
				    !(base_type == TYPE_FLOAT || base_type == TYPE_INT || base_type == TYPE_CHAR) ||
				    end != ']')
					throw new TypeFormatException("TypeFactory::parseComplexType: error #2");
				switch(base_type)
				{
					case TYPE_FLOAT: return new FloatRangeType(floatValues[0], floatValues[1]);
					case TYPE_INT: return new IntRangeType(intValues[0], intValues[1]);
					case TYPE_CHAR: return new CharRangeType(charValues[0], charValues[1]);
					default: throw new TypeFormatException("TypeFactory::parseComplexType: error #3");
				}
			case '{':
				if (!(base_type == TYPE_INT || base_type == TYPE_FLOAT ||
				      base_type == TYPE_CHAR || base_type == TYPE_STRING))
					throw new TypeFormatException("{} can only be used with int, char, float, and string");
				switch(base_type)
				{
					case TYPE_FLOAT: return new FloatValuesType(floatValues);
					case TYPE_INT: return new IntValuesType(intValues);
					case TYPE_CHAR: return new CharValuesType(charValues);
					case TYPE_STRING: return new StringValuesType(stringValues);
					default: throw new TypeFormatException("TypeFactory::parseComplexType: error #4");
				}
		}
		
		throw new TypeFormatException("TypeFactory::parseComplexType: unexpected error");
	}
	
	static Object parseSimpleValue(int type, String value) throws TypeFormatException
	{
		int i;
	
		try
		{
			switch(type)
			{
				case TYPE_INT:
					return Integer.valueOf(value);
				case TYPE_CHAR:
					if (value.length() > 1)
						return null;
					else
						return new Character(value.charAt(0));
				case TYPE_STRING:
					return value;
				case TYPE_ID:
					if (ID_START_CHARS.indexOf(value.charAt(0)) < 0)
						return null;
					for (i = 0; i < value.length(); i++)
						if (ID_CHARS.indexOf(value.charAt(i)) < 0)
							return null;
					return value;
				case TYPE_FLOAT:
					return Double.valueOf(value);
				case TYPE_BOOLEAN:
					return Boolean.valueOf(value);
				case TYPE_URL:
					return value;
			}
		}
		catch(Exception e)
		{
			throw new TypeFormatException(e.toString());
		}
		
		throw new TypeFormatException("Unexpected error in TypeFactory::parseSimpleValue");
	}
}
