package org.moxy.irc;

/**
 * This encompases all the "dirty" details with connecting to
 * an IRC server and handling incomming and outgoing messages.
 * This class is part of the IRC framework.
 * This class *may* handle DCC requests in the future.
 * @version 1.0
 * @author Marcus Wenzel
 */

/*/
 * Defines a connection between an IRC server and a client.
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
 */


import java.net.Socket;
import java.util.Vector;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.net.InetAddress;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.net.SocketException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;

public class IRCConnection extends DefaultIRCListener implements Runnable
{

	/**
	 * This is for debugging purposes only and will be removed
	 * before the final release.
	 **/
	public final boolean SHOULD_DO = false;
	public static final long MINIMAL_SEND_DELAY = 200;

	private String nick = null;
	private String fullname = null;
	private boolean serverFinalSet = false;
	private String server = null;
	private int port = 6667;
	private boolean connected = false;
	private Thread p_Self_Thread = null;
	private OutputStream sockOut;
	private BufferedReader sockIn;
	private Hashtable channels = new Hashtable();
	private String lastError = "";
	private IdentdThread identd = null;
	private Vector listeners = new Vector();
	private Vector outlisteners = new Vector ();
	private Vector consolelisteners = new Vector();
	private Hashtable unknownChannelListeners = new Hashtable();
	private long lastSendTime = 0;


	/**
	 * Creates an IRCConnection.
	 * Does not start the identd server.
	 * @since 1.0
	 * @param server the hostname or ip address of the IRC server to connect to.
	 * @param port the port number to connect on.
	 * @param nick the nick to attempt to use.
	 * @param fullname the fullname of the person using IRC.
	 */
	public IRCConnection(String server, int port, String nick, String fullname){
		this.server = server;
		this.port = port;
		this.nick = nick;
		this.fullname = fullname;
		registerIRCListener(this);
	}

	/**
	 * Creates an IRCConnection
	 * If the identd server is enabled then a default username and system
	 * are used.
	 * @since 1.0
	 * @param server the hostname or ip address of the IRC server to connect to.
	 * @param port the port number to connect on.
	 * @param nick the nick to attempt to use.
	 * @param fullname the full name of the person using IRC
	 * @param identd wether or not to run the identd server on connect.
	 */
	public IRCConnection(String server, int port, String nick, String fullname, boolean identd){
		this.server = server;
		this.port = port;
		this.nick = nick;
		this.fullname = fullname;
		if (identd)
			this.identd = new IdentdThread();
		registerIRCListener(this);
	}

	/**
	 * Creates an IRCConnection.
	 * Allows the specification of the username, system, and port number
	 * of the identd server.
	 * @since 1.0
	 * @param server the hostname or ip address of the IRC server to connect to.
	 * @param port the port number to connect on.
	 * @param nick the nick to attempt to use.
	 * @param fullname the full name of the person using IRC
	 * @param identd An instance of a configured IdentdThread to use. If null
	 *  none is used.
	 */
	public IRCConnection(String server, int port, String nick, String fullname, IdentdThread identd){
		this.server = server;
		this.port = port;
		this.nick = nick;
		this.fullname = fullname;
		this.identd = identd;
		registerIRCListener(this);
	}

	/**
	 * Returns the nick of the local user.
	 * @since 1.0
	 * @return the nick of the local user.
	 */
	public String getNick() {
		return nick;
	}
	
	public String getFullName(){
	 return fullname;
	}

	public boolean isIdentdEnabled(){return identd != null;}
	
	/**
	 * Returns the hostname of the server connected to.
	 * @since 1.0
	 * @return the hostname of the server.
	 */
	public String getServer() {
		return server;
	}
	/**
	 * Returns the port number connected to
	 * @returns the port number used to connect to the server
	 */
	public int getPort() {
		return port;
	}
	/**
	 * Returns a String repersentation of the last
	 * error that occured.
	 * @since 1.0
	 * @return the last error that occured null if one hasn't occured.
	 */
	public String getLastError() {
		return lastError;
	}

	/**
	 * Sets up an IRCListener to start calling it's methods when
	 * the appropiate messages are recieved.
	 * @since 1.0
	 * @param listener the IRCListener to register.
	 */
	public void registerIRCListener(IRCListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Removes an IRCListener so it's methods aren't called when
	 * messages are recieved.
	 * @since 1.0
	 * @param listener the listener to remove.
	 */
	public void unregisterIRCListener(IRCListener listener){
		listeners.removeElement(listener);
	}

	public void registerOutgoingListener(OutgoingListener listener){
		outlisteners.addElement(listener);
	}

	public void unregisterOutgoingListener(OutgoingListener listener){
		outlisteners.removeElement(listener);
	}
	
	public void registerConsoleListener(ConsoleListener listener){
		consolelisteners.addElement(listener);
	}

	public void unregisterConsoleListener(ConsoleListener listener){
		consolelisteners.removeElement(listener);
	}
	
	private void dispatchToConsole(IRCMessage message)
	{
		int i;
		for (i = 0; i < consolelisteners.size(); i++)
			((ConsoleListener)consolelisteners.elementAt(i)).handleConsoleMsg(message);
	}

	public void registerChannelListener(String channel, ChannelListener listener){
		Channel c = (Channel)channels.get(channel.toLowerCase());
		listener.init(this);
		if(c == null){
		ChannelListener[] listeners = (ChannelListener[])unknownChannelListeners.get(channel.toLowerCase());
		ChannelListener[] newlisteners = new ChannelListener[listeners.length];
		System.arraycopy(listeners,0,newlisteners,0,listeners.length);
		newlisteners[listeners.length] = listener;
		return;
		}
		c.addListener(listener);
	}

	public void unregisterChannelListener(String channel, ChannelListener listener){
	 Channel c = (Channel)channels.get(channel.toLowerCase());
	 if(c == null)return;//channel doesn't exist
	 c.removeListener(listener);
	}

	/**
	 * Sends the specified string to the server.
	 * @since 1.0
	 * @param s the string to send to the server.
	 */
	public synchronized void send(String s)
	{
		try
		{
			if(connected)
			{
				if (((new Date()).getTime() - lastSendTime) < MINIMAL_SEND_DELAY)
					try
					{Thread.sleep((new Date()).getTime() - lastSendTime + MINIMAL_SEND_DELAY);}
					catch(InterruptedException e) {}
				sockOut.write((s+"\n").getBytes());
				sockOut.flush();
				lastSendTime = (new Date()).getTime();
				OutgoingListener listeners[] = outlistenersToArray(outlisteners);
				for(int x = 0; x<listeners.length; x++)
					listeners[x].handleOutgoingMessage(this,new IRCMessage(s,this));
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
  	}

	public void sendPrivMsg(String target, String msg)
	{
	   send("PRIVMSG "+target+" :"+msg);
	}

	public void sendCTCPMsg(String target, String msg)
	{
	   send("PRIVMSG "+target+" :"+msg +"");
	}

	public void sendNotice(String nick, String msg)
	{
 	   send("NOTICE " + nick + " :" + msg);
	}

	/**
	 * sends a ctcp reply (ctcp notice) to the specified nick
	 */
	public void sendCTCPReply(String nick, String msg)
	{
 	   send("NOTICE "+nick+" :" + msg + "");
	}

	/**
	 * Returns wether the instance is currently connected to the IRC server or not
	 * @sicne 1.0
	 * @return true if connected to the server false otherwise.
	 */
	public boolean connected(){return connected;}

	/**
	 * Disconnects from the IRC server.
	 * @since 1.0
	 */
	public void disconnect(){
	 disconnect("http://www.moxy.org");
	}

	/**
	 * Disconnectes from the IRC server with the specified string.
	 * @since 1.0
	 * @param s the string to pass with the quit message.
	 */
	public void disconnect(String s){
	 connected = false;
	 send("QUIT :"+s);
         try{
  	  sockOut.write(("\n\n\n").getBytes());//might help flush the quit msg
	  sockOut.flush();
	 }catch(Exception e){}
	 IRCListener[] arry = listenersToArray(listeners);
	 for(int x = 0; x<arry.length; x++)
	  arry[x].handleDisconnect(this);
	}

	/**
	 * Attempts to connect to the IRC server.
	 * @since 1.0
	 **/
	public void connect() throws UnknownHostException, IOException{
	 if(connected){return;}
	 try{
	  if(identd != null){
           identd.startIdentd();
 	  }
	  Socket socket = new Socket(server,port);
	  sockIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	  sockOut = socket.getOutputStream();
	  connected = true;  
	  p_Self_Thread = new Thread(this);
	  p_Self_Thread.start();
	  String localHostName = InetAddress.getLocalHost().getHostName();
	  server = socket.getInetAddress().getHostName();
	  String userCommand = "USER "+nick+" "+localHostName+" "+server+ " :"+fullname;
	  send(userCommand);
	  send("NICK "+nick);
	  IRCListener[] arry = listenersToArray(listeners);
	  try{
	   for(int x = 0; x<arry.length; x++)
            arry[x].handleConnect(this);
 	  }catch(Exception e){e.printStackTrace();}
	 }catch(UnknownHostException uhe){
          connected = false;
          lastError = "Unknown Host";
          throw uhe;
	 } 
	  catch(IOException ioe){
           connected = false;
           lastError = "Problem reading or writing to the socket.";
           throw ioe;
	  }
	}

	/**
	 * This method should not be called directly it's called by the connect() method.
	 * This method handles the incomming side of the socket.
	 * @since 1.0
	 */
	public void run(){
	 String mesg,command;
	 while(connected){   
	  try{
	   mesg = sockIn.readLine();
           if(mesg == null){continue;}
	   if(org.moxy.oak.Preferences.DEBUG_LEVEL >= 10)
	    System.out.println(mesg);
	   if(mesg.startsWith("PING")){
            send("PONG "+mesg.substring(mesg.indexOf(":")+1,mesg.length()));
            continue;
           }else{
            if(mesg.indexOf(0x01) > -1 && mesg.toLowerCase().indexOf("version") > -1){
             send("NOTICE "+mesg.substring(1,mesg.indexOf("!"))+
    		  " :VERSION Oak an IRC Framework http://www.moxy.org/oak");   
            }
            if(mesg.indexOf(0x01) > -1 && mesg.toLowerCase().indexOf("ping") > -1){
             send("NOTICE "+mesg.substring(1,mesg.indexOf("!"))+
                 " :PING "+mesg.substring(mesg.lastIndexOf(" ")));
            }
            if(mesg.startsWith("ERROR"))
             command = "ERROR";
            else
             command = mesg.substring(mesg.indexOf(" ")+1,mesg.indexOf(" ",mesg.indexOf(" ")+1));
            //check for NICK message
            //handle all other messages
            if(command.equalsIgnoreCase("NICK") && mesg.startsWith(":"+nick)){
              nick = mesg.substring(mesg.lastIndexOf(':')+1);
            }
            IRCMessage message = new IRCMessage(mesg,this);
            if(!serverFinalSet && message.isServerMessage()){
             serverFinalSet = true;
             server = message.getSender();
            }
	    if (message.isConsoleMessage())
	     dispatchToConsole(message);
            IRCListener[] arry = listenersToArray(listeners);
            for(int x = 0; x<arry.length; x++){
             if(arry[x] == null){
              System.err.println("IRCConnection: A null value was found "+
    	   		"while processing the IRCListener array at position"+x+". The array has a "+
    	   		"length of "+arry.length);
              continue;
             }
             arry[x].handleIRCMessage(this,message);
            }
           }
	  }catch(SocketException e1){
            e1.printStackTrace();
            lastError = "Socket problem.";
            disconnect();
	   }
           catch(Exception e){e.printStackTrace();}
	 }
	}

	/**
	 * Looks to see if the local user is currently on the specified channel
	 * and returns a Channel object reperesnting the channel.
	 * @since 1.0
	 * @param name the name of the channel.
	 * @return a Channel object if the local user is on the channel or null
	 *     otherwise.
	 */
	public Channel getChannel(String name){
 	 if (name == null)
		return null;
	 return (Channel)channels.get(name.toLowerCase());
	}

	/**
	 * Returns an enumeration of all the channels the current user is on.
	 * @since 1.0
	 * @return an enumeration of all the channels the current user is on.
	 */
	public Enumeration getChannels(){
	 return channels.elements();
	}
 
	/**
	 * Sends a join command to the server with a key.
	 * @since 1.0
	 * @param channel the channel to attempt to join.
	 * @param key the key to use to join the channel.
	 **/
	public void joinChannel(String channel, String key){
	 if(key == null)joinChannel(channel);
	 else send("JOIN "+channel+" "+key);
	}

	/**
	 * Sends a join command to the server.
	 * @since 1.0
	 * @param channel the channel to attempt to join.
	 */
	public void joinChannel(String channel){
	 send("JOIN "+channel);
	}

	/**
	 * Sends a part command to the server.
	 * @since 1.0
	 * @param channel the name of the channel to part.
	 */
	public void partChannel(String channel){
	 partChannel(channel,"http://www.moxy.org look it up!");
	}
	/**
	 * Sends a part command to the server with the specified message.
	 * @since 1.0
	 * @param channel the channel to part.
	 * @param message the message to send.
	 */
	public void partChannel(String channel, String message){
	  send("PART "+channel+" :"+message);
	}
	/**
	 * Sends a message to a channel or nick.
	 * @since 1.0
	 * @param tgt the channel or nick to send the message to.
	 * @param msg the message to send.
	 */
	public void sendMsg(String tgt, String msg){
	 send("PRIVMSG "+tgt+" :"+msg);
	}
	/**
	 * Changes the local users nick.
	 * @since 1.0
	 * @param nick the nick to change to.
	 */
	public void changeNick(String nick){
	 send("NICK "+nick);
	}
	/**
	 * Ops a nick on a channel.
	 * @since 1.0
	 * @param channel the name of the channel to op the nick in.
	 * @param nick the nick to op.
	 */
	public void op(String channel, String nick){
	 send("MODE "+channel+" +o "+nick);
	}
	/**
	 * Voices a nick in a channel.
	 * @since 1.0
	 * @param channel the name of the channel to voice the nick in.
	 * @param nick the nick to voice.
	 */
	public void voice(String channel, String nick){
	 send("MODE "+channel+" +v "+nick);
	}
	/**
	 * Deops a nick in a channel.
	 * @since 1.0
	 * @param channel the channel to deop the nick in.
	 * @param nick the nick to deop.
	 */
	public void deOp(String channel, String nick){
	 send("MODE "+channel+" -o "+nick);
	}
	/**
	 * Devoices a nick in a channel.
	 * @since 1.0
	 * @param channel the channel to devoice the nick in.
	 * @param nick the nick to devoice.
	 */
	public void deVoice(String channel, String nick){
	 send("MODE "+channel+" -v "+nick);
	}
	/**
	 * Kicks a nick from a channel.
	 * @since 1.0
	 * @param channel the channel to kick the nick from.
	 * @param nick the nick to kick.
	 */
	public void kick(String channel, String nick){
	 kick(channel,nick,"http://www.moxy.org visit it now!");
	}
	/**
	 * Kicks a nick from a channel with a reason.
	 * @since 1.0
	 * @param channel the channel to kick the nick from
	 * @param nick the nick to kick.
	 * @param reason the reason for kicking.
	 */
	public void kick(String channel, String nick, String reason){
	 send("KICK "+channel+" "+nick+" :"+reason);
	}
	/**
	 * Bans a nick mask from a channel.
	 * @since 1.0
	 * @param channel the channel to ban in.
	 * @param nick the nick mask to ban.
	 */
	public void ban(String channel, String nick){
	 send("MODE "+channel+" +b "+nick);
	}
	/**
	 * Removes a ban from a channel.
	 * @since 1.0
	 * @param channel the channel to unban in.
	 * @param nick the nick mask to unban.
	 */
	public void unBan(String channel, String nick){
	 send("MODE "+channel+" -b "+nick);
	}
	/**
	 * sets the topic for a channel
	 * @param channel The channel of which the topic should be set.
	 * @param topic The new topic line.
	 */
	 public void setTopic(String channel, String topic)
	 {
  	   send("TOPIC " + channel + " :" + topic);
	 }
	/**
	 * Sends a ctcp reply.
	 * @since 1.0
	 * @param nick The nick to send the reply to.
	 * @param type The type of ctcp reply.
	 * @param msg The message to send.
	 */
	 public void sendCTCPReply(String nick, String type, String msg){
	  send("NOTICE "+nick+" :"+type+" "+msg+"");   
	 }
	/**
	 * Send ctcp request.
	 * @since 1.0
	 * @param nick The nick to request from.
	 * @param type The type of ctcp request.
	 * @param msg The message for the request.
	 */
	 public void sendCTCPRequest(String nick, String type, String msg){
	  send("PRIVMSG "+nick+" :"+type+" "+msg+"");   
	 }
	 /**
	  * Send an action.
	  * @since 1.0
	  * @param tgt The target to send the action to.
	  * @param msg The message to send with the action.
	  */
	  public void sendAction(String tgt, String msg){
       send("PRIVMSG "+tgt+" :ACTION "+msg+"");	
	  }


	// IRCListener implementation

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleERROR(IRCConnection connection, IRCMessage message){
 	 dispatchToConsole(message);
	 disconnect();
	 lastError = message.getRawLine();
	}
	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleJOIN(IRCConnection connection, String nick, String channel){
	Object chanObj;
	 if(IRCMessage.getNick(nick).equals(getNick())){
	  Channel chan = new Channel(channel, this);
	  channels.put(channel.toLowerCase(),chan);
	  send("MODE "+channel);
	  send("MODE "+channel+" +b");
	  send("WHO "+channel);
	  dispatchToConsole(new IRCMessage(":" + nick + " JOIN " + channel, connection));
	 }else{
	  chanObj = channels.get(channel.toLowerCase());
	  Channel chan = (Channel)chanObj;
	  chan.join(nick);
	 }
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handlePART(IRCConnection connection, String nick, 
	                       String channel, String msg)
	{
		Channel chan = (Channel)channels.get(channel.toLowerCase());
		chan.part(IRCMessage.getNick(nick), IRCMessage.getUser(nick), 
		          IRCMessage.getHost(nick), msg);
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleQUIT(IRCConnection connection, String nick, String msg){
	 String name = IRCMessage.getNick(nick);
	 String ident = IRCMessage.getUser(nick);
	 String host = IRCMessage.getHost(nick);
	 Enumeration enum = channels.elements();
	 while(enum.hasMoreElements())
	  ((Channel)enum.nextElement()).quit(name, ident, host, msg);
	 enum = null;
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleNICK(IRCConnection connection, String nick, String newName){
	 String onick = IRCMessage.getNick(nick);
	 Enumeration enum = channels.elements();
	 while(enum.hasMoreElements())
	  ((Channel)enum.nextElement()).nickChange(onick,newName);
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleKICK(IRCConnection connection, String kicker, 
	                       String chanName, String target, String msg)
	{
	 Channel chan = (Channel)channels.get(chanName.toLowerCase());
	 if(chan == null){return;}//not currently on that channel.
	 //version 1.9 used nick instead of target but I couldn't figure out how that compiled.
	 //nick was commented out.
	 chan.kick(target,msg, IRCMessage.getNick(kicker));
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handle367(IRCConnection connection, IRCMessage message){//ban list
	 Channel chan = (Channel)channels.get(message.getParam(2).toLowerCase());
	 if(chan == null){return;}
	 chan.addInitialBan(message.getParam(3));
         //can provide banner and time too.
	}
        //368 RPL_ENDOFBANLIST
        //if Channel wants to send all the initial bans to the listeners we can implement this to
        //signal to channel that all the initial bans are in.
        public void handle368(IRCConnection connection, IRCMessage message){
	 Channel chan = (Channel)channels.get(message.getParam(2).toLowerCase());	
         chan.initialBanListComplete();
        }

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handle353(IRCConnection connection, IRCMessage message){//set's NAMES list
	 Channel channel = (Channel)channels.get(message.getParam(3).toLowerCase());
         if(org.moxy.oak.Preferences.DEBUG_LEVEL>=20)	
	  System.out.println("IRCConnection::handle353: channel=" + message.getParam(3));
         if(channel == null)return;
	 IRCLine line = message.getIRCLine();
	 String name;
	 boolean chanop;
	 boolean voice;
	 while(line.hasMoreTokens()){
		chanop = false;
		voice = false;
		name = line.getNextToken();
		if (name.startsWith("@"))
		{
			name = name.substring(1);
			chanop = true;
		}
		else if (name.startsWith("+"))
		{
			name = name.substring(1);
			voice = true;
		}
		
		channel.addInitialNick(new ListNick(name, chanop, voice));
	 }
	}
	
	//If Channel wants to pass all the names to the listeners in one call this can signal that all
	//the initial nicks have been sent.
	public void handle366(IRCConnection connection, IRCMessage message){
          if(org.moxy.oak.Preferences.DEBUG_LEVEL>=1)
	   System.out.println("IRCConnection.366} channel="+message.getParam(2));
	  Channel chan =(Channel)channels.get(message.getParam(2).toLowerCase());
          chan.initialNickListComplete();
        }

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handle332(IRCConnection connection, IRCMessage message){//topic
	 Channel chan = (Channel)channels.get(message.getParam(2).toLowerCase());
	 if(chan == null){return;}
	 if(org.moxy.oak.Preferences.DEBUG_LEVEL>=3)
 	  System.out.println("{IRCConnection.handle332} Channel = message.getParams(2)|"+chan);
	 chan.initialTopic(message.getIRCLine().getRemaining());
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handle324(IRCConnection connection, IRCMessage message)
	{//channel mode
		if (org.moxy.oak.Preferences.DEBUG_LEVEL>=1)
			System.out.println(message.getRawLine());
		String sender = IRCMessage.getNick(message.getSender());
		Channel chan = (Channel)channels.get(message.getParam(2).toLowerCase());
		if(chan == null)
			return;
		String modes = message.getParam(3);
		int param = 4;
		boolean pm = true;///default for initial mode
		char curMode;
		for(int x = 0; x < modes.length(); x++)
		{
			curMode = modes.charAt(x);
			if (curMode == '+')
			{
				pm = true;
				continue;
			}
			
			if (curMode == '-')
			{
				pm = false;
				continue;
			}
			
			switch(curMode)
			{
				case 't':
					chan.initialOpTopic(pm);
					break;
				case 'n':
					chan.initialNoExtMsg(pm);
					break;
				case 'i':
					chan.initialInviteOnly(pm);
					break;
				case 'l':
					if(pm)
						try
						{
							chan.initialLimit(true, Integer.parseInt(message.getParam(param++)));
						}
						catch(NumberFormatException nfe)
						{
							nfe.printStackTrace();
						}
					else
						chan.initialLimit(false,-1);
					break;
				case 'p':
					chan.initialPrivate(pm);
		   			break;
				case 's':
					chan.initialSecret(pm);
					break;
				case 'k':
					if (pm)
						chan.initialKey(true, message.getParam(param++));
					else
						chan.initialKey(false, null);
					break;
				case 'm':
					chan.initialModerated(pm);
	   				break;
	  		}//switch
		}//for
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handle433(IRCConnection connection, IRCMessage message){
	 int d = (int)(Math.random()*10000);//max of 5 digit number generates more 4 digits
	 String alnick = "Guest" + d;//might be nice to make this customizable
	 try{
	 	 sockOut.write(("NICK "+alnick+"\n").getBytes());
 		 sockOut.flush();
	 }catch(IOException ioe){
 		 connection.send("NICK " + alnick);
	}
	 nick = alnick;
	}

	/**
	 * This is used internally only and should not be called directly.
	 */
	public void handleMODE(IRCConnection connection, String moder, String target, 
	                       char mode, boolean on, String param)
	{
	 String sender = IRCMessage.getNick(moder);
	 if(target.startsWith("#") || target.startsWith("&")){//else user mode
	  Channel chan = (Channel)channels.get(target.toLowerCase());
	  if(chan == null)
	  	return;//not on that channel

	  switch(mode){
	    case 'o': chan.op(param,on,sender);break;
	    case 'v': chan.voice(param,on,sender);break;
	    case 'l': if(on)
			try{
		 	 chan.setLimit(Integer.parseInt(param),sender);
			}catch(NumberFormatException nfe){nfe.printStackTrace();}
		      else
			chan.setLimit(0,sender);
		      break;
	    case 'k': if(on)
			chan.setKey(param,sender);
		      else
			chan.setKey(null,sender);
		      break;
	    case 'i':chan.setInviteOnly(on,sender);break;
	    case 'p':chan.setPrivate(on,sender);break;
	    case 's':chan.setSecret(on,sender);break;
	    case 'm':chan.setModerated(on,sender);break;
	    case 'n':chan.setNoExtMsg(on,sender);break;
	    case 't':chan.setOpTopic(on,sender);break;
	   }
	 }//if(target.startsWith("#")...)	 
	}

	public void handlePRIVMSG(IRCConnection connection, String sender, String target, String msg)
	{
		Channel channel;
		
		if (target.startsWith("#") || target.startsWith("&"))
                   // other channel prefixes! only prefixes recgonized by the RFC
		   // I believe ircX also uses %
		{
			channel = (Channel)channels.get(target.toLowerCase());
			if (channel == null)
				return;
			channel.handleMsg(sender, new IRCLine(msg));
		}
	}
	
	public void handleCTCPMSG(IRCConnection connection, String sender, String target, String msg)
	{
		Channel channel;
		
		if (target.startsWith("#") || target.startsWith("&"))
                   // other channel prefixes! only prefixes recgonized by the RFC
		   // I believe ircX also uses %
		{
			channel = (Channel)channels.get(target.toLowerCase());
			if (channel == null)
				return;
			channel.handleCTCPMsg(sender, new IRCLine(msg));
		}
	}


 	public void handleTOPIC(IRCConnection connection, String oper, String channel, String topic){
	 Channel chan = (Channel)channels.get(channel.toLowerCase());
	 if(chan == null){return;}//nothing to notify
	 chan.setTopic(topic,oper);
 	}

	/**
	 * Called for all incomming messages from the IRC server.
	 * This analyzes the IRCMessage looking for some commonly handled
	 * commands calling specific methods.
	 * @since 1.0
	 * @param IRCConnection The connection the message orignated from.
	 * @param IRCMessage The message recieved.
	 */
	public void handleOther(IRCConnection connection, IRCMessage message)
	{
		switch(message.getMsgType())
		{
			case IRCMessage.MSG_ERROR:
				handleERROR(connection, message);
				return;
			case IRCMessage.RPL_BANLIST:
				handle367(connection, message);
				return;
			case IRCMessage.RPL_ENDOFBANLIST:
				handle368(connection, message);
				return;
			case IRCMessage.RPL_NAMREPLY:
				handle353(connection, message);
				return;
			case IRCMessage.RPL_ENDOFNAMES:
				handle366(connection, message);
				return;
			case IRCMessage.RPL_TOPIC:
				handle332(connection, message);
				return;
			case IRCMessage.RPL_CHANNELMODEIS:
				handle324(connection, message);
				return;
			case IRCMessage.ERR_NICKNAMEINUSE:
				handle433(connection, message);
				return;
		 }
	}



	private static IRCListener[] listenersToArray(Vector v){
	 int size = v.size();
	 IRCListener[] retValue = new IRCListener[size];
	 for(int x  = 0; x< size; x++)
	  retValue[x] = (IRCListener)v.elementAt(x);
	 return retValue;
	}

	private static OutgoingListener[] outlistenersToArray(Vector v){
	 int size = v.size();
	 OutgoingListener[] retValue = new OutgoingListener[size];
	 for(int x = 0; x<size; x++)
	  retValue[x] = (OutgoingListener)v.elementAt(x);
	 return retValue;
	}
}
