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

class TriviaState
{
	Vector[] /* String */ teams;
	Vector[] /* Integer */ scores;
	int[] totals;
	int quota = 0;
	String[] teamNames;

		TriviaState()
	{
		teams = new Vector[2];
		scores = new Vector[2];
		totals = new int[2];
		teamNames = new String[2];
	}
	
	private int getTeamNr(String name)
	{
		int i;
		int j;
		
		for (j = 0; j < 2; j++)
		{
			for (i = 0; i < teams[j].size(); i++)
			{
				if (((String)teams[j].elementAt(i)).equalsIgnoreCase(name))
					return j + 1;
			}
		}
		
		return 0;
	}

	private void incrementScore(String name, int amount)
	{
		int i;
		int j;
		
		for (j = 0; j < 2; j++)
		{
			for (i = 0; i < teams[j].size(); i++)
			{
				if (((String)teams[j].elementAt(i)).equalsIgnoreCase(name))
				{
					scores[j].setElementAt(new Integer(((Integer)scores[j].elementAt(i)).intValue() + amount), i);
					return;
				}
			}
		}
	}

	public void reset(int quota)
	{
		teams[0] = new Vector();
		teams[1] = new Vector();
		scores[0] = new Vector();
		scores[1] = new Vector();
		totals[0] = 0;
		totals[1] = 0;
		teamNames[0] = "Team 1";
		teamNames[1] = "Team 2";
		this.quota = quota;
	}

	public void addPlayer(int team, String name)
	{
		if (getTeamNr(name) != 0)
			return; // for now, changing teams is not allowed
		
		teams[team - 1].addElement(name);
		scores[team - 1].addElement(new Integer(0));
	}
	
	public void setTeam(String player, String team_name)
	{
		int teamNum;
		
		teamNum = getTeamNr(player);
		if (teamNum == 0)
			return;
		
		teamNames[teamNum - 1] = team_name;
	}
	
	public boolean nameRegistered(String name)
	{
		int team;
	
		team = getTeamNr(name);
		
		if (team != 1 && team != 2)
			return false;
		else
			return true;
	}

	public void addPoint(String name)
	{
		int team;
	
		team = getTeamNr(name);
		if (team == 0)
			return;
	
		incrementScore(name, 1);
		totals[team - 1]++;
	}
	
	public boolean quotaReached()
	{
		int j = -1;
		
		if (totals[0] >= quota)
			j = 0;
		if (totals[1] >= quota)
			j = 1;
		
		if (j < 0)
			return false;
		else
			return true;
	}
	
	public String getWinningTeamName()
	{
		if (!quotaReached())
			return null;
		
		if (totals[0] >= quota)
			return teamNames[0];
		else
			return teamNames[1];
	}
	
	public int getTotalScore(int team)
	{
		if (team < 1 || team > 2)
			return 0;
		
		return totals[team - 1];
	}
	
	public int getNumPlayers(int team)
	{
		if (team < 1 || team > 2)
			return 0;
		
		return teams[team - 1].size();
	}

/*	
	public int getPlayerScore(String name)
	{
		int team;
		
		team = getTeamNr(name);
		if (team < 1 || team > 2)
			return 0;
		
		return ((Integer)scores[team - 1].elementAt(i)).intValue();
	}
*/
	
	public String getPlayerNameByIndex(int team, int index)
	{
		return teams[team - 1].elementAt(index).toString();
	}
	
	public int getPlayerScoreByIndex(int team, int index)
	{
		return ((Integer)scores[team - 1].elementAt(index)).intValue();
	}
	
	public void changeName(String oldName, String newName)
	{
		int team, i;
		
		for (team = 0; team < 2; team++)
			for (i = 0; i < teams[team].size(); i++)
				if (teams[team].elementAt(i).equals(oldName))
				{
					teams[team].setElementAt(newName, i);
					return;
				}
	}
}
