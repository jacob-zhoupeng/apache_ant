/* Copyright (C) 2000 Christiaan Welvaart
   This file is part of OAK The Java Bot.

   OAK The Java Bot is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   OAK The Java Bot is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public
   License along with OAK The Java Bot; see the file COPYING.LIB.  If not,
   write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

package com.cjw.oak;

import java.util.Vector;
import java.util.Random;
import java.sql.*;
import gnu.regexp.*;
import org.moxy.irc.*;
import org.moxy.oak.*;
import org.moxy.oak.plugin.*;
import org.moxy.util.config.*;

public class TriviaPlugin implements Runnable, ChannelPlugin
{
	static private final int STATE_IDLE = 0;
	static private final int STATE_BUSY = 1;
	static private final int STATE_ASKING = 2;
	
	static private final int ANSWER_TIME = 25000;
	static private final int NEW_QUESTION_TIME = 3000;
	static private final int STATUS_TIME = 150;
	
	private String table; // = "vragen";
	private String answer_prefix; // = "ff";
	private int answer_time;
	private int new_question_time;
	private int status_time;
	
	String database_url;
	IRCConnection connection;
	String channel;
	String identifier;
	int state;
	Random random;
	TriviaState trivia_state = null;
	int[] question_ids;
	boolean finished = true;
	int current_question = 0;
	String current_answer;
	RE current_pattern;
	int total_questions;
	Thread thread = null;

		public TriviaPlugin()
	{
	}
	
	public static GroupItem[] getConfigurables(int page)
	{
		GroupItem[] result;
	
		switch(page)
		{
			case 1:
				result = new GroupItem[6];
				result[0] = new SimpleGroupItem("url", "URL for database connections", 
												"URL for database connections", 
												"url");
				result[1] = new SimpleGroupItem("table", "Table with questions", 
												"The table in the database with trivia questions", 
												"regexp/[a-z,A-Z]{1,32}/");
				
				result[2] = new SimpleGroupItem("prefix", "Answer prefix", 
					"The prefix used to signify that an irc message is a trivia answer", 
												"regexp/[a-z]{1,5}/");
				result[3] = new SimpleGroupItem("answer_time", "Answer time", 
												"The time users get to answer a question (in milliseconds)", 
												"int[1,3600000]");
				result[4] = new SimpleGroupItem("question_delay", "Time till next question", 
												"The time the bot will wait before asking " +
												"a new question after a status blurb (in milliseconds)", 
												"int[1,3600000]");
				result[5] = new SimpleGroupItem("status_delay", "Time till status", 
												"The time the bot will wait after the question was " +
												"answered or timed out (in milliseconds)", 
												"int[1,3600000]");
				break;
			default:
				return null;	
		}
		
		return result;
	}
	
	private void reset(int quota)
	{
		finished = false;
		state = STATE_IDLE;
		total_questions = 0;
		getQuestionIDs();
		trivia_state.reset(quota);
	}
	
	private void getQuestionIDs()
	{
		int numQuestions;
		Vector ids;
		int i;

		String query;
		Connection dbconn;
		Statement stmt;
		ResultSet rslt;

		question_ids = null;
		ids = new Vector(1000, 1000);
	try
	{
		dbconn = DriverManager.getConnection(database_url, "guest", "");
		
		stmt = dbconn.createStatement();
		query = "SELECT _rowid FROM " + table;
		System.out.println(query);
		rslt = stmt.executeQuery(query);

		while(rslt.next())
		{
			ids.addElement(new Integer(rslt.getInt(1)));
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
		return;
	}
	
		numQuestions = ids.size();
		question_ids = new int[numQuestions];
		for (i = 0; i < numQuestions; i++)
			question_ids[i] = ((Integer)ids.elementAt(i)).intValue();
		System.out.println("TriviaPlugin::getQuestionIDs found " + numQuestions + " questions");
	}
	
	private void handleTriviaStart(String nick, IRCLine line)
	{
		int quota;
		
		// #questions?
		System.out.println("trivia start");
		
		if (state != STATE_IDLE)
		{
			connection.sendPrivMsg(channel, "trivia is already running");
			return;
		}
		
		try {
		quota = Integer.parseInt(line.getNextToken());
		} catch (Exception e) { quota = 10;}
		
		reset(quota);
		
		connection.sendPrivMsg(channel, "Starting a trivia round, the team that first has " + quota + " good answers wins the game");
		state = STATE_BUSY;
		finished = false;
		thread = new Thread(this);
		thread.start();
	}
	
	private void handleTriviaStop(String nick, IRCLine line)
	{
		System.out.println("trivia stop");
		
		finished = true;
	}

	private void handleTriviaJoin(String nick, IRCLine line)
	{
		int teamNum = 0;
		
		System.out.println("trivia join");
		
		try {
		teamNum = Integer.parseInt(line.getNextToken());
		} catch (Exception e) {}
		
		if (teamNum < 1 || teamNum > 2)
			return;
			
		// test if nick is already in a team
		System.out.println("TriviaPlugin::handleTriviaJoin adding " + nick + " to team " + teamNum);
		trivia_state.addPlayer(teamNum, nick);
	}
	
	private void handleTriviaTeam(String nick, IRCLine line)
	{
		trivia_state.setTeam(nick, line.getNextToken());
	}
	
	private void handleTriviaAnswer(String nick, IRCLine line)
	{
		String answer;
		int team;
		
		System.out.println("trivia answer");
				
		if (!trivia_state.nameRegistered(nick))
		{
			connection.sendPrivMsg(channel, nick + ", first join a team");
			return;
		}
		
		answer = line.getRemaining();

		if (current_pattern.isMatch(answer))
		{
			state = STATE_BUSY;
			thread.interrupt();
			
			trivia_state.addPoint(nick);
			
			connection.sendPrivMsg(channel, nick + " gave the correct answer!");
			checkGameEnd();
		}
	}
	
	private void checkGameEnd()
	{
		if (!trivia_state.quotaReached())
			return;
		
		connection.sendPrivMsg(channel, "team " + trivia_state.getWinningTeamName() + " won the game");
		finished = true;
		printStatistics();
	}
	
	private void printStatistics()
	{
		int total_good_answers;
		int i, j;
		
		total_good_answers = trivia_state.getTotalScore(1) + trivia_state.getTotalScore(2);
		
		connection.sendPrivMsg(channel, Integer.toString(total_questions) + " questions asked, " + total_good_answers + " given.");
		
		for (j = 1; j < 3; j++)
		{
			for (i = 0; i < trivia_state.getNumPlayers(j); i++)
			{
				connection.sendPrivMsg(channel, trivia_state.getPlayerNameByIndex(j, i) + " gave " + 
												trivia_state.getPlayerScoreByIndex(j, i) + " good answers, this is " 
												+ Math.floor( (double) trivia_state.getPlayerScoreByIndex(j, i)/ total_good_answers * 1000)/10
												+ "%");;
			}
		}
	
	}
	
	
	
	private void handleCommand(String command, String nick, IRCLine line)
	{
		if (command.equals("!start"))
			handleTriviaStart(nick, line);
		else if (command.equals("!stop"))
			handleTriviaStop(nick, line);
		else if (command.equals("!join"))
			handleTriviaJoin(nick, line);
		else if (command.equals("!team"))
			handleTriviaTeam(nick, line);

	}
	
	public void run()
	{
		while (!finished)
		{
			getQuestion();
			askQuestion();
			state = STATE_ASKING;
			try { thread.sleep(answer_time); } catch(Exception e) {}
			if (state == STATE_ASKING)
				giveAnswer();
			else
				state = STATE_BUSY;
			try { thread.sleep(STATUS_TIME); } catch(Exception e) {}
			showStatus();
			try { thread.sleep(NEW_QUESTION_TIME); } catch(Exception e) {}
		}
		state = STATE_IDLE;
	}
	
	void getQuestion()
	{
		int i;
		
		i = random.nextInt(question_ids.length);
	
		current_question = question_ids[i];
	}
	
	String fix_pattern(String pattern)
	{
		StringBuffer result;
		int i;
		
		result = new StringBuffer();
		
		for (i = 0; i < pattern.length(); i++)
			if (pattern.charAt(i) == '*')
				result.append(".*");
			else
				result.append(pattern.charAt(i));
		
		return result.toString();
	}
	
	void askQuestion()
	{
		Connection dbconn;
		Statement stmt;
		String query;
		ResultSet rslt;
		String question;
		
		try
		{
			dbconn = DriverManager.getConnection(database_url, "guest", "");
			stmt = dbconn.createStatement();
			query = "SELECT * FROM vragen WHERE _rowid=" + current_question;
			System.out.println(query);
			rslt = stmt.executeQuery(query);
			if (rslt.next())
			{
				question = rslt.getString(1);
				current_answer = rslt.getString(2);
				current_pattern = new RE(fix_pattern(rslt.getString(3)));
				
			}
			else
				throw new Exception("query gave no result: " + query);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		connection.sendPrivMsg(channel, "[Q] " + question);
		
		total_questions++;
	}
	
	void giveAnswer()
	{
		connection.sendPrivMsg(channel, "[A] " + current_answer);
	}
	
	void showStatus()
	{
		StringBuffer status;
		int i;

		status = new StringBuffer();
		status.append("team 1 [");
		for (i = 0; i < trivia_state.getNumPlayers(1); i++)
		{
			status.append(trivia_state.getPlayerNameByIndex(1, i));
			if (i < trivia_state.getNumPlayers(1) - 1)
				status.append(" ");
		}
		status.append("] ");
		status.append(trivia_state.getTotalScore(1));
		status.append(" points");
		connection.sendPrivMsg(channel, status.toString());

		status = new StringBuffer();
		status.append("team 2 [");
		for (i = 0; i < trivia_state.getNumPlayers(2); i++)
		{
			status.append(trivia_state.getPlayerNameByIndex(2, i));
			if (i < trivia_state.getNumPlayers(2) - 1)
				status.append(" ");
		}
		status.append("] ");
		status.append(trivia_state.getTotalScore(2));
		status.append(" points");
		connection.sendPrivMsg(channel, status.toString());
	}
	

	// org.moxy.oak.plugin.ChannelPlugin
	
	public void initChannelPlugin(Oak b, String id, String[] params, String channel, String identifier)
	{
		this.channel = channel;
		this.identifier = identifier;
		
		database_url = b.getProperty(id + ".url", null); //"jdbc:msql://neutrino.cjw.com:1114/Trivia/";
		table = b.getProperty(id + ".table", null);
		answer_prefix = b.getProperty(id + ".prefix", "zz");
		try {answer_time = Integer.parseInt(b.getProperty(id + ".answer_time"));}
		catch (NumberFormatException e) {answer_time = ANSWER_TIME;}
		try {new_question_time = Integer.parseInt(b.getProperty(id + ".question_delay"));}
		catch (NumberFormatException e) {new_question_time = NEW_QUESTION_TIME;}
		try {status_time = Integer.parseInt(b.getProperty(id + ".status_delay"));}
		catch (NumberFormatException e) {status_time = STATUS_TIME;}
		
		trivia_state = new TriviaState();

		random = new Random();
		
	}
	
	public IRCConnection getServer()
	{
		return connection;
	}
	
	public String getChannel()
	{
		return channel;
	}
	
	public String getIdentifier()
	{
		return identifier;
	}
	
	public void destroy()
	{

	}
	
	public void configure()
	{
	}



	// org.moxy.irc.ChannelListener

	public void init(IRCConnection connection)
	{
		this.connection = connection;
	}

	public void initialNickList(Vector nicks)
	{
	}

	public void initialTopic(String topic)
	{
	}

	public void initialOpTopic(boolean mode)
	{
	}

	public void initialNoExtMsg(boolean mode)
	{
	}

	public void initialSecret(boolean mode)
	{
	}

	public void initialInviteOnly(boolean mode)
	{
	}

	public void initialPrivate(boolean mode)
	{
	}

	public void initialModerated(boolean mode)
	{
	}

	public void initialLimit(boolean mode, int limit)
	{
	}

	public void initialKey(boolean modek, String key)
	{
	}

	public void initialBan(Vector /* String */ masks)
	{
	}

	public void setInviteOnly(boolean b, String chanop)
	{
	}

	public void setPrivate(boolean b, String chanop)
	{
	}

	public void setSecret(boolean b, String chanop)
	{
	}

	public void setModerated(boolean b, String chanop)
	{
	}

	public void setNoExtMsg(boolean b, String chanop)
	{
	}

	public void setOpTopic(boolean b, String chanop)
	{
	}

	public void setKey(String key, String chanop)
	{
	}

	public void setLimit(int limit, String chanop)
	{
	}

	public void ban(String mask, boolean mode, String chanop)
	{
	}

	public void setOtherMode(char mode, boolean type, String chanop)
	{
	}

	public void setTopic(String topic, String chanop)
	{
	}

	public void join(String name, String ident, String host)
	{
	}

	public void part(String name, String ident, String host, String msg)
	{
	}

	public void quit(String name, String ident, String host, String msg)
	{
	}

	public void nickChange(String oldName, String newName)
	{
		System.out.println("TriviaPlugin:nickchange; sender=" + oldName + " newNick=" + newName);
		
		trivia_state.changeName(oldName, newName);
	}

	public void kick(String name, String reason, String chanop)
	{
	}

	public void op(String name, boolean mode, String chanop)
	{
	}

	public void voice(String name, boolean mode, String chanop)
	{
	}

	public void handleMessage(String sender, IRCLine message)
	{
		String prefix;
		String nick;
		
		prefix = message.getNextToken();
		System.out.println("TriviaPlugin::handlePRIVMSG prefix=" + prefix);
		
		nick = IRCMessage.getNick(sender);
		if (prefix.startsWith("!"))
			handleCommand(prefix, nick, message);
		else if (state != STATE_ASKING)
			return;
		else if (prefix.equals(answer_prefix))
			handleTriviaAnswer(nick, message);

	}

	public void handleAction(String sender, IRCLine message)
	{
	}
	
}
