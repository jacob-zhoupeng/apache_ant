/*
 * $Id: Irc.java,v 1.2 2001/03/26 22:40:42 matti Exp $
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

/**
 * collection of static constants, messages and such things 
 * related to the IRC protocol.
 *
 * @author Matti Dahlbom 
 * @version $Name:  $ $Revision: 1.2 $
 */
public final class Irc 
{
    /* error replies */
    public static final String ERR_NICKNAMEINUSE    = "433";
    public static final String ERR_NONICKNAMEGIVEN  = "431";
    public static final String ERR_ERRONEUSNICKNAME = "432";
    //##TODO## add : nick temporarily unavailable

    /* command responses */
    public static final String RPL_WELCOME          = "001";

    public static final String RPL_ENDOFMOTD        = "376";
    public static final String RPL_MOTD             = "372";
    public static final String RPL_MOTDSTART        = "375";

    public static final String RPL_BANLIST          = "367";
    public static final String RPL_ENDOFBANLIST     = "368";

    public static final String RPL_NAMREPLY         = "353";
    public static final String RPL_ENDOFNAMES       = "366";
    public static final String RPL_WHOISUSER        = "311";

    public static final String RPL_WHOREPLY         = "352";
    public static final String RPL_ENDOFWHO         = "315";

    public static final String RPL_USERHOST         = "302";
    public static final String RPL_TOPIC            = "332";

    public static final String RPL_CHANNELMODEIS    = "324";
    /**
     * op status granted (+o)
     */
    public static final int MODE_OP            = 1;
    /**
     * op status removed (-o)
     */
    public static final int MODE_DEOP          = 2;
    /**
     * voice granted (+v)
     */
    public static final int MODE_VOICE         = 3;
    /**
     * voice removed (-v)
     */
    public static final int MODE_DEVOICE       = 4;
    /**
     * ban set (+b)
     */
    public static final int MODE_BAN           = 5;
    /**
     * ban removed (-b)
     */
    public static final int MODE_UNBAN         = 6;
    /**
     * channel key set (+k)
     */
    public static final int MODE_KEY_SET       = 7;
    /**
     * channel key removed (-k)
     */
    public static final int MODE_KEY_REMOVED   = 8;
    /**
     * need-invite exception set (+I)
     */
    public static final int MODE_INVEX_SET     = 9;
    /**
     * need-invite exception removed (-I)
     */
    public static final int MODE_INVEX_REMOVED = 10;
    /**
     * ban exception set (+e)
     */
    public static final int MODE_BANEX_SET     = 11;
    /**
     * ban exception removed (-e)
     */
    public static final int MODE_BANEX_REMOVED = 12;
    /**
     * channel limit set (+l)
     */
    public static final int MODE_LIMIT_SET     = 13;
    /**
     * channel limit removed (-l) <b>NOTE:</b> has not target.
     */
    public static final int MODE_LIMIT_REMOVED = 14;
}
