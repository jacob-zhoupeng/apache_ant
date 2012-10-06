package org.moxy.irc;

public class ListNick implements sun.misc.Compare
{
	private String name;
	private String ident;
	private String hostname;
	private boolean chanop;
	private boolean voice;
	private boolean away;
	private boolean ircop;

		public ListNick(String name)
	{
		this(name, false, false);
	}
	
		public ListNick(String name, boolean chanop, boolean voice)
	{
		this(name, null, null, chanop, voice);
	}
	
		public ListNick(String name, String ident, String hostname)
	{
		this(name, ident, hostname, false, false);
	}
	
		public ListNick(String name, String ident, String hostname, boolean chanop, boolean voice)
	{
		this.name = name;
		this.ident = ident;
		this.hostname = hostname;
		this.chanop = chanop;
		this.voice = voice;
		this.away = false;
		this.ircop = false;
	}
	
	public int doCompare(Object object1, Object object2)
	{
		ListNick nick1, nick2;
		
		nick1 = (ListNick)object1;
		nick2 = (ListNick)object2;
		
		return nick1.getName().compareTo(nick2.getName());
	}

	public String getName()
	{
		return name;
	}
	
	void setName(String newName)
	{
		this.name = newName;
	}
	
	public String getIdent()
	{
		return ident;
	}
	
	public String getHostname()
	{
		return hostname;
	}

	public boolean isChanop()
	{
		return chanop;
	}
	
	void setChanop(boolean chanop)
	{
		this.chanop = chanop;
	}
	
	public boolean hasVoice()
	{
		return voice;
	}

	void setVoice(boolean voice)
	{
		this.voice = voice;
	}
	
	public boolean isAway()
	{
		return away;
	}
	
	void setAway(boolean away)
	{
		this.away = away;
	}
	
	public boolean isIRCOp()
	{
		return ircop;
	}
	
	void setIRCOp()
	{
		this.ircop = true;
	}

}
