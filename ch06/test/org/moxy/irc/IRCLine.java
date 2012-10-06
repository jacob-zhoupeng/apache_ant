package org.moxy.irc;

/*/
 * IRCLine.java
 * Oak, moxy's IRC bot
 *
 * Copyright © 2000  Christiaan Welvaart
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * written by Christiaan Welvaart (cjw)
 * welvaart@phys.uu.nl
 *
 * 22-04-2000: cjw	added new javadoc comments and refined existing comments
 * 16-03-2000: cjw	added a destructive toVector() method
 * 22-02-2000: cjw	added putBack() and getParam() methods + necessary modifications
 * 17-02-2000: cjw	start, version 1.0
 * 
/*/


import java.util.Vector;

/**
 * A class that tokenizes Strings similar to StreamTokenizer.
 * @version 1.0
 * @author spturtle
 */
public class IRCLine{

	private String line;
	Vector /* <String> */ tokens;

	/**
	 * Creates a new IRCLine
	 * @since 1.0
	 * @param line The string to tokenize.
	 */
		public IRCLine(String line)
	{
		this.line = line;
		skipWhiteSpace();
		tokens = new Vector();
	}
	
	private void skipWhiteSpace()
	{
		int i;
		
		for (i = 0; i < line.length(); i++)
		{
			switch(line.charAt(i))
			{
				case ' ':
				case '\t':
				case '\n':
					break;
				default:
					if (i > 0)
						line = line.substring(i);
					return;
			}
		}
		line = "";
	}

    /**
     * Returns the amount of String that hasn't been
     * tokenized yet. If the line was fully tokenized, an empty string will be returned.
     * @since 1.0
     * @return String that hasn't been tokenized.
     */
	public String getRemaining()
	{
		StringBuffer buffer;
		int i;
		
		if (tokens.size() > 0)
		{
			buffer = new StringBuffer();
			for (i = 0; i < tokens.size(); i++)
				buffer.append((String)tokens.elementAt(i) + " ");
			return buffer.toString() + line;
		}
		else
			return line;
	}

    /**
     * Tells if there's any more tokens in the String.
     * @since 1.0
     * @return True if there's no more tokens false otherwise.
     */
	public boolean empty()
	{
		return line.equals("");
	}

	private String parseToken()
	{
		int i, endOfToken = 0;
		boolean foundToken = false;
		String oldLine;
		boolean inQuotedText = false;
		StringBuffer token;
		
		token = new StringBuffer();
			
		for (i = 0; i < line.length(); i++)
		{
			switch(line.charAt(i))
			{
				case ' ':
				case '\t':
					if (inQuotedText)
					{
						token.append(line.charAt(i));
						break;
					}
					oldLine = line;
					line = oldLine.substring(i);
					skipWhiteSpace();
					return token.toString();
				case '\"':
					if (token.length() > 0)
					{
						if (inQuotedText)
							i++;
						oldLine = line;
						line = oldLine.substring(i);
						if (inQuotedText)
						{
							skipWhiteSpace();
							inQuotedText = false;
						}
						return token.toString();
					}
					inQuotedText = true;
					break;
				case '\\':
					i++;
					switch(line.charAt(i))
					{
						case 't':
							token.append('\t');
						case 'n':
							token.append('\n');
						default:
							token.append(line.charAt(i));
					}
					i++;
					break;
				default:
					token.append(line.charAt(i));
			}
		}
		
		oldLine = line;
		line = "";
		return oldLine;
	}
	
	/**
     * Gets the next token in the String. Double quotes are removed, 
	 * tabs and spaces are ignored, backslash and double quote can be 
	 * escaped with a backslash, \n = newline, \t = tab.
     * @since 1.0
     * @return The next token in the line.
     */
	public String getNextToken()
	{
		String token;
		
		if (tokens.size() > 0)
		{
			token = (String)tokens.lastElement();
			tokens.removeElementAt(tokens.size() - 1);
			return token;
		}
		else
			return parseToken();
	}
	
	/**
	 * Puts back a token into the line. A subsequent call to getNextToken 
	 * will return this token.
	 *
	 * @param token The token to put back. 
	 */
	public void putBack(String token)
	{
		tokens.addElement(token);
	}
	

    /**
     * Checks to see if there are any more tokens left in the String.
     * Basically the oppiste of empty().
     * @since 1.0
     * @return True if there are more tokens false otherwise.
	 */
	public boolean hasMoreTokens()
	{
		if (tokens.size() > 0)
			return true;
		else
        	if(line.equals(""))
				return false;
			else
				return true;
	}
	
	/** 
	 * Checks if the next token is equal to the given keyword.
	 * If this is the case, the token following the keyword 
	 * is returned. If there is no next token, the next token 
	 * is not equal to the given keyword, or no token follows 
	 * the found keyword, null is returned.
	 */	
	public String getParam(String keyword)
	{
		String token1, token2;
		
		token1 = parseToken();
		if (token1.equals(""))
			return null;
		
		if (token1.equals(keyword))
		{
			token2 = getNextToken();
			if (token2.equals(""))
			{
				putBack(token1);
				return null;
			}
			else
				return token2;
		}
		else
		{
			putBack(token1);
			return null;
		}
	}
	
	/**
	 * Counts the tokens in the line.
	 * @returns the number of tokens still in this line.
	 */
	public int countTokens()
	{
		int n;
		String token;
		
		n = 0;
		skipWhiteSpace();

		while (!(token = parseToken()).equals(""))
		{
			putBack(token);
			n++;
		}
		
		return n;
	}
	
	/**
	 * Consumes all remaining tokens and returns them 
	 * in a Vector.
	 * @returns a Vector containing all remaining tokens.
	 */
	public Vector toVector()
	{
		Vector result = new Vector();
		
		while (hasMoreTokens())
			result.addElement(getNextToken());
		
		return result;
	}
}
