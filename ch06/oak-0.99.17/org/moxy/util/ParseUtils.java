package org.moxy.util;

import java.util.Vector;

public class ParseUtils
{
	public static String[] parseBlock(String source, char beginchar, char endchar, char[] whitespacechars)
	{
		int i;
		int blockstart, blockend;
		char currentChar;
		String[] result;
		String whitespacestring;
		
		blockstart = -1;
		blockend = -1;
		whitespacestring = new String(whitespacechars);
		
		for (i = 0; i < source.length(); i++)
		{
			currentChar = source.charAt(i);
			if (whitespacestring.indexOf(currentChar) >= 0)
				continue;
			if (currentChar == beginchar)
				if (blockstart < 0)
					blockstart = i;
				else
					return null;
			if (currentChar == endchar)
				if (blockstart < 0)
					return null;
				else
					if (blockend < 0)
					{
						blockend = i;
						result = new String[2];
						result[0] = source.substring(blockend + 1);
						result[1] = source.substring(blockstart + 1, blockend);
						return result;
					}
					else
						return null; // should *never* happend since we'll exit when we find a blockend
		}
		
		if (blockstart >= 0 && blockend < 0)
			return null;
		
		result = new String[2];
		result[0] = source;
		result[1] = "";
		return result;
	}

	public static String[] parseList(String source, char delimiter, char[] whitespacechars)
	{
		int i;
		char currentChar;
		int tokenstart, tokenend;
		String whitespacestring;
		Vector tokens;
		String[] result;
		
		tokenstart = -1;
		tokenend = -1;
		whitespacestring = new String(whitespacechars);
		tokens = new Vector();
		
		for (i = 0; i < source.length(); i++)
		{
			currentChar = source.charAt(i);
			
			if (whitespacestring.indexOf(currentChar) >= 0)
			{
				if (tokenstart >= 0 && tokenend < 0)
					tokenend = i - 1;
				continue;
			}
			if (currentChar == delimiter)
				if (tokenstart < 0)
					return null;
				else
				{
					if (tokenend < 0)
						tokenend = i - 1;
					tokens.addElement(source.substring(tokenstart, tokenend + 1));
					tokenstart = -1;
					tokenend = -1;
					continue;
				}
			
			if (tokenstart < 0)
				if (tokenend < 0)
					tokenstart = i;
				else
					return null;
		}
		if (tokenstart < 0)
			if (tokens.size() == 0)
				return new String[0];
			else
				return null;
		if (tokenend < 0)
			tokenend = i - 1;
		tokens.addElement(source.substring(tokenstart, tokenend + 1));
		
		result = new String[tokens.size()];
		for (i = 0; i < tokens.size(); i++)
			result[i] = tokens.elementAt(i).toString();
		return result;
	}
	
	public static String[] parseListInBlock(String source, char beginchar, char endchar, 
									 char delimiter, char[] whitespacechars)
	{
		String[] result1, result2;
		String[] result;
		
		result1 = parseBlock(source, beginchar, endchar, whitespacechars);
		if (result1 == null)
			return null;
		
		result2 = parseList(result1[1], delimiter, whitespacechars);
		if (result2 == null)
			return null;
		
		result = new String[result2.length + 1];
		result[0] = result1[0];
		System.arraycopy(result2, 0, result, 1, result2.length);
		
		return result;
	}
	
}
