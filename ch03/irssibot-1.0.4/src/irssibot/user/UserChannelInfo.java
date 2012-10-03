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
package irssibot.user;

/**
 * represents a channel info for a User
 *
 * @author Matti Dahlbom
 */
public class UserChannelInfo
{
    private String channelName = null;
    
    /* channel flags for user */
    private boolean op = false;
    private boolean voice = false;
    private boolean chanAdmin = false;

    public boolean isOp() { return op; }
    public boolean isVoice() { return voice; }
    public boolean isChanAdmin() { return chanAdmin; }
    public String getChannelName() { return channelName; }

    public UserChannelInfo(String channelName,String flags)
    {
	this.channelName = channelName;
	processChannelFlagsString(flags);
    }

    /**
     * go through channel flags string and turn on indicated flags
     * @param flags flags string
     */
    public void processChannelFlagsString(String flags)
    {
	op = false;
	voice = false;
	chanAdmin = false;

	if( flags != null ) {
	    for( int i = 0; i < flags.length(); i++ ) {
		switch( flags.charAt(i) ) {
		case 'o' : /* channel operator flag */
		    op = true;
		    break;
		case 'v' : /* voice flag */
		    voice = true;
		    break;
		case 'A' : /* channel admin flag */
		    chanAdmin = true;
		    break;
		}
	    }
	} else {
	    putlog("processChannelFlagsString(): null flags");
	}
    }

    public String getChannelFlags()
    {
	String ret = "";

	if( op ) ret += "o"; 
	if( voice ) ret += "v"; 
	if( chanAdmin ) ret += "A"; 

	return ret;
    }

    /** 
     * write a string to log stream
     *
     * @param logMsg string to write to log
     */
    private void putlog(String logMsg) 
    {
        System.out.println(getClass().getName()+": "+logMsg);
    }
}
 
