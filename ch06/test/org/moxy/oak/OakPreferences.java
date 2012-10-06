package org.moxy.oak;

import org.moxy.util.config.*;

public class OakPreferences
{
	private static String[] booleanChoice = {"true", "false"};
	private static String[] onAndOff = {"on", "off"};
	
	public static GroupItem[] getConfigurables(int page)
	{
		GroupItem[] result;
		
		switch(page)
		{
			case 1:
				result = new GroupItem[1];
				result[0] = new LinkGroupItem("oak", "Oak prefs", 
											  "Oak preferences", 
											  "org.moxy.oak.OakPreferences", 
											  2);
//				result[1] = new LinkGroupItem("plugs", "Plugin prefs", 
//											  "Plugin preferences", 
//											  "org.moxy.oak.OakPreferences", 
//											  4);
				break;
			case 2:
				result = new GroupItem[6];
				result[0] = new SimpleGroupItem("securitymanager", 
												"Oak security manager", 
												"Oak security manager", 
												"string" );
				result[1] = new LinkGroupItem("smprefs", "Configure security manager", 
											  "Configure the Oak security manager", 
											  "@securitymanager", 
											  1);
				result[2] = new SimpleGroupItem("nick", 
												"bot nick", 
												"The bot's nick on IRC", 
												"string" );
				result[3] = new SimpleGroupItem("fullname", 
												"bot full name", 
												"The bot's full name on IRC", 
												"string" );
				result[4] = new LinkGroupItem("identd", "Configure ident server", 
											  "Configure the Oak ident server", 
											  "org.moxy.oak.OakPreferences", 
											  5);
				result[5] = new ListGroupItem("servers", 
											  "IRC servers", 
											  "IRC servers to connect to on startup", 
											  "string", 
											  "org.moxy.oak.OakPreferences", 
											  3);
				break;
			case 3:
				result = new GroupItem[2];
				result[0] = new ListGroupItem("channels", "Channels", 
											  "Channels to join on startup", 
											  "string", 
											  "org.moxy.oak.OakPreferences", 4 );
				result[1] = new ListGroupItem("plugins", "Server plugins", 
											  "Server plugins to load on startup", 
											  "string", 
											  "@", 1);
				break;
			case 4:
				result = new GroupItem[1];
				result[0] = new ListGroupItem("plugins", "Channel plugins", 
											"plugins to load for this channel", 
											"string", 
											"@", 1);
				break;
			case 5:
				result = new GroupItem[3];
				result[0] = new ChoiceGroupItem("on", 
												"Ident server setting", 
												"Ident server, true=on, false=off", 
												onAndOff, booleanChoice );
				result[1] = new SimpleGroupItem("port", 
												"Identd port (standard=113)", 
												"Identd port (standard=113)", 
												"int[0,65535]" );
				result[2] = new SimpleGroupItem("user", 
												"ident user reply", 
												"the username the oak identd should return", 
												"regexp/[a-zA-Z0-9]{1,8}/" );
				break;
			default:
				result = null;
		}
		
		return result;
	}
}
