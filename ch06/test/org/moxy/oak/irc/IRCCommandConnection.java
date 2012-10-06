package org.moxy.oak.irc;
import org.moxy.irc.*;
public class IRCCommandConnection extends IRCConnection implements CommandConnection{
	public static final int CTCP_REPLY = 0;
	public static final int NOTICE_REPLY = 1;
	public static final int PRIVMSG_REPLY = 2;
	private int rpl_type = 1;
    public IRCCommandConnection(String server, int port, String nick, String fullname) {
     super(server,port,nick,fullname);
    }

    public IRCCommandConnection(String server, int port, String nick, String fullname,boolean identd){
     super(server,port,nick,fullname,identd);
    }

    public IRCCommandConnection(String server, int port, String nick, String fullname, IdentdThread identd) {
     super(server,port,nick,fullname,identd);
    }
 
   public void sendReply(String nick, String message){
    nick = IRCMessage.getNick(nick);
    switch(rpl_type){
      case NOTICE_REPLY: sendNotice(nick,message);break;
      case CTCP_REPLY  : sendCTCPReply(nick,message);break;
      case PRIVMSG_REPLY:sendPrivMsg(nick,message);break;
    }
   }

   public void setReplyType(int reply){
    switch(reply){
	case NOTICE_REPLY: rpl_type = NOTICE_REPLY;return;
	case CTCP_REPLY  : rpl_type = CTCP_REPLY; return;
	case PRIVMSG_REPLY: rpl_type = PRIVMSG_REPLY;return;
    }
    throw new IllegalArgumentException("Not a valid reply type.");
   }
}
