package org.moxy.irc;
/**
 * This class was taken mostly from hex-binary's hIRC<br>
 * http://members.xoom.com/hexbinary/<br>
 * I've added a main method, taken all the
 * System.out stuff out and made a few other minor
 * modifications.
 * @since 1.0
 * @author Hex-binary, Marcus Wenzel
 **/
/*/
 * An identd server
 * Copyright (C) 1999 Hex-binary
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


import java.net.*;
import java.io.*;

public class IdentdThread implements Runnable {

    private boolean on;
    private Thread t;
    private ServerSocket ssocket;

    int port = 113;
    String user = "user";
    String system = "java";

    /**
     * Creates an IdentdThread with the default values:
     * <br> port = 113
     * <br> user = user
     * <br> system = java
     * @since 1.0
     */
    public IdentdThread() {
        on = false;
    }

    /**
       * Creates an IdentdThread with the specified params.
       * @since 1.0
       */
    public IdentdThread(int port, String user, String system) {
        this.port = port;
        this.user = user;
        this.system = system;
    }

    /**
       * Starts the thread listening for a connection.
       * @since 1.0
       **/
    public void startIdentd() {
        on = true;
        t = new Thread(this);
        t.start();
    }
    /**
       * Stops the thread from listening for a connection.
       * @since 1.0
       */
    public void stopIdentd() {
        on = false;
        try {
            ssocket.close();
        } catch (Exception e) {}
    }

    /**
       * Handles the connection once established.
       * Then stops listening.
       * @sice 1.0
       */
    public void run() {
        try {
            ssocket = new ServerSocket(port);
        } catch (Exception e) {
            System.err.println("An error occured while starting the Identd Daemon. Port may not be available or accessible");
            return;
        }
        while (on) {
            try {
                Socket s = ssocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                String request = in.readLine();
                out.println(request + " : USERID : " + system + " : " + user);
                s.close();
                stopIdentd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
       * Provides the ability to run the ident server stand alone.
       * @since 1.0
       */
    public static void main(String arg[]) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter user name:");
        String user = in.readLine();
        System.out.print("Enter system type:");
        String system = in.readLine();
        IdentdThread t = new IdentdThread(113, user, system);
        t.startIdentd();
    }
}
