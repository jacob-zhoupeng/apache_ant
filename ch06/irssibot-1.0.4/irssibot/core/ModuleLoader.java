/*
 * $Id: ModuleLoader.java,v 1.2 2001/04/10 19:41:40 matti Exp $
 *
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
package irssibot.core;

import java.io.*;

/**
 * A custom ClassLoader to load and unload modules.
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.2 $
 */
class ModuleLoader extends ClassLoader
{
    public ModuleLoader()
    {

    }

    public Class loadClass(String name) throws ClassNotFoundException
    {
	Class loadedClass = findLoadedClass(name);
	if( loadedClass == null ) {
	    try {
		loadedClass = findSystemClass(name);
	    } catch( Exception e ) {
		/* system class not found. do nothing */
	    }
	    
	    if( loadedClass == null ) {
	    
		String fileName = "modules/" + name.replace('.', File.separatorChar) + ".class";
		byte data[] = null;

		try {
		    data = loadClassData(fileName);
		} catch( IOException e ) {
		    e.printStackTrace();
		    throw new ClassNotFoundException(e.getMessage());
		}

		loadedClass = defineClass(name,data,0,data.length);

		if( loadedClass == null ) {
		    throw new ClassNotFoundException("Could not load " + name);
		} else {
		    resolveClass(loadedClass);
		    System.out.println("loadClass() resolved: " + name);
		}
	    } else {
		//		System.out.println("loadClass(): found loaded system class: " + name);
	    }
	} else {
	    //	    System.out.println("loadClass(): found loaded class: " + name);
	}

	return loadedClass;
    }

    private byte[] loadClassData(String fileName) throws IOException 
    {
	File file = new File(fileName);
	byte buffer[] = new byte[ (int)file.length() ];

	FileInputStream in = new FileInputStream(file);
	DataInputStream dataIn = new DataInputStream(in);
	//	int read = in.read(buffer);

	dataIn.readFully(buffer);
	dataIn.close();

	return buffer;
    }
}












