/*
 * $Id: Core.java,v 1.2 2001/04/10 19:41:40 matti Exp $
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
import java.net.*;
import java.util.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

/* import other IrssiBot packages */
import irssibot.config.*;
import irssibot.util.*;
import irssibot.modules.AbstractModule;
import irssibot.user.*;

/**
 * core code for IrssiBot the irc-bot
 *
 * @author Matti Dahlbom 
 * @version $Name:  $ $Revision: 1.2 $
 */
public class Core 
{
    /* some statics */
    public static final String modulesPackageName = "irssibot.modules";
    public static final String botVersion = "IrssiBot 1.0.4 for Java Copyright (C) 2000-2001 Matti Dahlbom";

    public static final String moduleStateFilePath = "modules/irssibot/modules/state/";
    public static final String configFilePath = "config/irssibot-conf.xml";

    /**
     * Contains ServerConnection objects
     */
    private Vector serverInstances = null;
    private ThreadGroup serverInstanceThreads = null;
    private ModuleHandler moduleHandler = null;
    private Date startedTime = null;
    private Date moduleStateSaved = null;

    /* database related */
    private static final String dbDriver = "org.gjt.mm.mysql.Driver";

    private String dbHostName = null;
    private String dbHostPort = null;
    private String dbUserName = null;
    private String dbUserPassword = null;

    private String dateFormatString = null;

    public String getDateFormatString() { return dateFormatString; }

    public Core()
    {
	startedTime = new Date();
	moduleStateSaved = new Date();

	putlog("constructed");
	serverInstances = new Vector();
	serverInstanceThreads = new ThreadGroup("server instances");
    }

    public Vector getServerInstances() { return serverInstances; }
    public ModuleHandler getModuleHandler() { return moduleHandler; }

    /**
     * Broadcasts a message to all channels of all server instances.
     *
     * @param message message to broadcast
     */
    public void globalChannelBroadcast(String message) 
    {
	Enumeration connections = serverInstances.elements();
	while( connections.hasMoreElements() ) {
	    ServerConnection connection = (ServerConnection)connections.nextElement();
	    connection.channelBroadcast(message);
	}
    }
    
    /**
     * Tells module handler to save all modules' state.
     *
     * @param force if true, disregard the 5min timelimit and save anyhow. If false, 
     *              don't save unless 5 minutes has passed from last save.
     */
    public void saveModuleStates(boolean force)
    {
	Date now = new Date();

	/* if last module state save time older than 5 minutes, save */
	if( ((now.getTime() - moduleStateSaved.getTime()) > 30000) || force ) {
	    moduleStateSaved = now;
	    AbstractModule modules[] = moduleHandler.getModuleTable();
	    for( int i = 0; i < modules.length; i++ ) {
		moduleHandler.saveModuleState(modules[i]);
	    }
	}
    }

    /**
     * Connects to the given database.
     *
     * @param dbName name of database to connect to
     */
    public Connection connectDB(String dbName)
    {
	String url = "jdbc:mysql://"+dbHostName+":"+dbHostPort+"/"+dbName;
	Connection connection = null;

	/* register SQL driver and connect to db */
	try {
	    Driver driver = (Driver)Class.forName(dbDriver).newInstance();
	    DriverManager.registerDriver(driver);
	    putlog("connectDB(): connecting to "+url+" as "+dbUserName);
	    connection = DriverManager.getConnection(url,dbUserName,dbUserPassword);
	} catch( Exception e ) { 
	    e.printStackTrace(); 
	    putlog("connectDB(): connection failed");
	    return null; 
	} 
	putlog("connectDB(): connection ok");

	return connection;
    }

    /**
     * Requests XML configuration from each server instance and
     * writes the config file.
     *
     */
    private void saveConfigFile()
    {
	String xml = "";

	xml += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n";
	xml += "<!-- main configuration file for IrssiBot / created "+new Date().toString()+" -->\n";
	xml += "<irssibot-config>\n";

	/* construct XML for general info */
	xml += "  <general>\n";
	xml += "    <dateformat>"+dateFormatString+"</dateformat>\n";
	xml += "  </general>\n\n";
	
	/* construct XML for module list */
	AbstractModule modules[] = moduleHandler.getModuleTable();
	xml += "  <module-list>\n";

	for( int i = 0; i < modules.length; i++ ) {
	    String name = modules[i].getClass().getName();
	    name = name.substring(name.lastIndexOf('.')+1);
	    xml += "    <module>"+name+"</module>\n";
	}

	xml += "  </module-list>\n\n";

	xml += "  <database>\n";
	xml += "    <host hostname=\""+dbHostName+"\" hostport=\""+dbHostPort+"\" />\n";
	xml += "    <user name=\""+dbUserName+"\" password=\""+dbUserPassword+"\" />\n";
	xml += "  </database>\n\n";
	
	/* construct XML for server instance list */
	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    xml += connection.getXML()+"\n";
	}
	xml += "</irssibot-config>\n";

	try {
	    FileOutputStream out = new FileOutputStream(configFilePath);
	    out.write(xml.getBytes());
	    out.flush();
	    out.close();
	} catch( IOException e ) { /* dont handle */ }
    }

    /**
     * Loads a module
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String loadModule(String params[],ServerConnection caller,String source)
    {
	AbstractModule loadedModule = null;
	Class loadedClass = null;

	if( (params == null) || (params.length != 1) ) 
	    return new String("loadModule(): incorrect number of arguments");

	String className = modulesPackageName + "." + params[0];

	try {
	    ModuleLoader loader = new ModuleLoader();
	    loadedClass = loader.loadClass(className);
	} catch( ClassNotFoundException e ) {
	    return new String("loadModule(): could not find module " + className);
	}

	if( loadedClass == null ) {
	    return null;
	}

	try {
	    loadedModule = (AbstractModule)loadedClass.newInstance();
	} catch( InstantiationException ie ) {
	    return new String("loadModule(): " + className + ": caught " + 
			      ie.getClass().getName() + ": " + ie.getMessage());
	} catch( IllegalAccessException ie ) {
	    return new String("loadModule(): " + className + ": caught " + 
			      ie.getClass().getName() + ": " + ie.getMessage());
	} catch( ClassCastException ce ) {
	    return new String("loadModule(): " + className + ": invalid module.");
	}

	/* add to module handling */
	if( moduleHandler.addModule(className,loadedModule) )
	    return new String("loadModule(): module "+className+" loaded.");
	else 
	    return new String("loadModule(): error loading module "+className);
    }

    /**
     * Lists info about loaded modules
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String listLoadedModules(String params[],ServerConnection caller,String source)
    {
	if( params != null )
	    return new String("listLoadedModules(): incorrect number of arguments");

	AbstractModule modules[] = moduleHandler.getModuleTable();
	if( (modules != null) && modules.length > 0 ) {
	    caller.write("PRIVMSG "+source+" :Modules loaded:\n");
	    for( int i = 0; i < modules.length; i++ ) {
		String msg = modules[i].getModuleInfo()+" ("+modules[i].getClass().getName()+")";
		caller.write("PRIVMSG "+source+" :  "+msg+"\n");
	    }
	} else {
	    caller.write("PRIVMSG "+source+" :No modules loaded.\n");
	}
	
	return null;
    }

    /**
     * Unload a module
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String unloadModule(String params[],ServerConnection caller,String source)
    {
	if( (params == null) || (params.length != 1) ) 
	    return new String("unloadModule(): incorrect number of arguments");

	String className = modulesPackageName+"."+params[0];
	String ret = null;

	if( moduleHandler.removeModule(className) ) {
	    ret = new String("unloadModule(): module irssibot.modules." + params[0] + " unloaded.");
	} else {
	    ret = new String("unloadModule(): module irssibot.modules." + params[0] + " not found.");
	}

	return ret;
    }

    /**
     * Lists all callers channel connects
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String listChannelConnects(String params[],ServerConnection caller,String source)
    {
	String connects = "";
	int numConnects = 0;

	if( params != null )
	    return new String("listChannelConnects(): incorrect number of arguments");

	/* list channel connects for all server instances */
	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    Vector v = connection.getChannelConnects();

	    if( v != null ) {
		for( int j = 0; j < v.size(); j++ ) {
		    ChannelConnect connect = (ChannelConnect)v.elementAt(j);
		    String msg = connection.getInstanceData().getNetwork()+connect.sourceChannel;
		    msg += " -> "+connect.destinationNetwork.getInstanceData().getNetwork()+connect.destinationChannel;
		    connects += msg + "|";
		    numConnects++;
		}
	    }
	}

	if( numConnects == 0 ) {
	    caller.write("PRIVMSG "+source+" :no channel connects.\n");
	} else {
	    caller.write("PRIVMSG "+source+" :registered channel connects:\n");

	    String list[] = StringUtil.separate(connects,'|');
	    for( int i = 0; i < list.length; i++ ) {
		caller.write("PRIVMSG "+source+" :  "+list[i]+"\n");
	    }
	}

	return null;
    }

    /**
     * Removes all callers channel connects
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String removeChannelConnects(String params[],ServerConnection caller,String source)
    {
	if( (params == null) || (params.length != 1) ) 
	    return new String("removeChannelConnect(): incorrect number of arguments");

	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    /* look for fromNetwork */
	    if( connection.getInstanceData().getNetwork().equalsIgnoreCase(params[0]) ) {
		Vector v = connection.getChannelConnects();
		if( v != null ) {
		    if( connection.getChannelConnects().size() > 0 ) {
			connection.removeChannelConnects();
			return new String(connection.getInstanceData().getNetwork()+": channel connects removed.\n");
		    } else {
			return new String(connection.getInstanceData().getNetwork()+": there are no channel connects\n");
		    }
		} else {
		    return new String(connection.getInstanceData().getNetwork()+": bad channel connect vector\n");
		}
	    }
	}
	return new String("no such server instance");
    }

    /**
     * Adds a channel connect.
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String addChannelConnect(String params[],ServerConnection caller,String source)
    {
	String ret = null;
	
	int index1 = 0;
	int index2 = 0;

	if( (params == null) || (params.length != 2) ) 
	    return new String("addChannelConnect(): incorrect number of arguments");

	index1 = params[0].indexOf('#');
	index2 = params[1].indexOf('#');

	if( (index1 == -1) || (index2 == -1 ) )
	    return new String("addChannelConnect(): invalid arguments");

	String fromNetwork = params[0].substring(0,index1);
	String fromChannel = params[0].substring(index1);
	String toNetwork = params[1].substring(0,index2);
	String toChannel = params[1].substring(index2);

	ServerConnection fromConnection = null;
	ServerConnection toConnection = null;

	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    /* look for fromNetwork */
	    if( connection.getInstanceData().getNetwork().equalsIgnoreCase(fromNetwork) ) 
		fromConnection = connection;
	    /* look for toNetwork */
	    if( connection.getInstanceData().getNetwork().equalsIgnoreCase(toNetwork) ) 
		toConnection = connection;
	}

	if( (fromConnection == null) || (toConnection == null) )
	    return new String("addChannelConnect(): invalid network");

	ret = fromConnection.addChannelConnect(fromChannel,toConnection,toChannel);

	if( ret == null )
	    return new String("addChannelConnect(): added new channel connect "+params[0]+" -> "+params[1]);
	else
	    return ret;
    }

    /**
     * Forces the bot to write out its userfile and the state of all loaded modules.
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String save(String params[],ServerConnection caller,String source)
    {
	if( params != null )
	    return new String("save(): incorrect number of arguments");

	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    connection.writeUserFile();
	}
	
	/* save module states */
	saveModuleStates(true);

	/* save config file */
	saveConfigFile();

	return new String("save(): saved.");
    }

    /**
     * Quits the bot.
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String quit(String params[],ServerConnection caller,String source)
    {
	String msg = null;

	if( params == null )
	    msg = "Leaving";
	else {
	    if( params.length > 1 )
		return new String("quit(): incorrect number of arguments");
	    else
		msg = params[0]; 
	}
	
	for( int i = 0; i < serverInstances.size(); i++ ) {
	    ServerConnection connection = (ServerConnection)serverInstances.elementAt(i);
	    connection.quit(msg);
	}
	return new String("quit(): all instances quitted..");
    }

    /**
     * Shows info about bot.
     *
     * @param params array of String parameters
     * @param caller calling server instance
     * @param source nick or channel where call originated from
     */
    private String info(String params[],ServerConnection caller,String source)
    {
	caller.write("PRIVMSG "+source+" :I am "+botVersion+".\n");

	/* calculate uptime */
	Date now = new Date();
	long secs = (now.getTime() - startedTime.getTime()) / 1000;
	int days = (int)(secs / 86400);
	secs = secs % 86400;
	int hrs = (int)(secs / 3600);
	secs = secs % 3600;
	int mins = (int)(secs / 60);
	secs = secs % 60;
	String uptime = days+" days, "+hrs+" hours, "+mins+" minutes and "+secs+" seconds.";

	caller.write("PRIVMSG "+source+" :I have been running "+uptime+"\n");
	caller.write("PRIVMSG "+source+" :Java VM version: "+System.getProperty("java.version")+", "+
		     "Operating System: "+System.getProperty("os.name")+" (version "+System.getProperty("os.version")+")\n");
		     

	return null;
    }

    /**
     * parse and execute a core call 
     * @param message message defining the call
     * @return null on success; error description otherwise
     */
    public String processCoreCall(IrcMessage message,ServerConnection caller,String source)
    {
	String call = null;
	int index1;
	int index2;
	String params[] = null;
	String method = null;

	call = message.trailing.substring(new String("core->").length());
	
	/* isolate method name and parameters */
	index1 = call.indexOf('(');
	index2 = call.indexOf(')');

	if( (index1 == -1) || (index2 == -1 ) )
	    return new String("bad call");

	method = call.substring(0,index1);
	params = StringUtil.separate(call.substring(index1+1,index2),',');

	/* handle call */
	if( method.equals("addChannelConnect") ) {
	    return addChannelConnect(params,caller,source);
	} else if( method.equals("listChannelConnects") ) {
	    return listChannelConnects(params,caller,source);
	} else if( method.equals("removeChannelConnects") ) {
	    return removeChannelConnects(params,caller,source);
	} else if( method.equals("loadModule") ) {
	    return loadModule(params,caller,source);
	} else if( method.equals("unloadModule") ) {
	    return unloadModule(params,caller,source);
	} else if( method.equals("listLoadedModules") ) {
	    return listLoadedModules(params,caller,source);
	} else if( method.equals("quit") ) {
	    return quit(params,caller,source);
	} else if( method.equals("save") ) { 
 	    return save(params,caller,source); 
	} else if( method.equals("info") ) { 
	    return info(params,caller,source); 
	} else { 
	    /* default */
	    return new String(method+": no such method");
	}
    }

    private void launch()
    {
	ConfigParser parser = null;
	String arg[] = new String[1];

	/* read configuration */
	parser = new ConfigParser();
	parser.parse();

	/* get database related data */
	dbHostName = parser.getDBHostName();
	dbHostPort = parser.getDBHostPort();
	dbUserName = parser.getDBUserName();
	dbUserPassword = parser.getDBUserPassword();

	dateFormatString = parser.getDateFormatString();

	Vector instanceData = parser.getInstanceData();

	/* launch server instances */
	for( int i = 0; i < instanceData.size(); i++ ) {
	    putlog("launching server instance #"+i);
	    ServerInstanceData instance = (ServerInstanceData)instanceData.elementAt(i);
	    ServerConnection connection = new ServerConnection(serverInstanceThreads,instance,this);
	    connection.setPriority(Thread.MAX_PRIORITY);
	    connection.start();
	    serverInstances.add(connection);
	}

	/* load initial modules from config file. launch module handler */
	moduleHandler = new ModuleHandler(this);
	if( parser.initialModules != null ) {
	    for( int i = 0; i < parser.initialModules.size(); i++ ) {
		arg[0] = (String)parser.initialModules.elementAt(i);
		putlog("launch(): "+loadModule(arg,null,null));
	    }
	}

	/* wait for all threads to exit */
	boolean alive = true;
	while( alive ) {
	    boolean doKill = true;
	    for( int i = 0; i < serverInstances.size(); i++ ) { 
		ServerConnection connection = (ServerConnection)serverInstances.elementAt(i); 
		if( connection.isAlive() ) { 
		    doKill = false; 
		} 
	    }    

	    if( doKill ) {
		alive = false;
	    }

	    /* sleep for a moment */
	    try {
		Thread.currentThread().sleep(100);
	    } catch( InterruptedException e ) {
		putlog("InterruptedException: "+e.getMessage());
	    }
	}

	putlog(save(null,null,null));
	putlog("core exiting..");

	System.exit(0);
    }

    public static void main(String ArgV[])
    {
	Core core = new Core();
	core.launch();
    }
	
    private void putlog(String msg)
    {
	String logMsg = getClass().getName()+": "+msg+"\n";
	System.out.print(logMsg);
    }
}



