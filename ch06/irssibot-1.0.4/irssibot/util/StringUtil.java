/*
 * IrssiBot - An advanced IRC automation ("bot")
 * Copyright (C) 2000 Matti Dahlbom
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * mdahlbom@cc.hut.fi
 */
package irssibot.util;

import java.lang.*;
import java.util.Vector;

/**
 * includes a collection of handy tools for parsing and editing strings
 *
 * @author Matti Dahlbom
 */
public final class StringUtil 
{
    /**
     * returns a range of a string table elements as a new string table
     * @param input string table
     * @param beginIndex first element of the range
     * @param endIndex last element of the range
     * @return range
     */
    public static String[] range(String input[],int beginIndex,int endIndex) 
    {
	String ret[] = null;

	if( input != null ) {
	    if( (beginIndex >= 0) && (endIndex < input.length) && (beginIndex <= endIndex) ) {
		ret = new String[endIndex - beginIndex + 1];
		
		for( int i = beginIndex; i <= endIndex; i++ ) {
		    ret[i-beginIndex] = input[i];
		}
	    }
	}
	return ret;
    }
	    
    /**
     * returns a range of a string table elements as a new string table
     * starting from beginIndex to end of the input table.
     * @param input string table
     * @param beginIndex first element of the range
     * @return range
     */
    public static String[] range(String input[],int beginIndex)
    {
	return range(input,beginIndex,input.length - 1);
    }
	
    /**
     * combines elements of a stringtable into a single string.
     * @param input string table
     * @param beginIndex first element of the range
     * @param endIndex last element of the range
     * @return combined string
     */
    public static String join(String input[],int beginIndex,int endIndex) 
    {
	String ret = "";
	String filler = "";

	if( input == null ) {
	    return null;
	}

	if( (beginIndex >= 0) && (endIndex < input.length) && (beginIndex <= endIndex) ) {
	    for( int i = beginIndex; i <= endIndex; i++ ) {
		ret += filler + input[i];
		filler = " ";
	    }
	    return ret;
	} else 
	    return null;
    }

    /**
     * combines elements of a stringtable into a single string. range to combine
     * is from firstIndex to end of the table.
     * @param input string table
     * @param beginIndex first element of the range
     * @return combined string
     */
    public static String join(String input[],int beginIndex)
    {
	return join(input,beginIndex,input.length - 1);
    }

    /**
     * combines all elements of a stringtable into a single string. 
     * @param input string table
     * @return combined string
     */
    public static String join(String input[])
    {
	return join(input,0,input.length - 1);
    }

    /**
     * break up a String by separating it by sepChar's. return the
     * sections as a string table.
     * @param input input string
     * @param sepChar separating character
     * @return separated strings as a table
     */
    public static String[] separate(String input,char sepChar)
    {
	String ret[] = null;
	String tmp = null;
	String verified = null;
	Vector v = new Vector();
	int num = 1;

	/* check input */
	if( (input == null) || (input.equals("")) ) {
	    ret = null;
	} else {
	    /* check whether there are any instances of sepChar */
	    if( input.indexOf(sepChar) == -1 ) {
		/* none - return the original string as a single table cell */
		ret = new String[1];
		ret[0] = input;
	    } else {
		/* calculate number of separator characters */
		for( int i = 0; i < input.length(); i++ ) {
		    if( input.charAt(i) == sepChar )
			num++;
		}
		
		/* separate sections */
		tmp = input;
		for( int i = 0; i < (num - 1); i++ ) {
		    if( (verified = verify(tmp.substring(0,tmp.indexOf(sepChar)))) != null )
			v.add(verified);
		    tmp = tmp.substring(tmp.indexOf(sepChar)+1);
		}
		if( (verified = verify(tmp)) != null )
		    v.add(verified);

		ret = new String[v.size()];
		/* get an array out of the vector */
		Object tmpret[] = v.toArray();
		for( int i = 0; i < tmpret.length; i++ ) {
		    ret[i] = (String)tmpret[i];
		}
	    }
	}
	return ret;
    }

    /**
     * verify string. the following things are done:
     * <ul>trimming
     * <ul>removal of leading/trailing occurrences of the separator characters
     * <ul>checking for emptiness
     * @param input input string
     * @return verified string or null if failed
     */
    public static String verify(String input)
    {
	String ret = null;

	input = input.trim();

	if( (input == null) || (input.equals("")) ) {
	    ret = null;
	} else {
	    ret = input;
	}

	return ret;
    }

    /**
     * take a multiline string as input and separate each 
     * line and return a table of the separated lines. 
     * @param input source (multiline) string
     * @return separated lines as table
     */
    public static java.lang.String[] lines(String input)
    {
	return separate(input,'\n');
    }

    /**
     * do wildcard matching for two strings
     *
     * @param mask pattern to match
     * @param input string
     * @return <ul><li>true if match
     *             <li>false if not</ul>
     */
    public static boolean wildmatch(String mask,String input)
    {
	if( mask == null || input == null ) {
	    return false;
	}

	String lowerMask = mask.trim().toLowerCase();
	String lowerInput = input.trim().toLowerCase();

	int maskLen = lowerMask.length();
	int inputLen = lowerInput.length();

	if( (inputLen <= 0) || (maskLen <= 0) ) {
	    return false;
	}

	int maskIndex = 0;
	int inputIndex = 0;

	int nextMaskIndex = -1;
	int nextInputIndex = -1;

	char maskChar = '\0';
	boolean doingAsterisk = false;

	while( true ) {
	    /* look for end conditions */
	    if( maskIndex >= maskLen ) {
		if( doingAsterisk || (inputIndex >= inputLen) ) return true;
		maskChar = '\0';
	    } else {
		maskChar = lowerMask.charAt(maskIndex);
	    }

	    switch( maskChar ) {
	    case '*':
		doingAsterisk = true;
		nextMaskIndex = ++maskIndex;
		nextInputIndex = inputIndex;
		break;
	    default:
		if( inputIndex >= inputLen ) return false;

		doingAsterisk = false;
		if( maskChar != lowerInput.charAt(inputIndex) ) {
		    if( nextMaskIndex != -1 ) {
			maskIndex = nextMaskIndex;
			inputIndex = nextInputIndex++;
		    } else {
			return false;
		    }

		    if( inputIndex >= inputLen ) {
			return false;
		    }
		} else {
		    maskIndex++;
		    inputIndex++;
		}
		break;
	    }
	}
    }
}














