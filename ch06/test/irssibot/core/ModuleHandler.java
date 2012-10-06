/*
 * $Id: ModuleHandler.java,v 1.1 2001/03/26 22:29:41 matti Exp $
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

import irssibot.modules.AbstractModule;
import irssibot.user.*;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Manages modules for IrssiBot. the modules can be loaded and added to
 * handler at startup (from config file) or dynamically from IRC
 * using core->loadModule().<p>
 * 
 * All calls to modules are wrapped inside a tight try - catch in 
 * order to detect exceptions in the module and remove it in that case.<p>
 *
 * @author Matti Dahlbom 
 * @version $Name:  $ $Revision: 1.1 $
 */
public class ModuleHandler
{
    private Hashtable loadedModules = null;
    private AbstractModule moduleTable[] = null;
    private int numModules = 0;
    private Core core = null;

    public ModuleHandler(Core core)
    {
	loadedModules = new Hashtable();
	this.core = core;
    }

    public AbstractModule[] getModuleTable() { return moduleTable; }

    /**
     * Removes faulted module cleanly. 
     *
     * @param e Exception caused by module fault
     * @param module the module error occurred in
     */
    private void handleModuleCrash(Exception e,AbstractModule module)
    {
	putlog("handleModuleCrash(): module " + module.getClass().getName() + 
	       " caused an " + e.getClass().getName() + " and was removed. Cause: " +
	       e.getMessage());
	e.printStackTrace();

	if( !removeModule(module.getClass().getName()) ) {
	    putlog("handleModuleCrash(): error removing module "+module.getClass().getName());
	}
	
	reconstructTable();

	/* notify core */
	core.globalChannelBroadcast("Module " + module.getClass().getName() + 
				    " caused an " + e.getClass().getName() + " and was removed. " + 
				    "Cause: " + e.getMessage());
    }

    /**
     * Forwards an IrcMessage to all registered modules. All exceptions in the
     * the module are caught and handled.
     *
     * @param message the IrcMessage to forward
     * @param caller the ServerConnection the message came from
     * @return true is successful. false indicates an error occurred in a
     * module and it was removed from module table.
     */
    public boolean forwardMessage(IrcMessage message,ServerConnection caller) 
    {
	boolean ret = true;

	/* add message to all module's message queue */
	for( int i = 0; i < numModules; i++ ) {
	    try {
		moduleTable[i].addMessage(message,caller);
	    } catch( Exception e ) {
		handleModuleCrash(e,moduleTable[i]);
		ret = false;
	    }
	}
	return ret;
    }

    /**
     * for optimal access to modules, construct a table representation of
     * the contents of the hash table and maintain the number of 
     * loaded modules in numModules
     */
    private void reconstructTable()
    {
	numModules = loadedModules.size(); 
	moduleTable = null;
	moduleTable = new AbstractModule[numModules];

	Enumeration enum = loadedModules.elements();
	int i = 0;
	while( enum.hasMoreElements() ) {
	    //	    moduleTable[i++] = (Module)enum.nextElement();
	    moduleTable[i++] = (AbstractModule)enum.nextElement();
	}
    }

    /**
     * Adds a module. The module's onLoad() is called after addition, 
     * and it starts receiving message events immediately.
     *
     * @param moduleClassName Java class name of module 
     * @param module AbstractModule to add
     * @return true if successful, or false if failed.
     */
    public boolean addModule(String moduleClassName,AbstractModule module)
    {
	boolean ret = false;

	putlog("addModule(): adding module "+module.getClass().getName());

	/* if module isnt yet loaded, load it up. */
	if( !loadedModules.containsKey(moduleClassName) ) {
	    loadedModules.put(moduleClassName,module);

	    /* notify module it was loaded */
	    ret = module.onLoad(loadModuleState(moduleClassName),core);
	} else {
	    putlog("addModule(): module already loaded");
	    return false;
	}
	
	if( ret == false ) {
	    removeModule(moduleClassName);
	    return false;
	}
	/* module loaded ok. start the consumer thread */
	module.start();

	reconstructTable();

	return ret;
    }

    /**
     * Removes a module. The module's onUnload() is called on removal,
     * and it stops receiving message events.
     * 
     * @param moduleClassName name o module to remove
     * @return true if successfully removed. false if could not remove
     */
    synchronized public boolean removeModule(String moduleClassName)
    {
	boolean ret = false;
	AbstractModule module = null;

	/* if module is loaded, remove it. */
	if( loadedModules.containsKey(moduleClassName) ) {
	    /* notify module it was unloaded */
	    module = (AbstractModule)loadedModules.get(moduleClassName);
	    module.onUnload();

	    ClassLoader loader = module.getClass().getClassLoader();
	    loader = null;
	    module = null;

	    System.gc();

	    loadedModules.remove(moduleClassName);
	    ret = true;
	}

	reconstructTable();

	return ret;
    }

    /**
     * Loads state of a module from disk as a Properties object. 
     * 
     * @param moduleClassName the name of module class
     * @return Module's state as a Properties object or a null if state file not found.
     */
    private Properties loadModuleState(String moduleClassName)
    {
	String fileName = Core.moduleStateFilePath+moduleClassName+".state";
	Properties props = new Properties();

	try {
	    FileInputStream inStream = new FileInputStream(fileName.toLowerCase());
	    props.load(inStream);
	    putlog("loadModuleState(): loaded state for "+moduleClassName);
	} catch( IOException e ) {
	    putlog("loadModuleState(): "+e.getClass().getName()+": "+e.getMessage());
	    props = null;
	}
	return props;
    }

    /**
     * Saves module state to disk as a Properties object. 
     *
     */
    public void saveModuleState(AbstractModule module)
    {
	Properties props = null;

	try {
	    props = module.getState();
	} catch( Exception e ) {
	    e.printStackTrace();
	    putlog("saveModuleState(): module "+module.getClass().getName()+" caused an error and was removed.");
	    removeModule(module.getClass().getName());
	    return;
	}
	    
	if( props != null ) {
	    String fileName = Core.moduleStateFilePath+module.getClass().getName()+".state";

	    try {
		FileOutputStream outStream = new FileOutputStream(fileName.toLowerCase());
		props.store(outStream,"state file for "+module.getClass().getName());
		putlog("saveModuleState(): saved state for "+module.getClass().getName());
	    } catch( IOException e ) {
		putlog("saveModuleState(): "+e.getClass().getName()+": "+e.getMessage());
		props = null;
	    }
	}
    }

    /**
     * Write a logging message to stdout
     *
     * @param msg message to write
     */
    private void putlog(String msg)
    {
	String logMsg = getClass().getName()+": "+msg+"\n";
	System.out.print(logMsg);
    }
}













