//WARNING THIS FILE WAS NOT COMPILED
package org.moxy.oak.plugin;
import org.moxy.irc.*;
import org.moxy.oak.*;
public class DefaultChannelPlugin implements ChannelPlugin,ChannelListener{
 Oak bot = null;
 String[] args = null;
 String channel = null;
 String identifier = null;
 IRCConnection connection = null;
 public void init(IRCConnection connection){this.connection = connection;}
 public void initChannelPlugin(Oak bot, String id, String[] args, String channel, String identifier){
  this.bot = bot;
  this.args = args;
  this.channel = channel;
  this.identifier = identifier;
 }
 public IRCConnection getServer(){return connection;}
 public String getChannel(){return channel;}
 public String getIdentifier(){return identifier;}
 public void destroy(){}
 public void configure(){}
 public void initialNickList(java.util.Vector v){}
 public void initialTopic(String topic){}
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
 public void setTopic(String topic, String chanop){}
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
