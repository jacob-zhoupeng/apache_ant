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
package irssibot.core;

/**
 * Represents simple timer counting downwards from the given value
 * and dying when reaching zero.
 *
 * @author Matti Dahlbom
 */
public class Timer extends Thread {
    protected int timeToLive = 0;

    public Timer(int seconds)
    {
	timeToLive = seconds;
    }

    /**
     * The actual life cycle 
     *
     */
    protected void doRun() 
    {
	while( timeToLive > 0 ) {
	    try {
		sleep(1000);
	    } catch( InterruptedException ie ) {}
	    timeToLive--;
	}
    }

    public void run()
    {
	doRun();
    }

    public void kill()
    {
	timeToLive = 0;
    }
}

