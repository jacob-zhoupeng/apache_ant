/*/
 * BotDatabaseSecurityManager.java
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
 * 10-06-2000: cjw	Implemented encrypted password support.
 * 05-06-2000:  mw	Added phoney changePassword method
 * 15-05-2000: cjw	Initial support for multiple databases and 
 *					concrete for PostgreSQL.
 * 15-05-2000: cjw	Added a global variable database_connection.
 *					The various methods that interact with the 
 *					database use this variable through the method 
 *					getConnection to get a connection to the db.
 *					Changed the user column of the useraccess table
 *					to username because of a keyword problem in 
 *					PostgreSQL.
 * 15-05-2000: cjw	A fake channel for network-wide privileges is
 *					now added when a new network is created.
 *					These network-wide privileges can now be 
 *					given to users using the appropriate 
 *					setPrivilege method.
 * 15-05-2000: cjw	removed privileges table because the privileges
 *					are hardcoded into various plugins.
 *					Removed id column from servers table.
 * 11-03-2000: cjw	removed a bug in addPrivilege & removePrivilege
 * 09-03-2000: cjw	added the security management methods
 * 28-02-2000: mw       changed all BotUser's to OakUser's added
 *                      default implementations for addUser(), 
 *                      removeUser(), setPrivilege(), and the
 *                      new hasAccess() method.
 * 24-02-2000: mw       moved the constructor into init(Oak);
 * 19-02-2000: cjw	start, version 1.0
 * 
/*/

/**
 * An implementation of BotSecurityManager that checks access of 
 * nicks according to a database that is accessed through JDBC.
 * The code is currently targeted at mSQL and PostgreSQL.
 * For help on the publicly accessible methods, see the
 * BotSecurityManager interface.
 * The database should contain the following tables:
 * 
 * Table    = users
 *  +-----------------+----------+--------+----------+
 *  |     Field       |   Type   | Length | Not Null |
 *  +-----------------+----------+--------+----------+
 *  | name            | char     | 15     | Y        |
 *  | password        | char     | 15     | Y        |
 *  | long_name       | char     | 255    | Y        |
 *  +-----------------+----------+--------+----------+
 * 
 * Table    = networks
 *  +-----------------+----------+--------+----------+
 *  |     Field       |   Type   | Length | Not Null |
 *  +-----------------+----------+--------+----------+
 *  | name            | char     | 15     | Y        |
 *  | description     | char     | 255    | Y        |
 *  +-----------------+----------+--------+----------+
 * 
 * Table    = useraccess
 *  +-----------------+----------+--------+----------+
 *  |     Field       |   Type   | Length | Not Null |
 *  +-----------------+----------+--------+----------+
 *  | username        | char     | 15     | Y        |
 *  | privilige       | char     | 15     | Y        |
 *  | channel         | uint     | 4      | Y        |
 *  +-----------------+----------+--------+----------+
 * 
 * Table    = servers
 *  +-----------------+----------+--------+----------+
 *  |     Field       |   Type   | Length | Not Null |
 *  +-----------------+----------+--------+----------+
 *  | address         | char     | 255    | Y        |
 *  | description     | char     | 255    | Y        |
 *  | port            | uint     | 4      | Y        |
 *  | network         | char     | 15     | Y        |
 *  +-----------------+----------+--------+----------+
 * 
 * Table    = channels
 *  +-----------------+----------+--------+----------+
 *  |     Field       |   Type   | Length | Not Null |
 *  +-----------------+----------+--------+----------+
 *  | id              | uint     | 4      | Y        |
 *  | name            | char     | 255    | Y        |
 *  | network         | char     | 15     | Y        |
 *  | description     | char     | 255    | Y        |
 *  +-----------------+----------+--------+----------+
 * 
 * @version 1.0
 * @date 19-02-2000
 * @author Christiaan Welvaart
 */

package org.moxy.oak.security;

import java.util.Vector;
import java.sql.*;
import org.moxy.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.util.config.*;

public class BotDatabaseSecurityManager implements BotSecurityManager
{
	String url;
	Connection database_connection;
	Vector /* <OakUser> */ users;
	int database_type;
	Object object = "";
	
	static final String[] userKey = {"name"};
	static final String[] networkKey = {"code"};
	static final String[] useraccessKey = {"username", "privilege", "channel"};
	static final String[] serverKey = {"address", "port", "network"};
	static final String[] channelKey = {"name", "network"};
	
	static final String GLOBAL_CHANNEL = "@GLOBAL";
	static final String GLOBAL_NETWORK = "@GLOBAL";
	static final String OWNER_PRIVILEGE = "@OWNER";
	
	static private String[] dbtypes = {"msql", "mysql", "postgresql"};
	static final int DB_MSQL = 1;
	static final int DB_MYSQL = 2;
	static final int DB_POSTGRESQL = 3;

	/**
	* Creates a new databased bot security manager.
	* See the intro text for into about the database structure required.
	* This class should only be accessed as an implementor of BotSecurityManager.
	* See the documentation of that interface for more information.
	*/
	public BotDatabaseSecurityManager()
	{
	}

	public void init(Oak bot)
	{
		String dbtype_name;
		int i;
	
		System.out.println("BDSM starting up..."); 
		this.url = bot.getProperty("oak.smprefs.url",null);
			dbtype_name = bot.getProperty("oak.smprefs.dbtype", null);
		if (dbtype_name == null)
			this.database_type = 0;
		else
			for (i = 0; i < dbtypes.length; i++)
				if (dbtype_name.equals(dbtypes[i]))
					this.database_type = i + 1;
		

		if(this.url == null || this.database_type == 0)
			System.err.println("ERROR: database not correctly specified, security manager will not function");

/*
		{
			try
			{
				java.io.BufferedReader in = new java.io.BufferedReader(
                            		new java.io.InputStreamReader(System.in));
				System.out.print("What is the URL for the security database:");
				this.url = in.readLine();
				this.database_type = 0;
				while (this.database_type == 0)
				{
					System.out.println("What is the type of the database (1=msql,2=mysql,3=postgresql):");
					try {this.database_type = Integer.parseInt(in.readLine());}
					catch(NumberFormatException e) {}
				}
				in = null;
			}
			catch(java.io.IOException ioe)
			{
				System.err.println(ioe);
				url = "";
			}
			bot.putProperty("bdsm.security.url",this.url);
			bot.putProperty("bdsm.security.dbtype", Integer.toString(this.database_type));
		}
*/
		users = new Vector();
		database_connection = null;
	}
	
	
	
	static public GroupItem[] getConfigurables(int page)
	{
		GroupItem[] result;
		
		switch(page)
		{
			case 1:
				result = new GroupItem[2];
				result[0] = new ChoiceGroupItem("dbtype", "Database type", "Database type", 
												dbtypes, dbtypes);
				result[1] = new SimpleGroupItem("url", "URL for database connections", 
											 "URL for database connections", 
											 "url");
				break;
			default:
				result = null;
		}
		return result;
	}
	
	private Connection getConnection() throws SQLException
	{
		if (database_connection == null)
			database_connection = DriverManager.getConnection(url, "cjw", "test");
		
		return database_connection;
	}
	
	private boolean isOwner(String user)
	{
		return hasAccess(user, GLOBAL_NETWORK, GLOBAL_CHANNEL, OWNER_PRIVILEGE);
	}
	
	public OakUser login(CommandConnection connection, String nick, String userName, String password)
	{
		OakUser user;
	
		System.out.println("BDSM|LOGIN: " + nick + " " + userName + " " + password);
	
		if (checkPassword(userName, password))
		{
			if (isOwner(userName))
			{
				System.out.println("recognized " + userName + " as owner");
				user = new OakUser(connection, userName, nick, object, true);
			}
			else
				user = new OakUser(connection, userName, nick, object);
				
			users.addElement(user);
			
			return user;
		}
		return null;
	}
	
	public void logout(CommandConnection connection, String nick)
	{
		OakUser user;
		
		user = getUser(connection, nick);
		if (user == null)
			return; // should be reported...
		
		users.removeElement(user);
	}
	
	public void nickChanged(CommandConnection connection, String oldNick, String newNick)
	{
		OakUser user;
		
		System.out.println("BDSM|NickChange: " + oldNick + " -> " + newNick);
		user = getUser(connection, oldNick);
		if (user == null)
			return;
		user.setNick(newNick, object);
	}
	
	public String getUserNameFromNick(CommandConnection connection, String nick)
	{
		OakUser user;
		
		user = getUser(connection, nick);
		if (user == null)
			return null;
		return user.getUserName();
	}
	
	public boolean isLoggedIn(CommandConnection connection, String nick)
	{
		OakUser user;
		
		user = getUser(connection, nick);
		return (user != null);
	}

	public boolean hasAccess(String nick, CommandConnection connection, String privilege)
	{
		OakUser user;

		user = getUser(connection, nick);
		if (user == null)
			return false;

		if (user.isOwner())
			return true;
		
		return hasAccess(user.getUserName(), GLOBAL_NETWORK, GLOBAL_CHANNEL, privilege);
	}
	
	public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String privilege)
	{
		return hasAccess(nick, hostConnection, targetConnection, GLOBAL_CHANNEL, privilege);
	}

	public boolean hasAccess(String nick, IRCCommandConnection connection, String channel, String privilege)
	{
		
	return hasAccess(nick, connection, connection, channel, privilege);
	}

	public boolean hasAccess(String nick, CommandConnection connection, String network, String channel,String privilege)
	{
		OakUser user;

		user = getUser(connection, nick);
		if (user == null)
			return false;
		
		if (user.isOwner())
			return true;
		
		return hasAccess(user.getUserName(), network, channel, privilege);
	}
	
	public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String channel, String privilege)
	{
		String network;
		OakUser user;
		
		System.out.println("BSDM|ACCESS: nick=" + nick + " channel="+ channel + " privilege=" + privilege);
		
		user = getUser(hostConnection, nick);
		if (user == null)
			return false;
		
		if (user.isOwner())
			return true;
		
		network = getNetwork(targetConnection.getServer(), targetConnection.getPort());
		if (network == null)
			return false;
		
		return hasAccess(user.getUserName(), network, channel, privilege);
	}

	private boolean hasAccess(String user, String targetNetwork, String channel, String privilege)
	{
		int channelNum;
		
		try
		{
			channelNum = getChannelNum(targetNetwork, channel);
		}
		catch(BotSecurityException e)
		{
			return false;
		}
		
		if (channelNum < 0)
			return false;
		
		return hasAccess(user, channelNum, privilege);
	}
	
	private boolean hasAccess(String user, int channel, String privilege)
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		
		try
		{
			dbconn = getConnection();
			stmt = dbconn.createStatement();
			query = "SELECT * FROM useraccess WHERE username=\'" + user + "\' AND channel=" + 
			         channel + " AND privilege=\'" + privilege + "\'";
			rslt = stmt.executeQuery(query);
			if (rslt.next())
				rslt.getString(1);
			else
				throw new BQLException("query gave no result: " + query);
		}
		catch(Exception e)
		{
			System.out.println("Access for " + privilege + " denied to " + user);
			return false;
		}

		return true;
	}
	
	private boolean checkPassword(String userName, String passwordToCheck)
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		String password;
		
		try
		{
			dbconn = getConnection();
			stmt = dbconn.createStatement();

			query = "SELECT password FROM users WHERE name=\'"+userName+"\'";
			rslt = stmt.executeQuery(query);
			if (rslt.next())
				password = rslt.getString(1);
			else
				throw new BQLException("query gave no result: " + query);
		}
		catch(Exception e)
		{
			System.out.println("Wrong password given or user unknown: " + userName);
			return false;
		}
		
		return org.moxy.security.Crypt.check(password, passwordToCheck);
	}
	
	private OakUser getUser(CommandConnection connection, String nick)
	{
		int i;
 		OakUser user;
		
		for (i = 0; i < users.size(); i++)
		{
			user = (OakUser)users.elementAt(i);
			if (user.match(connection, nick))
			return user;
		}
		
		return null;
	}
	
	private String getNetwork(String server, int port)
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		String network;

		try
		{
			dbconn = getConnection();
			stmt = dbconn.createStatement();

			query = "SELECT network FROM servers WHERE address=\'" + server + "\' AND port=" + port;
			rslt = stmt.executeQuery(query);
			if (rslt.next())
				network = rslt.getString(1);
			else
				throw new BQLException("query gave no result: " + query);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		return network;
	}
	
	private int getChannelNum(String network, String channel) throws BotSecurityException
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		int channelNum;
		
		try
		{
			dbconn = getConnection();
			stmt = dbconn.createStatement();

			query = "SELECT id FROM channels WHERE name=\'" + channel + "\' AND network=\'" + network + "\'";
			rslt = stmt.executeQuery(query);
			if (rslt.next())
				channelNum = rslt.getInt(1);
			else
				throw new BQLException("query gave no result: " + query);
		}
		catch(Exception e)
		{
			throw new BotSecurityException("channel " + channel + " on " + network + " doesn't exist");
		}
		
		return channelNum;
	}
	
	// Get the value of an msql sequence on a table.
	private String getNextID(String table, Connection dbconn)
	{
		Statement stmt;
		ResultSet rslt;
		String query;

		try
		{
			stmt = dbconn.createStatement();
			query = "SELECT _seq FROM " + table;
			rslt = stmt.executeQuery(query);
			if (rslt.next())
				return rslt.getString(1);
			else
				return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
 
	private int addRow(String table, IRCLine row) throws BotSecurityException
	{
		String temp_string;
		int i;
		String query;
		StringBuffer querybuf;
		ResultSetMetaData metadata;
		Connection dbconn;
		Statement stmt;
		ResultSet rslt;
		String id;
		int numColumns;
		
		id = null;
		
		try {
			dbconn = getConnection();

			metadata = getMetaData(table);

			if (metadata.getColumnName(1).equalsIgnoreCase("id"))
				switch(database_type)
				{
					case DB_MSQL:
						id = getNextID(table, dbconn);
						break;
					case DB_POSTGRESQL:
						id = "NEXTVAL(\'" + table + "_id_seq\')";
						break;
					default:
						id = "0";
				}

			// insert the row
			stmt = dbconn.createStatement();
			querybuf = new StringBuffer("INSERT INTO ");
			querybuf.append(table);
			querybuf.append(" VALUES (");

			numColumns = metadata.getColumnCount();

			if (id != null)
			{
				querybuf.append(id + ", ");
				i = 1;
			}
			else
				i = 0;

			for (; i < numColumns; i++)
			{
				if (!row.hasMoreTokens())
					throw new BotSecurityException("incorrect number of fields in INSERT; expected " + numColumns + ", got " + i);

				temp_string = row.getNextToken();

				querybuf.append(printValue(metadata, i + 1, temp_string));

				if ( i != (numColumns - 1))
					querybuf.append(", ");
			}

			querybuf.append(")");

			query = querybuf.toString();
			return stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			throw new BotSecurityException(e.toString());
		}
	}
	
	private String printWhereOnKey(ResultSetMetaData metadata, String[] fieldNames, IRCLine key) throws BotSecurityException
	{
		StringBuffer querybuf;
		int i;
		String keyValue;
		
		querybuf = new StringBuffer("WHERE ");
						
		for (i = 0; i < fieldNames.length; i++)
		{
			if (!key.hasMoreTokens())
				throw new BotSecurityException("incorrect number of fields in key; expected " + fieldNames.length + ", got " + i);
			
			keyValue = key.getNextToken();
			if (keyValue.equals("*"))
				continue;
			
			querybuf.append(fieldNames[i]);
			querybuf.append("=");
			try
			{
				querybuf.append(printValue(metadata, fieldNames[i], keyValue));
			}
			catch(SQLException e)
			{
				throw new BotSecurityException(e.toString());
			}
		
			querybuf.append(" ");
			if ( i != (fieldNames.length - 1))
				querybuf.append("AND ");
		}
		
		return querybuf.toString();
	}
	
	private int removeRow(String table, String[] fieldNames, IRCLine key) throws BotSecurityException
	{
		String query;
		ResultSetMetaData metadata;
		Connection dbconn;
		Statement stmt;
		ResultSet rslt;
		
		try {
			metadata = getMetaData(table);
			dbconn = getConnection();
			stmt = dbconn.createStatement();
			query = "DELETE FROM " + table + " " + printWhereOnKey(metadata, fieldNames, key);
			return stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			throw new BotSecurityException(e.toString());
		}
	}
	
	private int getColumnIndex(ResultSetMetaData metadata, String column) throws SQLException
	{
		int i, n;
		
		n = metadata.getColumnCount();
		for (i = 1; i <= n; i++)
			if (metadata.getColumnName(i).equalsIgnoreCase(column))
				return i;
		return -1;
	}
	
	private String printValue(ResultSetMetaData metadata, String column, String value) throws SQLException
	{
		int index;
		
		index = getColumnIndex(metadata, column);
		if (index < 0)
			return null;
		
		return printValue(metadata, index, value);
	}
	
	private String printValue(ResultSetMetaData metadata, int column, String value) throws SQLException
	{
		switch(metadata.getColumnType(column))
		{
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DECIMAL:
				return value;
			default:
				return "\'" + value + "\'";
		}
	}
	
	ResultSetMetaData getMetaData(String table) throws SQLException
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		
		dbconn = getConnection();
		stmt = dbconn.createStatement();
		query = "SELECT * FROM " + table + " WHERE ";
		switch(database_type)
		{
			case DB_MSQL:
				query += "_rowid=-1";
				break;
			case DB_POSTGRESQL:
				query += "false";
				break;
			default:
				query +="false";
		}
		rslt = stmt.executeQuery(query);
		return rslt.getMetaData();
	}
	
	private int modifyRow(String table, String[] fieldNames, IRCLine key, String column, String value) throws BotSecurityException
	{
		String query;
		ResultSetMetaData metadata;
		Connection dbconn;
		Statement stmt;
		ResultSet rslt;
		
		try {
			metadata = getMetaData(table);
			dbconn = getConnection();
			stmt = dbconn.createStatement();
			query = "UPDATE " + table + " SET " + column + " = " + 
		        	printValue(metadata, column, value) + " WHERE " +
					printWhereOnKey(metadata, fieldNames, key);			
			System.out.println(query);
			return stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			throw new BotSecurityException(e.toString());
		}
	}
	
	private void removeAllPrivileges(String user) throws BotSecurityException
	{
		Connection dbconn;
		Statement stmt;
		String query;
		int result;
		
		try 
		{
			dbconn = getConnection();
			stmt = dbconn.createStatement();
			query = "DELETE FROM useraccess WHERE username=\'" + user + "\'";
			result = stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			throw new BotSecurityException(e.toString());
		}
		
		if (result < 1)
			throw new BotSecurityException("removeAllPrivileges: too few records affected; expected 1, got " + result);
	}

	// mask is currently not used
	public void addUser(String user, String password, String mask) throws BotSecurityException
	{
		IRCLine line;
		int result;
		
		System.out.println("addUser: user=" + user + ", password=" + password + ", mask=" + mask);
		
		line = new IRCLine("");
		line.putBack("no_description");
		line.putBack(org.moxy.security.Crypt.crypt(password));
		line.putBack(user);
		
		result = addRow("users", line);
		if (result > 1)
			throw new BotSecurityException("addUser: too many records affected; expected 1, got " + result);
		if (result < 1)
			throw new BotSecurityException("addUser: too few records affected; expected 1, got " + result);
	}
	
	public void removeUser(String user) throws BotSecurityException
	{
		IRCLine line;
		int result;
		
		line = new IRCLine("");
		line.putBack(user);
		
		result = removeRow("users", userKey, line);
		if (result > 1)
			throw new BotSecurityException("removeUser: too many records affected; expected 1, got " + result);
		if (result < 1)
			throw new BotSecurityException("removeUser: too few records affected; expected 1, got " + result);
		
		removeAllPrivileges(user);
	}

	public void setPrivilege(String user, String privilege, boolean setting, String network) throws BotSecurityException
	{
		setPrivilege(user, privilege, setting, network, GLOBAL_CHANNEL);
	}

	public void setPrivilege(String user, String privilege, boolean setting, String network, String channel) throws BotSecurityException
	{
		boolean privilegeState;
	
		privilegeState = hasAccess(user, network, channel, privilege);
		
		if (setting && privilegeState || !setting && !privilegeState)
			return;
		
		if (setting)
			addPrivilege(user, privilege, network, channel);
		else
			removePrivilege(user, privilege, network, channel);
	}
	
	private void addPrivilege(String user, String privilege, String network, String channel) throws BotSecurityException
	{
		int channelNum;
		IRCLine line;
		
		channelNum = getChannelNum(network, channel);

		line = new IRCLine("");
		line.putBack(Integer.toString(channelNum));
		line.putBack(privilege);
		line.putBack(user);
		addRow("useraccess", line);
	}
	
	private void removePrivilege(String user, String privilege, String network, String channel) throws BotSecurityException
	{
		int channelNum;
		IRCLine line;
		
		channelNum = getChannelNum(network, channel);
		
		line = new IRCLine("");
		line.putBack(Integer.toString(channelNum));
		line.putBack(privilege);
		line.putBack(user);
		removeRow("useraccess", useraccessKey, line);
	}

	public void addNetwork(String name, String description) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(description);
		line.putBack(name);
		addRow("networks", line);
		
		addChannel(GLOBAL_CHANNEL, name, "fake channel for privileges set for all channels on a network");
	}
	
	public void removeNetwork(String name) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(name);
		removeRow("networks", networkKey, line);
		
		// remove all channels on this network!!!
		// + all the privileges for these channels!!!
//		DELETE FROM useraccess where channel in (select id from channels where network=name)
//		line = new IRCLine("");
//		line.putBack("*");
//		line.putBack(name);
//		removeRow("channels", channelKey, line);
	}
	
	public void addChannel(String name, String network, String description) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(description);
		line.putBack(network);
		line.putBack(name);
		addRow("channels", line);
	}
	
	public void removeChannel(String name, String network) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(network);
		line.putBack(name);
		removeRow("channels", channelKey, line);
	}
	
	public void addServer(String address, String port, String network, String description) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(network);
		line.putBack(port);
		line.putBack(description);
		line.putBack(address);
		addRow("servers", line);
	}
	
	public void removeServer(String address, String port, String network) throws BotSecurityException
	{
		IRCLine line;
		
		line = new IRCLine("");
		line.putBack(network);
		line.putBack(port);
		line.putBack(address);
		removeRow("servers", serverKey, line);
	}
	
	public void changePassword(String nick, String user, String oldpassword, String newPassword,CommandConnection con)
	{
		try
		{
			if (checkPassword(user, oldpassword))
				modifyRow("users", userKey, new IRCLine(user), "password", 
				          org.moxy.security.Crypt.crypt(newPassword));
		}
		catch (BotSecurityException e)
		{
			e.printStackTrace();
		}
	}
}
