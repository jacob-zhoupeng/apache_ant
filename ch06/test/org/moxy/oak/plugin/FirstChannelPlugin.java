//WARNING THIS FILE WAS NOT COMPILED
package org.moxy.oak.plugin;
import org.moxy.irc.*;
import org.moxy.oak.*;
public class FirstChannelPlugin extends DefaultChannelPlugin{
 IRCConnection connection;
 String chan;
 String topic;
 public void init(IRCConnection connection){
  super.init(connection);
  System.out.println("<--FirstChannelPlugin inited(IRCConnection) connection set-->");
  this.connection = connection;
 }
 public void initChannelPlugin(Oak bot, String id, String[] args, String channel, String identifier){
  super.initChannelPlugin(bot,id, args,channel,identifier);
  this.chan = channel;
System.out.println("org.moxy.oak.plugin.FirstChannelPlugin has been loaded.");
 }
 public void destroy(){System.out.println("{FCP destroyed()}");}
 public void configure(){}
 public void initialNickList(java.util.Vector v){}
 public void initialTopic(String topic){
  System.out.println("FirstChannelPlugin initial topic set.");
  this.topic = topic;
 }
 public void initialOpTopic(boolean b){}
 public void initialNoExtMsg(boolean b){}
 public void initialSecret(boolean b){}
 public void initialInviteOnly(boolean b){}
 public void initialPrivate(boolean b){}
 public void initialModerated(boolean b){}
 public void initialLimit(boolean b, int limit){}
 public void initialKey(boolean b, String key){}
 public void initialBan(java.util.Vector mask){}
 public void setInviteOnly(boolean b, String chanop){}
 public void setPrivate(boolean b, String chanop){}
 public void setSecret(boolean b,String chanop){}
 public void setModerated(boolean b, String chanop){}
 public void setNoExtMsg(boolean b, String chanop){}
 public void setOpTopic(boolean b,String chanop){}
 public void setKey(String key, String chanop){}
 public void setLimit(int limit, String chanop){}
 public void ban(String mask,boolean mode, String chanop){}
 public void setOtherMode(char mode, boolean b, String chanop){}
 public void setTopic(String topic, String chanop){
  System.out.println("FirstChannelPlugin topic changed.");
  chanop = chanop.substring(0,chanop.indexOf("!"));
  if(chanop.equalsIgnoreCase(connection.getNick()))
  {
	this.topic = topic;
	return;//plugin just changed topic.
  }
  connection.setTopic(chan,this.topic);
  connection.sendMsg(chan,chanop+": you don't like the topic?");
 }
 public void setURL(String url){}
 public void join(String name,String ident, String host){}
 public void part(String name, String ident, String host, String mesg){}
 public void quit(String name, String ident, String host, String mesg){}
 public void nickChange(String oldnick,String newNick){}
 public void kick(String name,String reason, String chanop){}
 public void op(String name, boolean mode, String chanop){}
 public void voice(String name, boolean mode, String chanop){}
 public void reset(){}
 public void handleMessage(String sender, IRCLine message){}
 public void handleAction(String sender, IRCLine message){}
}
