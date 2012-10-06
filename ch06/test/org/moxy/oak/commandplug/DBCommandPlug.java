package org.moxy.oak.commandplug;
import java.sql.*;
import java.util.*;
import org.moxy.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.oak.security.*;

public class DBCommandPlug implements CommandPlug{

	static private String spaces = "                                                                                                                                                                                                                                                                                                 ";

 	private static final String[] table_ids = 
		{"NETWORK", "CHANNEL", "SERVER", "USER", "USERACCESS"};
	private static final String[] table_names =
		{"networks", "channels", "servers", "users", "useraccess"};
	private static final String[][] table_keys =
		{
			{"code"}, 
			{"name", "network"}, 
			{"address", "port", "network"}, 
			{"name"},
			{"username", "privilege", "channel"}
		};

	private Oak bot;
	private BotSecurityManager sm;
	private String url = null;
	private Connection saved_connection = null;

	public void init(Oak b, BotSecurityManager bsm, OakCommand oc)
	{
		bot = b;
		this.sm = bsm;
		this.url = bot.getProperty("bdsm.security.url", null);
	}
	
	public String[] getCommands()
	{
		String[] ret = {"DB"};
		return ret;
	}

	public String getGroup()
	{
		return null;
	}
	
	public String getShortHelpDescription(String command)
	{
		return null;
	}
	
	public String[] getLongHelpDescription(String command)
	{
		return null;
	}

	public void doCommand(CommandConnection connection, String fnick, String command, String params)
	{
		if (!command.equalsIgnoreCase("DB"))
			return;
		
		if (!sm.hasAccess(fnick, connection, "@OWNER"))
			return;
		doDBCommand(connection, new IRCLine(params), fnick);
	}
	
	private Connection getConnection() throws SQLException
	{
		if (saved_connection == null)
			saved_connection = DriverManager.getConnection(url, "cjw", "");
		
		return saved_connection;
	}
	
private void doDBCommand(CommandConnection conn, IRCLine st, String nick)
{
	String notice;
	String command, param1, param2;

	String networkname;
	String description;
	String query;

	Connection dbconn;
	Statement stmt;
	ResultSet rslt;

	if (url == null)
	{
		conn.sendReply(nick, "DB no database specified");
		return;
	}

	try
	{
		command = st.getNextToken();
		param1 = st.getNextToken();
		
		System.out.println("command = \"" + command + "\"");
		System.out.println("param1 = \"" + param1 + "\"");
	
		if (command.equalsIgnoreCase("ADD"))
		{
			String table;
			int result;
			
			table = getTableName(param1);
			if (table == null)
				throw new BQLException("unknown table name");
			
			result = addRow(table, st);
			conn.sendReply(nick, "DB " + result + " rows added to " + table + "");
		}
		else if (command.equalsIgnoreCase("DELETE"))
		{
			String table;
			int result;
			String[] key;
			
			table = getTableName(param1);
			if (table == null)
				throw new BQLException("unknown table name");
			key = getTableKey(param1);
			if (key == null)
				throw new BQLException("unknown table name (unexpected error)");
			
			result = removeRow(table, key, st);
			conn.sendReply(nick, "DB " + result + " rows deleted from " + table + "");
		}
		else if (command.equalsIgnoreCase("LIST"))
		{
			String table;
			
			table = getTableName(param1);
			if (table == null)
				throw new BQLException("unknown table name");
			
			listTable(table, conn, nick);
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
		notice = "DB un1known error: "+e.toString();
		System.out.println(notice);
		sendNotice(conn, nick, notice);
     	return;
	}
	catch(Error e)
	{
		notice = "DB un2known error: "+e.toString();
		sendNotice(conn, nick, notice);
     	return;
	}

 }		
	
	private int getTableNum(String id)
	{
		int i;
		
		for (i = 0; i < table_ids.length; i++)
			if (table_ids[i].equalsIgnoreCase(id))
				return i;
		return -1;
	}
		
	private String getTableName(String id)
	{
		int num;
		
		num = getTableNum(id);
		if (num < 0)
			return null;
		return table_names[num];
	}
	
	private String[] getTableKey(String id)
	{
		int num;
		
		num = getTableNum(id);
		if (num < 0)
			return null;
		return table_keys[num];
	}
	
	private String getNextID(String table, Connection dbconn)
	{
		Statement stmt;
		ResultSet rslt;
		String query;

		// get sequence value
		try
		{
			stmt = dbconn.createStatement();
			query = "SELECT _seq FROM " + table;
			System.out.println(query);
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
 
 	private int addRow(String table, IRCLine row) throws Exception
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
		dbconn = getConnection();
		
		// get metadata
		stmt = dbconn.createStatement();
		query = "SELECT * FROM " + table + " WHERE _rowid=-1";
		System.out.println(query);
		rslt = stmt.executeQuery(query);
		metadata = rslt.getMetaData();
		
		if (metadata.getColumnName(1).equalsIgnoreCase("id"))
			id = getNextID(table, dbconn);
		
		// insert the row
		stmt = dbconn.createStatement();
		querybuf = new StringBuffer("INSERT INTO ");
		querybuf.append(table);
		querybuf.append(" VALUES (");
		
		numColumns = metadata.getColumnCount();
		
		System.out.println("id=" + id);
		
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
				throw new BQLException("incorrect number of fields in INSERT");
				
			temp_string = row.getNextToken();
		
			System.out.println("column type=" + metadata.getColumnType(i+1));
			
			if (temp_string.charAt(0) == '#')
				querybuf.append("\'" + temp_string + "\'");
			else
				switch(metadata.getColumnType(i+1))
				{
					case Types.BIGINT:
					case Types.INTEGER:
					case Types.SMALLINT:
					case Types.TINYINT:
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.REAL:
					case Types.DECIMAL:
						querybuf.append(temp_string);
						break;
				default:
						querybuf.append("\'" + temp_string + "\'");
				}
			if ( i != (numColumns - 1))
				querybuf.append(", ");
		}
		
		querybuf.append(")");
		
		query = querybuf.toString();
		System.out.println(query);
		return stmt.executeUpdate(query);
	}
	
	private int removeRow(String table, String[] fieldNames, IRCLine key) throws BQLException
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
		
		stmt = dbconn.createStatement();
		querybuf = new StringBuffer("DELETE FROM ");
		querybuf.append(table);
		querybuf.append(" WHERE ");
						
		for (i = 0; i < fieldNames.length; i++)
		{
			if (!key.hasMoreTokens())
				throw new BQLException("incorrect number of fields in REMOVE; expected " + fieldNames.length + ", got " + i);
			
			querybuf.append(fieldNames[i]);
			querybuf.append("=");
			
			temp_string = key.getNextToken();
		
			if (temp_string.charAt(0) == '#')
				querybuf.append("\'" + temp_string + "\'");
			else
				try
				{
					Integer.parseInt(temp_string);
					querybuf.append(temp_string);
				}
				catch(NumberFormatException e)
				{
					querybuf.append("\'" + temp_string + "\'");
				}

			querybuf.append(" ");
			if ( i != (fieldNames.length - 1))
				querybuf.append("AND ");
		}
		
		query = querybuf.toString();
		System.out.println(query);
		return stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			throw new BQLException(e.toString());
		}
	}
	
	private static void sendNotice(CommandConnection conn, String nick, String msg)
	{
		conn.sendReply(nick, msg);
	}
	
	private void listTable(String table, CommandConnection conn, String nick) throws Exception
	{
		StringBuffer rowbuf;
		String columnName;
		int i, j;
		String query;
		ResultSetMetaData metadata;
		Connection dbconn;
		Statement stmt;
		ResultSet rslt;
		
		int[] sizes;
		Vector rows;
		int numColumns;
		String[] curRow;
		boolean finished;
				
		
		dbconn = getConnection();
		
		// get metadata
		stmt = dbconn.createStatement();
		query = "SELECT * FROM " + table;
		System.out.println(query);
		rslt = stmt.executeQuery(query);
		metadata = rslt.getMetaData();
		
		numColumns = metadata.getColumnCount();
		
		sizes = new int[numColumns];
		rows = new Vector();

		
		curRow = new String[numColumns];
		
		for (i = 0; i < numColumns; i++)
		{
			curRow[i] = metadata.getColumnName(i + 1);
			sizes[i] = curRow[i].length();
		}
		rows.addElement(curRow);
				
		finished = false;
		
		while(rslt.next())
		{
			curRow = new String[numColumns];
			for (i = 0; i < numColumns; i++)
			{
				System.out.print("i="+i+", ");
				curRow[i] = rslt.getString(i+1);
				if (curRow[i].length() > sizes[i])
					sizes[i] = curRow[i].length();
			}
			rows.addElement(curRow);
		}
		
		for (i = 0; i < rows.size(); i++)
		{
			rowbuf = new StringBuffer();
			for (j = 0; j < numColumns; j++)
			{
				String curField = ((String[])rows.elementAt(i))[j];
				rowbuf.append(curField);
				rowbuf.append(spaces.substring(0, sizes[j] - curField.length()));
				if (j < (numColumns - 1))
					rowbuf.append(' ');
			}
			sendNotice(conn, nick, rowbuf.toString());
		}
		
	}



}
