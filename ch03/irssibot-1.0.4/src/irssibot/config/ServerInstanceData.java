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
package irssibot.config;

import java.util.Vector;
import java.util.Hashtable;

/**
 * configuration data for a server instance (<server-instance>) 
 * in the config file.
 *
 * @author Matti Dahlbom
 */

public class ServerInstanceData 
{
    private String network = null;
    private String userFilePath = null;
    private String botNick = null;
    private String botAltNick = null;
    private String realName = null;
    private Vector serverList = null;
    private Hashtable channels = null;

    public String getNetwork() { return network; }
    public String getUserFilePath() { return userFilePath; }
    public String getBotNick() { return botNick; }
    public String getBotAltNick() { return botAltNick; }
    public String getRealName() { return realName; }
    public Vector getServerList() { return serverList; }
    public Hashtable getChannels() { return channels; }
 
    public ServerInstanceData(String network,String userFilePath,String botNick,String botAltNick,String realName,
			      Vector serverList,Hashtable channels)
    {
	this.network = network;
	this.userFilePath = userFilePath;
	this.botNick = botNick;
	this.botAltNick = botAltNick;
	this.realName = realName;
	this.serverList = serverList;
	this.channels = channels;
    }

    public void setBotNick(String nick) {
	botNick = nick;
    }

    public void setBotAltNick(String nick) {
	botAltNick = nick;
    }
}


