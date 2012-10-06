package org.moxy.oak.security;
/*/
 * PropertiesSecurityManager.java
 * Oak, moxy's IRC bot
 *
 * Copyright © 2000  Marcus Wenzel
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
 * written by Marcus Wenzel (mw)
 * moxy@moxy.org
 *
 *
/*/

/**
 * An interface for bot security managers. These check privileges for nicks
 * when supplied with the right information.
 * @version 1.0
 * @author moxy
 */

import org.moxy.oak.*;
import org.moxy.oak.irc.*;
import org.moxy.irc.*;
import org.moxy.security.*;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;

public class PropertiesSecurityManager implements BotSecurityManager {

    private Vector users = new Vector();
    private Vector directUsers = new Vector();
    private Properties userProps = new Properties();
    private Properties networks = new Properties();

    /**
     *@since 1.0
     */
    public PropertiesSecurityManager() {
        File f;
        try {//this crap's not used yet.
            String s;
            s = System.getProperty("user.home");
            if (!s.endsWith(System.getProperty("file.separator"))) {
                s = s + System.getProperty("file.separator");
            }
            s = s + ".OakTJB";
            f = new File(s);
            if (!f.exists()) {
                f.mkdirs();
            }
            f = new File(s, "users.props");
            if (!f.exists()) { /* start external config program */
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Couldn't find users.props file create one now?[n/y]");
                String answer = in.readLine();
                if (answer != null && answer.toLowerCase().startsWith("y")) {
                    System.out.println("Creating master user.");
                    System.out.print("Master user login name:");
                    answer = in.readLine();
                    System.out.print("Master user password:");
                    userProps.put(answer, Crypt.crypt(in.readLine()));
                    System.out.print("Enter the nickmask for the master user ex. *!*@*.host.com:");
                    userProps.put(answer + ".nickmask",in.readLine());
                    userProps.put(answer + ".owner","true");
                    userProps.put(answer + ".all.all.op","true");
                    userProps.put(answer + ".all.all.deop","true");
                    userProps.put(answer + ".all.all.devoice","true");
                    userProps.put(answer + ".all.all.voice","true");
                    userProps.put(answer + ".all.all.kick","true");
                    userProps.put(answer + ".all.all.ban","true");
                    userProps.put(answer + ".all.all.exit","true");
                    userProps.put(answer + ".all.all.say","true");
                    userProps.put(answer + ".all.all.do","true");
                    userProps.put(answer + ".all.all.privdo","true");
                    userProps.put(answer + ".all.all.privsay","true");
                    userProps.put(answer + ".all.all.connect","true");
                    userProps.put(answer + ".all.all.disconnect","true");
                    userProps.put(answer + ".all.all.serverlist","true");
                    userProps.put(answer + ".all.all.join","true");
                    userProps.put(answer + ".all.all.part","true");
                    userProps.put(answer + ".all.all.nick","true");
                    userProps.put(answer + ".all.all.serverplugin","true");
                    userProps.put(answer + ".all.all.channelplugin","true");
                    userProps.put(answer + ".all.all.pluginlist","true");
                    userProps.put(answer + ".all.all.deluser","true");
                    userProps.put(answer + ".all.all.adduser","true");
                    userProps.put(answer + ".all.all.setpriv","true");
		    userProps.put(answer + ".all.all.dcc","true");
		    userProps.put(answer + ".all.all.dcc.configure","true");
                    userProps.store(new FileOutputStream(f), "PropertiesSecurityManager user definitions");
                    in = null;
                    answer = null;
                } else {
                    System.out.println("No master user set. All login's will fail.");
                }
            } else
                userProps.load(new FileInputStream(f));
            f = new File(s, "networks.props");
            if (!f.exists()) {
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Couldn't find the networks.props file create one now?[n/y]");
                String answer = in.readLine();
                if (answer != null && answer.toLowerCase().startsWith("y")) {
                    System.out.println("Adding a few dalnet servers to the list.");
                    //should be able to be loaded via an external file.
                    //then again isn't that what we just tried to do?
                    //better yet download it since we're connected to the net
                    //www.moxy.org/oak/updates/networks.props ;o)
                    networks.put("koala.nsw.au.dal.net","dalnet");
                    networks.put("raptor.dal.net","dalnet");
                    networks.put("farside.ab.ca.dal.net","dalnet");
                    networks.put("powertech.no.eu.dal.net","dalnet");
                    networks.put("viking.dal.net","dalnet");
                    networks.put("ced.dal.net","dalnet");
                    networks.put("sodre.dal.net","dalnet");
                    networks.put("hebron.dal.net","dalnet");
                    networks.put("qis.md.us.dal.net","dalnet");
                    networks.put("webbernet.dal.net","dalnet");
                    networks.put("stlouis.dal.net","dalnet");
                    networks.put("liberty.dal.net","dalnet");
                    networks.put("glass.oh.us.dal.net","dalnet");
                    networks.put("webzone.dal.net","dalnet");
                    networks.put("lineone.dal.net","dalnet");
                    networks.put("splitrock.tx.us.dal.net","dalnet");
                    networks.put("viper.mo.us.dal.net","dalnet");
                    while (true) {
                        System.out.print("Add another server to the list?[n/y]");
                        answer = in.readLine();
                        if (answer == null || !answer.toLowerCase().startsWith("y"))
                            break;
                        System.out.print("Enter server hostname:");
                        answer = in.readLine();
                        System.out.println("Enter network name:");
                        networks.put(answer.toLowerCase(),
                                in.readLine().toLowerCase());
                    }
                    networks.store(new FileOutputStream(f), "PropertiesSecurityManager network definitions");
                } else {
                    System.out.println("Caution: no netowks are defined access errors may occur.");
                }
            } else
                networks.load(new FileInputStream(f));
        } catch (Exception e) {
            System.err.println("{PropertiesSecurityManager.PropertiesSecurityManager()} "+
                    "an error occured while reading the user and network properties files.");
            e.printStackTrace();
        }
    }

    private void saveUserProps() {
        File f;
        try {
            String s;
            s = System.getProperty("user.home");
            if (!s.endsWith(System.getProperty("file.separator"))) {
                s = s + System.getProperty("file.separator");
            }
            s = s + ".OakTJB";
            f = new File(s);
            if (!f.exists()) {
                f.mkdirs();
            }
            f = new File(s, "users.props");
            userProps.store(new FileOutputStream(f), "PropertiesSecurityManager user definitions");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Error saving user definitions.");
        }
    }

    private void saveNetworks() {
        File f;
        try {
            String s;
            s = System.getProperty("user.home");
            if (!s.endsWith(System.getProperty("file.separator"))) {
                s = s + System.getProperty("file.separator");
            }
            s = s + ".OakTJB";
            f = new File(s);
            if (!f.exists()) {
                f.mkdirs();
            }
            f = new File(s, "networks.props");
            networks.store(new FileOutputStream(f), "PropertiesSecurityManager network definitions");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Error saving network definitions.");
        }
    }




    /**
       * Figured someone might like this method even if this class isn't
       * used as the security manager. This method take a nick mask (ban mask)
       * and a full nick. ex. *!*@*.level3.net and moxy!mosers@houston.level3.net
       * This method compares them for equality. *'s are considered wild cards
       * of 0 or more characters. In the above example the mask will take any
       * string of characters up to the first ! followed by any string of
       * characters up to the first @ then followed by any string ending
       * in .level3.net. Now this is more flexiable than just checking
       * nick masks. This can be used for checking anything with * as a
       * wild card counting as 0 or more characters. You could use it to check
       * say a file name against some sort of patern.
       * (image*.ext with image392.ext). I'll eventually be adding one where
       * ? can be used as a single character wild card and one where
       * the single character and multicharacter wild cards can be specified.
       * @return If the nick matches the mask then true will be
       * returned else false will be returned.
       */
    public static boolean isMatch(String mask, String nick) {
        mask = mask.trim();
        if (mask.equalsIgnoreCase("*")) {
            return true;
        }
        if (mask.startsWith("*")) {
            int end = -1;
            if (mask.indexOf("*",mask.indexOf("*") + 1) == -1) {
                end = mask.length();
            } else {
                end = mask.indexOf("*",mask.indexOf("*") + 1);
            }
            if (end == -1) {
                return false;
            }
            String tmask = mask.substring(1, end).trim();
            if (nick.indexOf(tmask) == -1) {
                return false;
            } else {
                mask = mask.substring(end).trim();
                nick = nick.substring(nick.indexOf(tmask)).trim();
                return isMatch(mask, nick);
            }
        }
        int end = -1;
        if (mask.indexOf("*") == -1) {
            end = mask.length();
        } else {
            end = mask.indexOf("*");
        }
        if (end == -1) {
            return false;
        }
        String tmask = mask.substring(0, end).trim();
        if (nick.startsWith(tmask)) {
            if (end == mask.length()) {
                return true;
            }
            mask = mask.substring(end).trim();
            nick = nick.substring(tmask.length()).trim();
            return isMatch(mask, nick);
        } else {
            return false;
        }
    }




    /**
     * Called just after the default constructor to finish initilizing the bot.
     * If the security manager needs additional information to load from Oak
     * such as a database url it should use Oak's getProperty() method.
     * @since 1.0
     * @param bot The Oak instance that owns the manager.
     */
    public void init(Oak bot){}//not needed by this plugin.

    /**
     * Checks access for a nick to a section of the bot for a possibly
     * non existent IRCConnection. This is used by the SM commands to check
     * a for a users access rights to a possibly disconnected network. Some of
     * the SM commands allow the user to be on one network and adminster another.
     *
     * @sinec 1.0
     * @param privilege the permission requested.
     * @param channel the channel of the permission or null if it doesn't pertain to a specific channel.
     * @param nick the nick of the user.
     * @param connection the  connection the user is currently on.
     * @param network the network the user is tring to administer.
     **/
    public boolean hasAccess(String nick, CommandConnection connection, String network, String channel, String privilege){
        if(nick == null || connection == null || privilege == null)return false; //bogus info
        String user = getUserNameFromNick(connection, nick);
        if (user == null) return false; //user not logged in
	if(channel == null){
	 String result;
	 if(network == null){
	  result = userProps.getProperty(user+"."+network+".all."+privilege);
	  if(result != null && result.equalsIgnoreCase("true"))return true;
	 }else{
   	  result = userProps.getProperty(user+".all.all."+privilege);//changing user.all.priv to user.all.all.priv to specify a global priv
	  if(result != null && result.equalsIgnoreCase("true"))return true;
	 }
	 return false;
        }
        String result;
	if(network == null){
         result = userProps.getProperty(user+".all."+channel+"."+privilege);
	 if(result != null && result.equalsIgnoreCase("true")) return true;
	}else{
	 result = userProps.getProperty(user + "."+network + "."+channel + "."+ privilege);
         if (result != null && result.equalsIgnoreCase("true")) return true;
	}
        return false;
    }


    /**
    * Looks up a global permission for a nick regarding some connection.
    * @since 1.0
    * @param privilage the privilage to check access to.
    * @param nick the full nick of the user.
    * @param hostConnection the connection the user is on.
    * @param targetConnection the connection the action will be executed on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String privilege) {
     if (nick == null || hostConnection == null || targetConnection == null || privilege == null) return false;
 	//bogus data
     String user = getUserNameFromNick(hostConnection, nick);
     if (user == null)return false; //user not logged in.
     String network = networks.getProperty(targetConnection.getServer());
     if (network != null) {
      String result = userProps.getProperty(user + "."+network + ".all."+privilege);
      if (result != null) {
       return result.equalsIgnoreCase("true");
      }//else check network wide status
     }
     String result = userProps.getProperty(user + ".all.all."+privilege);
     if (result == null) {//access not set.
      return false;
     }
     if (result.equalsIgnoreCase("true"))
      return true;
	 return false;//return false if we dunno what it is.
    }


    /**
    * Should look up those securitys that don't have to do with a
    * IRCConnection such as the EXIT command. That command doesn't
    * have a specific target IRCConnection;
    * Looks up a global permission for a nick
    * @since 1.0
    * @param privilege the privilage to check access to.
    * @param nick the fullnick of the user.
    * @param connection the connection the user is on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection connection, String privilege){
     if(nick == null || connection == null ||  privilege == null)return false; //bogus info
     String user = getUserNameFromNick(connection, nick);
     if (user == null) return false; //user not logged in
     String result = userProps.getProperty(user+".all.all."+privilege);
     if(result != null && result.equalsIgnoreCase("true")) return true;
     return false;         
    }


    /**
    * Looks up a channel specific permission for a nick regarding some connection.
    * @since 1.0
    * @param privilage the privilage to check access to.
    * @param channel the channel the privilege is for or null if doesn't pertain to a channel.
    * @param nick the full nick of the user.
    * @param hostConnection the connection the user is on.
    * @param targetConnection the connection the action will be executed on.
    * @return true if the access is granted false otherwise.
    */
    public boolean hasAccess(String nick, CommandConnection hostConnection, IRCConnection targetConnection, String channel, String privilege) {
        if (nick == null || hostConnection == null || privilege == null || targetConnection == null) return false;
		//bogus data

        String user = getUserNameFromNick(hostConnection, nick);
        if (user == null) return false; //user not logged in
	if(channel == null){
	     return hasAccess(nick, hostConnection, targetConnection, privilege);
        }
        String network = networks.getProperty(targetConnection.getServer());
        if (network != null) {                   //user.network.channel.privilege
            String result = userProps.getProperty(user + "."+network + "."+channel + "."+ privilege);
            if (result != null && result.equalsIgnoreCase("true"))
                return true;
        }
        String result = userProps.getProperty(user + ".all."+channel + "."+privilege);
        if (result != null && result.equalsIgnoreCase("true"))
         return true;
     return hasAccess(nick, hostConnection, targetConnection, privilege);
    }

    /**
    * Attempts to log a user in.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param nick the nick
    * @param username the username used to login
    * @param password the password supplied with the username to check.
    * @return An OakUser repersenting the logged in user if the login
    *    was successfull or null if it failed
    */
    public OakUser login(CommandConnection connection, String nick, String userName, String password) {
        String pword = userProps.getProperty(userName);
        String mask = userProps.getProperty(userName + ".nickmask");
        if (pword == null || mask == null || !Crypt.check(pword,password) ||
                !isMatch(mask, nick)) {
            return null;
        }
        OakUser bu = new OakUser(connection, userName, nick, this);
        users.addElement(bu);
        return bu;
    }
    /** changes the password for a user **/
    public void changePassword(String nick, String user, String oldpassword, String newPassword, CommandConnection con){
     String snick = nick.substring(0,nick.indexOf("!"));
     if(!Crypt.check(userProps.getProperty(user),oldpassword)){
      con.sendReply(nick, "Password invalid.");
      return;
     }
     userProps.setProperty(user,Crypt.crypt(newPassword));
     saveUserProps();
     con.sendReply(nick,"Password changed.");
    }


    /**
    * Logs a user out of the system.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param the nick of the user.
    */
    public void logout(CommandConnection connection, String nick) {
        Enumeration enum = users.elements();
	if(nick.indexOf("!")>-1){
	        while (enum.hasMoreElements()) {
        	    OakUser bu = (OakUser) enum.nextElement();
	            if (bu.getNick().equalsIgnoreCase(nick) &&
        	            bu.getConnection() == connection) {
                	users.removeElement(bu);
	                enum = null;
        	        connection.sendReply(nick, "LOGOUT You have been logged out.");
                	return;
	            }
        	}
	}else{
		nick+="!";
		nick = nick.toLowerCase();
		while(enum.hasMoreElements()){
			OakUser bu = (OakUser)enum.nextElement();
			if(bu.getNick().toLowerCase().startsWith(nick) &&
					bu.getConnection() == connection){
				users.removeElement(bu);
				enum = null;
				connection.sendReply(nick, "LOGOUT You have been logged out.");
				return;			
			}
		}
	}
    }

    /**
    * Tests to see if a given nick is currently logged in.
    * @since 1.0
    * @param connection the connection the nick is on.
    * @param nick the nick of the user.
    * @return true if the user is logged in false otherwise.
    */
    public boolean isLoggedIn(CommandConnection connection, String nick) {
System.err.println("nick="+nick);
     return getUserNameFromNick(connection,nick) != null;
    }

    /**
    * Returns the user name for this nick, if logged in through this security manager.
    * If the nick is not registered, null will be returned.
    * @param connection the connection the nick is on
    * @nick the nick that is queried
    * @returns the first username wichthis nick is logged in
    */
    public String getUserNameFromNick(CommandConnection connection, String nick) {
        Enumeration enum = users.elements();
	OakUser bu;
	if(nick.indexOf("!")>-1){
	        while (enum.hasMoreElements()) {
        	    bu = (OakUser) enum.nextElement();
	            if (bu.getNick().equalsIgnoreCase(nick) && bu.getConnection() == connection) {
			enum = null;
	                return bu.getUserName();
        	    }
	        }
	}else{//short nick passed look for less precision.
		nick += "!";
		nick = nick.toLowerCase();
		while(enum.hasMoreElements()){
			bu = (OakUser) enum.nextElement();
			if(bu.getNick().toLowerCase().startsWith(nick) && bu.getConnection() == connection){
				enum = null;
				return bu.getUserName();
			}
		}
	}
        return null;
    }

    /**
    * Reports a nick change to this security manager. If the user
    * is not logged in, this method doesn't do anything.
    * This method must be called on at least every nick change of
    * a nick that is logged in through this security manager.
    * It is adviced to report all nick changes to all active
    * security managers.
    * @since 1.0
    * @param connection the irc connection oldNick is on
    * @param oldNick the old nick.
    * @param newNick the new nick.
    */
    public void nickChanged(CommandConnection connection, String oldNick, String newNick) {
        Enumeration enum = users.elements();
        OakUser bu;
        while (enum.hasMoreElements()) {
            bu = (OakUser) enum.nextElement();
            if (bu.getNick().equalsIgnoreCase(oldNick) && bu.getConnection() == connection) {
                enum = null;
                bu.setNick(newNick, this);
                return;
            }
        }
    }

    /**
     * Adds a user to the system with the specified username,
     * password, and nick mask with no privileges. If this call
     * fails it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user name of the new user.
     * @param password The password of the new user.
     * @param mask The mask of the new user.
     **/
    public void addUser(String user, String password, String mask) {
        userProps.put(user, Crypt.crypt(password));
        userProps.put(user + ".nickmask",mask);
        saveUserProps();
    }

    /**
     * Removes a user from the system. If this method should
     * fail for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to remove from the system.
     **/
    public void removeUser(String user) {
        //this is going to take some tricky stuff. lucky for us we've got some rules
        //in place.
		//it'll remove any key starting with user. just incase a user with the same
		//name is added later.
        Enumeration enum = userProps.propertyNames();
        String key;
        while (enum.hasMoreElements()) {
            key = (String) enum.nextElement();
            if (key.startsWith(user)) {
                userProps.remove(key);
            }
        }
        saveUserProps();
    }

    /**
     * Sets a global privilege for a user. If this method fails
     * for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to set the privilege for.
     * @param privilege The privilege to set.
     * @param setting True to turn set it False to unset it.
     * @param network The network the privilege is for or null if it's for all networks.
     **/
    public void setPrivilege(String user, String privilege, boolean setting,  String network){
	if(user == null)throw new IllegalArgumentException("No user specified.");
	if(privilege == null)throw new IllegalArgumentException("No privilege specified.");
	if(network == null)network = "all";
	 userProps.put(user+"."+network+".all."+privilege,(new Boolean(setting)).toString());
    }

    /**
     * Sets a specific privilege for a user. If this method fails
     * for any reason it should throw a BotSecurityException.
     * @since 1.0
     * @param user The user to set the privilege for.
     * @param privilege The privilege to set.
     * @param setting True to turn set it False to unset it.
     * @param network The network the privilege is for or null if it's for all networks.
     * @param channel The channel the privilege is for or null if it's for all channels.
     **/
    public void setPrivilege(String user, String privilege, boolean setting, String network, String channel) {
	if(user == null)throw new IllegalArgumentException("No user specified.");
	if(privilege == null)throw new IllegalArgumentException("No privilege specified.");
	if(network == null)network = "all";
	if(channel == null)channel = "all";
	userProps.put(user+"."+network+"."+channel+"."+privilege,(new Boolean(setting)).toString());
	saveUserProps();
    }

    /**
     * Registers a network with a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The (short) name of the network.
     * @param description A description of the network.
     **/
    public void addNetwork(String name, String description){
	 return;//networks created when a server is added to it.
    }

    /**
     * Removes a network from a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * Also removes any servers and channels on this network that were registered.
     * Also removes any privileges for this network and any channels on it.
     * @param name The (short) name of the network that is to be removed.
     **/
    public void removeNetwork(String name){
        //this is going to take some tricky stuff. lucky for us we've got some rules
        //in place.
        Enumeration enum = userProps.propertyNames();
        String key;
        while (enum.hasMoreElements()) {
            key = (String) enum.nextElement();
            if (networks.getProperty(key).equalsIgnoreCase(name)) {
                networks.remove(key);
            }
        }
        saveNetworks();
    }

    /**
     * Registers a channel on some network with a security manager.
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The name of the channel to add.
     * @param network The (short) name of the network the channel is on.
     * @param description A description of the channel.
     **/
    public void addChannel(String name, String network,  String description){
      //channels added when a privilege is set for it.
    }

    /**
     * Removes a channel from a security manager
     * Also removes all privileges that are related to this channel
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param name The name of the channel to remove.
     * @param network The (short) name of the network the channel is on.
     **/
    public void removeChannel(String name, String network) {
        //this is going to take some tricky stuff. lucky for us we've got some rules
        //in place.
		//it'll remove any key starting with user. just incase a user with the same
		//name is added later.
        Enumeration enum = userProps.propertyNames();
        String key;
		network = network.toLowerCase()+"."+name.toLowerCase();
        while (enum.hasMoreElements()) {
            key = (String) enum.nextElement();
			        //indexOf("network.name")
            if (key.indexOf(network)>-1) {
                userProps.remove(key);
            }
        }
        saveUserProps();
	}

    /**
     * Registers a server with a security manager
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param address The IP address (normally in DNS form) of the server
     * @param port The TCP port to connect to.
     * @param network The network this server belongs to.
     * @param description A description of this server.
     **/
    public void addServer(String address, String port, String network,  String description){
      networks.put(address,network);
      saveNetworks();
    }

    /**
     * Removes a server from a security manager
     * If this method fails for any reason it should throw a BotSecurityException.
     * @param address The IP address (normally in DNS form) of the server
     * @param port The TCP port to connect to.
     * @param network The network this server belongs to.
     **/
    public void removeServer(String address, String port, String network){
     networks.remove(address);
     saveNetworks();
    }


}
