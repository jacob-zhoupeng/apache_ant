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

import java.util.Vector;

/* XML (dom) parser support */
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;

/**
 * collection of util functions for XML documents 
 *
 * @author Matti Dahlbom
 */
public class XMLUtil
{
    /**
     * used to parse out elements of a list such as this:
     * <node>
     *    <element>value1</element>
     *    <element>value2</element>
     *    <element>value3</element>
     * </node>
     *
     * for this example, called with Node pointing to <node> element and "element" 
     * in listElementName.
     * @param listNode pointer to list's "root" node
     * @param listElementName name of list element
     * @return Vector of list elements 
     */
    public static Vector getListElements(Node listNode,String listElementName)
    {
	String tmp = "";
	Vector ret = null;
    
	NodeList children = listNode.getChildNodes();
	if( children != null ) {
	    ret = new Vector();

	    for( int i = 0; i < children.getLength(); i++ ) {
		Node child = children.item(i);
		    	
		if( child.getNodeName().equalsIgnoreCase(listElementName) ) {
		    /* value is in element node's only (text) child */
		    String value = child.getChildNodes().item(0).getNodeValue();
		    if( (value != null) && !value.equals("") ) {
			ret.add(value);
		    }
		}
	    }
	} 
	return ret;
    }
}







