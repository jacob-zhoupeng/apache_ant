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
package irssibot.core;

import irssibot.user.*;
/**
 * Represents a nick actually sitting on an irc chennel.
 *
 * @author Matti Dahlbom
 */
public class Nick 
{
    private Host host = null;
    private boolean op = false;
    private boolean voice = false;

    public Host getHost() { return host; }
    public boolean isOp() { return op; }
    public boolean isVoice() { return voice; }

    public void setOp(boolean state) { op = state; }
    public void setVoice(boolean state) { voice = state; }

    public void setNick(String newNick) 
    {
	host.setNick(newNick);
    }
    
    public Nick(Host host,boolean op,boolean voice)
    {
	this.host = host;
	this.op = op;
	this.voice = voice;
    }
}
    
