package org.moxy.irc;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;

/**
 * A utility class to assist in the tokenizing of the incomming
 * line from an IRC server.
 * @version 1.0
 * @author Marcus Wenzel
 */
/*/
 * Tokenizes an incomming line from an IRC server
 * Copyright (C) 2000 Marcus Wenzel
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * 21-05-2000: cjw	removed old code that was already commented out
 * 16-03-2000: cjw	parsing redone, added integer message type support
 */

public class IRCMessage{

        private String line = null;
		private String raw_line;
        private IRCConnection connection;
		private Vector /* <String> */ params = null;
        private int type = 0;

// regular messages
	static final public int MSG_PASS = 1;
	static final public int MSG_NICK = 2;
	static final public int MSG_USER = 3;
	static final public int MSG_SERVER = 4;
	static final public int MSG_OPER = 5;
	static final public int MSG_QUIT = 6;
	static final public int MSG_SQUIT = 7;
	
	static final public int MSG_JOIN = 8;
	static final public int MSG_PART = 9;
	static final public int MSG_MODE = 10;
	static final public int MSG_TOPIC = 11;
	static final public int MSG_NAMES = 12;
	static final public int MSG_LIST = 13;
	static final public int MSG_INVITE = 14;
	
	static final public int MSG_KICK = 15;
	static final public int MSG_VERSION = 16;
	static final public int MSG_STATS = 17;
	static final public int MSG_LINKS = 18;
	static final public int MSG_TIME = 19;
	static final public int MSG_CONNECT = 20;
	
	static final public int MSG_TRACE = 21;
	static final public int MSG_ADMIN = 22;
	static final public int MSG_INFO = 23;
	static final public int MSG_PRIVMSG = 24;
	static final public int MSG_NOTICE = 25;
	static final public int MSG_WHO = 26;
	
	static final public int MSG_WHOIS = 27;
	static final public int MSG_WHOWAS = 28;
	static final public int MSG_KILL = 29;
	static final public int MSG_PING = 30;
	static final public int MSG_PONG = 31;
	static final public int MSG_ERROR = 32;
	static final public int MSG_AWAY = 33;
	
	static final public int MSG_REHASH = 34;
	static final public int MSG_RESTART = 35;
	static final public int MSG_SUMMON = 36;
	static final public int MSG_USERS = 37;
	static final public int MSG_WALLOPS = 38;
	static final public int MSG_USERHOST = 39;
	
	static final public int MSG_ISON = 40;

// errors
	static final public int ERR_NOSUCHNICK = 41;
	static final public int ERR_NOSUCHSERVER = 42;
	static final public int ERR_NOSUCHCHANNEL = 43;
	static final public int ERR_CANNOTSENDTOCHAN = 44;
	static final public int ERR_TOOMANYCHANNELS = 45;
	static final public int ERR_WASNOSUCHNICK = 46;
	static final public int ERR_TOOMANYTARGETS = 47;
	
	static final public int ERR_NOORIGIN = 48;
	static final public int ERR_NORECIPIENT = 49;
	static final public int ERR_NOTEXTTOSEND = 50;
	static final public int ERR_NOTOPLEVEL = 51;
	static final public int ERR_WILDTOPLEVEL = 52;
	static final public int ERR_UNKNOWNCOMMAND = 53;
	static final public int ERR_NOMOTD = 54;
	static final public int ERR_NOADMININFO = 55;
	static final public int ERR_FILEERROR = 56;
	
	static final public int ERR_NONICKNAMEGIVEN = 57;
	static final public int ERR_ERRONEUSNICKNAME = 58;
	static final public int ERR_NICKNAMEINUSE = 59;
	static final public int ERR_NICKCOLLISION = 60;
	static final public int ERR_USERNOTINCHANNEL = 61;
	static final public int ERR_NOTONCHANNEL = 62;
	static final public int ERR_USERONCHANNEL = 63;
	static final public int ERR_NOLOGIN = 64;
	static final public int ERR_SUMMONDISABLED = 65;
	
	static final public int ERR_USERSDISABLED = 66;
	static final public int ERR_NOTREGISTERED = 67;
	static final public int ERR_NEEDMOREPARAMS = 68;
	static final public int ERR_ALREADYREGISTRED = 69;
	static final public int ERR_NOPERMFORHOST = 70;
	static final public int ERR_PASSWDMISMATCH = 71;
	static final public int ERR_YOUREBANNEDCREEP = 72;
	static final public int ERR_KEYSET = 73;
	static final public int ERR_CHANNELISFULL = 74;
	
	static final public int ERR_UNKNOWNMODE = 75;
	static final public int ERR_INVITEONLYCHAN = 76;
	static final public int ERR_BANNEDFROMCHAN = 77;
	static final public int ERR_BADCHANNELKEY = 78;
	static final public int ERR_NOPRIVILEGES = 79;
	static final public int ERR_CHANOPRIVSNEEDED = 80;
	static final public int ERR_CANTKILLSERVER = 81;
	static final public int ERR_NOOPERHOST = 82;
	static final public int ERR_UMODEUNKNOWNFLAG = 83;
	
	static final public int ERR_USERSDONTMATCH = 84;

// command replies
	static final public int RPL_NONE = 85;
	static final public int RPL_USERHOST = 86;
	static final public int RPL_ISON = 87;
	static final public int RPL_AWAY = 88;
	static final public int RPL_UNAWAY = 89;
	static final public int RPL_NOWAWAY = 90;
	static final public int RPL_WHOISUSER = 91;
	static final public int RPL_WHOISSERVER = 92;
	
	static final public int RPL_WHOISOPERATOR = 93;
	static final public int RPL_WHOISIDLE = 94;
	static final public int RPL_ENDOFWHOIS = 95;
	static final public int RPL_WHOISCHANNELS = 96;
	static final public int RPL_WHOWASUSER = 97;
	static final public int RPL_ENDOFWHOWAS = 98;
	static final public int RPL_LISTSTART = 99;
	static final public int RPL_LIST = 100;
	static final public int RPL_LISTEND = 101;
	
	static final public int RPL_CHANNELMODEIS = 102;
	static final public int RPL_NOTOPIC = 103;
	static final public int RPL_TOPIC = 104;
	static final public int RPL_INVITING = 105;
	static final public int RPL_SUMMONING = 106;
	static final public int RPL_VERSION = 107;
	static final public int RPL_WHOREPLY = 108;
	static final public int RPL_ENDOFWHO = 109;
	static final public int RPL_NAMREPLY = 110;
	
	static final public int RPL_ENDOFNAMES = 111;
	static final public int RPL_LINKS = 112;
	static final public int RPL_ENDOFLINKS = 113;
	static final public int RPL_BANLIST = 114;
	static final public int RPL_ENDOFBANLIST = 115;
	static final public int RPL_INFO = 116;
	static final public int RPL_ENDOFINFO = 117;
	static final public int RPL_MOTDSTART = 118;
	static final public int RPL_MOTD = 119;
	
	static final public int RPL_ENDOFMOTD = 120;
	static final public int RPL_YOUREOPER = 121;
	static final public int RPL_REHASHING = 122;
	static final public int RPL_TIME = 123;
	static final public int RPL_USERSSTART = 124;
	static final public int RPL_USERS = 125;
	static final public int RPL_ENDOFUSERS = 126;
	static final public int RPL_NOUSERS = 127;
	static final public int RPL_TRACELINK = 128;
	
	static final public int RPL_TRACECONNECTING = 129;
	static final public int RPL_TRACEHANDSHAKE = 130;
	static final public int RPL_TRACEUNKNOWN = 131;
	static final public int RPL_TRACEOPERATOR = 132;
	static final public int RPL_TRACEUSER = 133;
	static final public int RPL_TRACESERVER = 134;
	static final public int RPL_TRACENEWTYPE = 135;
	static final public int RPL_TRACELOG = 136;
	static final public int RPL_STATSLINKINFO = 137;
	
	static final public int RPL_STATSCOMMANDS = 138;
	static final public int RPL_STATSCLINE = 139;
	static final public int RPL_STATSNLINE = 140;
	static final public int RPL_STATSILINE = 141;
	static final public int RPL_STATSKLINE = 142;
	static final public int RPL_STATSYLINE = 143;
	static final public int RPL_ENDOFSTATS = 144;
	static final public int RPL_STATSLLINE = 145;
	static final public int RPL_STATSUPTIME = 146;
	
	static final public int RPL_STATSOLINE = 147;
	static final public int RPL_STATSHLINE = 148;
	static final public int RPL_UMODEIS = 149;
	static final public int RPL_LUSERCLIENT = 150;
	static final public int RPL_LUSEROP = 151;
	static final public int RPL_LUSERUNKNOWN = 152; 
	static final public int RPL_LUSERCHANNELS = 153;
	static final public int RPL_LUSERME = 154;
	static final public int RPL_ADMINME = 155;
	
	static final public int RPL_ADMINLOC1 = 156;
	static final public int RPL_ADMINLOC2 = 157;
	static final public int RPL_ADMINEMAIL = 158;
	
// reserved numeric msgs
	static final public int RPL_TRACECLASS = 159;
	static final public int RPL_STATSQLINE = 160;
	static final public int RPL_SERVICEINFO = 161;
	static final public int RPL_ENDOFSERVICES = 162;
	static final public int RPL_SERVICE = 163;
	static final public int RPL_SERVLIST = 164;
	
	static final public int RPL_SERVLISTEND = 165;
	static final public int RPL_WHOISCHANOP = 166;
	static final public int RPL_KILLDONE = 167;
	static final public int RPL_CLOSING = 168;
	static final public int RPL_CLOSEEND = 169;
	static final public int RPL_INFOSTART = 170;
	static final public int RPL_MYPORTIS = 171;
	static final public int ERR_YOUWILLBEBANNED = 172;
	static final public int ERR_BADCHANMASK = 173;
	
	static final public int ERR_NOSERVICEHOST = 174;

// weird msgs
	static final public int WRD_001 = 175;
	static final public int WRD_002 = 176;
	static final public int WRD_003 = 177;
	static final public int WRD_004 = 178;
	static final public int WRD_005 = 179;
	static final public int WRD_265 = 180;
	static final public int WRD_266 = 181;
	static final public int WRD_328 = 182;
	
	static final public int WRD_329 = 183;
	static final public int WRD_333 = 184;
	
	static final public int MSG_CTCP = 1001;
	static final public int MSG_CTCPREPLY = 1002;

	static final private String[] typeStrings = 
		{
			"PASS", "NICK", "USER", "SERVER", "OPER", "QUIT", "SQUIT",     //  7
			"JOIN", "PART", "MODE", "TOPIC", "NAMES", "LIST", "INVITE",    // 14
			"KICK", "VERSION", "STATS", "LINKS", "TIME", "CONNECT",        // 20
			"TRACE", "ADMIN", "INFO", "PRIVMSG", "NOTICE", "WHO",          // 26
			"WHOIS", "WHOWAS", "KILL", "PING", "PONG", "ERROR", "AWAY",    // 33
			"REHASH", "RESTART", "SUMMON", "USERS", "WALLOPS", "USERHOST", // 39
			"ISON", "401", "402", "403", "404", "405", "406", "407",       // 47
			"409", "411", "412", "413", "414", "421", "422", "423", "424", // 56
			"431", "432", "433", "436", "441", "442", "443", "444", "445", // 65
			"446", "451", "461", "462", "463", "464", "465", "467", "471", // 74
			"472", "473", "474", "475", "481", "482", "483", "491", "501", // 83
			"502", "300", "302", "303", "301", "305", "306", "311", "312", // 92
			"313", "317", "318", "319", "314", "396", "321", "322", "323", // 101
			"324", "331", "332", "341", "342", "351", "352", "315", "353", // 110
			"366", "364", "365", "367", "368", "371", "374", "375", "372", // 119
			"376", "381", "382", "391", "392", "393", "394", "295", "200", // 128
			"201", "202", "203", "204", "205", "206", "208", "261", "211", // 137
			"212", "213", "214", "215", "216", "218", "219", "241", "242", // 146
			"243", "244", "221", "251", "252", "253", "254", "255", "256", // 155
			"257", "258", "259", "209", "217", "231", "232", "233", "234", // 164
			"235", "316", "361", "362", "363", "373", "384", "466", "476", // 173
			"492", "001", "002", "003", "004", "005", "265", "266", "328", // 182
			"329", "333"
		};
	
	static final private String[] ctcpStrings = 
		{
			"CTCP", "CTCPREPLY"
		};

	static final private Hashtable typeHasher;
	
	static
		{
			int i;
			
			typeHasher = new Hashtable();
			for (i = 0; i < typeStrings.length; i++)
				typeHasher.put(typeStrings[i], new Integer(i + 1));
		}

    /**
     * Constructs a "fake" IRCMessage using a null IRCConnection
     * @since 1.0
     **/
    public void IRCMessage(String nickMask, String type, String params){
     String line = ":"+nickMask+" PRIVMSG :"+type.toUpperCase()+" "+params+"";
        this.raw_line = line;
        this.connection = null;
        parseMessage(line);
    }


	/**
	 * Creates a new IRC message with the specified line.
	 * @since 1.0
	 * @param line the line from the IRC server.
	 * @param connection the connection to the originating IRC server.
	 **/
		IRCMessage(String line, IRCConnection connection)
	{
		this.raw_line = line;
		this.connection = connection;
		params = new Vector();
		parseMessage(line);
	}
	
	// parses an RFC1459 compliant server->client message
	// sender and command are put in local variables
	// remaining part is checked on CTCP signifier
	
	private void parseMessage(String line)
	{
		IRCLine st;
		String token;
		String type;
		String sender, target;
		
		st = new IRCLine(line);
		token = st.getNextToken();
		if (token.startsWith(":"))
		{
			// this signifies a sender, either a user or a server
			sender = token.substring(1);
			type = st.getNextToken();
		}
		else
		{
			// no sender specified
			sender = connection.getServer();
			type = token;
		}
		
		params.addElement(sender);
		
//		System.out.println("IRCMessage::parseMessage: type=" + type);
		this.type = ((Integer)typeHasher.get(type)).intValue();
				
		token = st.getRemaining();
		
		while (st.hasMoreTokens() && !token.startsWith(":"))
		{
			params.addElement(st.getNextToken());
			token = st.getRemaining();
		}
		
		if (token.equals(""))
		{
			this.line = token;
			return;
		}
		
		// check on CTCP
		if ((this.type == MSG_PRIVMSG) && (token.charAt(1) == 0x01))
		{
			this.line = token.substring(2, token.length() - 1);
			this.type = MSG_CTCP;
		}
		else
			if (token.startsWith(":"))
				this.line = token.substring(1);
			else
				System.out.println("UNEXPECTED ERROR: IRCMessage::parseMessage expected a : but found " + token);
		
		switch(this.type)
		{
			case MSG_JOIN:
				if (params.size() < 2)
				{
					params.addElement(this.line);
					this.line = "";
				}
			default: // do nothing 
		}
	}
        /**
         * Get the number of parameters in the message.
         * @since 1.0
         * @return the number of parameters in this message.
         */
		public int getParamCount()
		{
			return params.size();
		}

		/**
		 * Get a param.
		 * @return the param at place index
		 */		
		public String getParam(int index)
		{
			return (String)params.elementAt(index);
		}

        /**
		 * @deprecated use getMsgType instead
         * Returns the type of message this class repersents.
         * @since 1.0
         * @return String repersenting the type of message.
         */
        public String getType()
		{
			if (type < 1000)
				return typeStrings[type - 1];
			else
				return ctcpStrings[type - 1001];
		}
		
		public int getMsgType()
		{
			return type;
		}

        /**
         * Returns true if the sender of this message was NOT a server.
         * @since 1.0
         * @return True if this message orignated from another IRC user.
         */
         public boolean isFromIRCUser(){
          return getSender().indexOf("!") > -1;
         }

        /**
         * Returns true if the sender of this message was a server.
         * @since 1.0
         * @return True if the message orignated from an IRC server.
         */
         public boolean isServerMessage(){
          return getSender().indexOf("!") <= -1;
         }
		 
		/**
		 * Checks if this message does not belong to a channel or a private conversation.
		 */
		public boolean isConsoleMessage()
		{
			String target;
		
			if (!isServerMessage())
				return false;
//			System.out.println("IRCMessage::isConsoleMessage: target = " + getTarget());
			switch(getMsgType())
			{
				case RPL_BANLIST:
				case RPL_ENDOFBANLIST:
				case ERR_USERONCHANNEL:
				case ERR_KEYSET:
				case ERR_CHANOPRIVSNEEDED:
				case RPL_LIST:
				case RPL_CHANNELMODEIS:
				case RPL_NOTOPIC:
				case RPL_TOPIC:
				case RPL_INVITING:
				case RPL_WHOREPLY:
				case RPL_ENDOFWHO:
				case RPL_NAMREPLY:
				case RPL_ENDOFNAMES:
				case WRD_328:
				case WRD_329:
				case WRD_333:
					return false;
				default:
					return true;
			}
		}

         /**
          * Returns the sender of the message.
          * @since 1.0
          * @return The sender of the message.
          */
         public String getSender()
		 {
		 	return getParam(0);
		}

		public String getTarget()
		{
			if (params.size() < 2)
				return null;
			return getParam(1);
		}
		
        /**
		 * @deprecated
         * Returns the entire line.
         * @since 1.0
         * @return The entire line.
         */
        public String getRawLine(){
         return raw_line;
        }

        /**
         * Returns the IRCConnection that the line orignated from.
         * @since 1.0
         * @return the orignating IRC connection.
         */
        public IRCConnection getConnection(){
         return connection;
        }

       /**
         * Returns the IRCLine held by this class.
         * @since 1.0
         * @return The enclosed IRCLine.
         */
        public IRCLine getIRCLine()
		{
			return new IRCLine(line);
		}

       /**
        * Returns the nick portion from a nick mask nick!user@host.
        * @since 1.0
        * @param lnick The "long nick" of the user nick!user@host.
        * @return The nick section of the mask or null if it doesn't exist.
        */
       public static String getNick(String lnick){
        if(lnick.indexOf("!")>-1){
         return lnick.substring(0,lnick.indexOf("!"));
        }
        return lnick;
       }
       /**
        * Returns the user portion from a nick mask nick!user@host.
        * @since 1.0
        * @param lnick The "long nick" of the user nick!user@host.
        * @return The user section of the mask or null if it doesn't exist.
        */
       public static String getUser(String lnick){
        if(lnick.indexOf("!") > -1){
         lnick = lnick.substring(lnick.indexOf("!")+1);
        }
        if(lnick.indexOf("@") > -1){
         return lnick.substring(0,lnick.indexOf("@"));
        }
        return null;
       }
       /**
        * Returns the host portion of a nick mask nick!user@host.
        * @since 1.0
        * @param lnick The "long nick" of the user nick!user@host.
        * @return The host section of the mask or null if it doesn't exist.
        */
       public static String getHost(String lnick){
        if(lnick.indexOf("@")>-1){
         return lnick.substring(lnick.indexOf("@")+1);
        }
        return null;
       }
}
