/*
 * $Id: AbstractModule.java,v 1.1.1.1 2001/03/26 10:57:42 matti Exp $
 */

package irssibot.modules;

import irssibot.core.*;

import java.util.Vector;
import java.util.Properties;

/**
 * Storage class for storing data about a IrcMessage and the
 * ServerConnection that sent it.
 *
 */
class MessageData 
{
    public IrcMessage message = null;
    public ServerConnection serverConnection = null;

    public MessageData(IrcMessage message,ServerConnection serverConnection)
    {
	this.message = message;
	this.serverConnection = serverConnection;
    }
}

/**
 * Base class for all modules for IrssiBot. Implements some basic
 * functionality common for all modules. The module acts as a
 * 'consumer' for the bot core, who 'produces' IrcMessage objects
 * to the module's processing queue.
 *
 * @author Matti Dahlbom
 * @version $Name:  $ $Revision: 1.1.1.1 $ 
 */
public abstract class AbstractModule extends Thread 
{
    /**
     * Vector containing MessageData objects waiting for processing.
     */
    private final Vector messageQueue = new Vector();
    /**
     * A lock object used for producer-consumer synchronization
     */
    private final Object processLock = new Object();
    /**
     * Indicates whether the module thread should continue executing
     * its run() loop.
     */
    private boolean alive = true;
    /**
     * A state variable indicating whether the module is executing ok.
     * When an exception occurs in the consumer thread, this variable is set
     * to the thrown exception and it is thrown to the producer thread
     * next time addMessage() is called.
     *
     * @see #addMessage(IrcMessage,ServerConnection)
     */
    private Exception consumerException = null;

    /**
     * Module should return a descriptive info string 
     * containing at least module's name and version.
     *
     * @return module info string
     */
    public abstract String getModuleInfo();
    /**
     * Gets module's state as a Properties object. If module does not wish to save state 
     * OR its state has not been changed since last getState(), it should return null. 
     * This is to minimize disk access. 
     *
     * @return state of module as a Properties object
     */
    public Properties getState() 
    {
	return null;
    }
    /**
     * called upon loading the module
     *
     * @param state the initial state of module as an Properties object, or
     *              null if no state was saved for module.
     * @param core a Core instance. this can be used to initialize module if no state was
     *             retrieved.
     * @return true if ok, false if error. modules returning false from onLoad() will
     *         be unloaded immediately.
     * @see irssibot.core.Core
     */
    public boolean onLoad(Properties state,Core core) 
    {
	return true;
    }
    /**
     * Called upon unloading the module.
     *
     */
    public void onUnload()
    {
	/* do nothing */
    }

    /**
     * Appends the new IrcMessage+ServerConnection pair to the end of the message queue. 
     * Access to the queue is synchronized. When a new message is added to the queue,
     * the waiting consumer thread is notify()'ed.
     * 
     * @param message the IrcMessage to append to the message queue
     * @exception Exception thrown if an exception was thrown by the
     * consumer thread in processMessage().
     * @see #processMessage(IrcMessage,ServerConnection)
     */
    public final void addMessage(IrcMessage message,ServerConnection serverConnection) 
	throws Exception
    {
	synchronized ( processLock ) {

	    //	    System.out.println(getClass().getName() + ".addMessage(): adding message: " + 
	    //	       message.trailing + " (" + currentThread().getName() + ")");
	
	    /* see if an exception was thrown by the consumer thread */
	    if( consumerException != null ) {
		alive = false;
		throw consumerException;
	    }

	    if( (message == null) || (serverConnection == null) ) {
		throw new IllegalArgumentException("AbstractModule.addMessage(): " +
						   "either parameter cannot be null!");
	    }

	    /* add the message at the end of the queue */
	    messageQueue.addElement(new MessageData(message,serverConnection));

	    /* notify the consumer thread */
	    processLock.notifyAll();
	}
	//System.out.println(getClass().getName() + ".addMessage(): exiting");
    }

    /**
     * Fetch next message in queue and pass it on to processMessage(). If
     * message is empty, the thread wait()s.
     *
     * @see #processMessage(IrcMessage)
     */
    protected final void fetchNextMessage() {
	//	System.out.println(getClass().getName() + ".fetchNextMessage(): synchronizing.." +
	//	   " (" + currentThread().getName() + ")");

	MessageData messageData = null;

	synchronized ( processLock ) {
	    //  System.out.println(getClass().getName() + ".fetchNextMessage(): synchronized!" +
	    //	       " (" + currentThread().getName() + ")");

	    if( messageQueue.size() == 0 ) {
		try {
		    //		    System.out.println(getClass().getName() + ".fetchNextMessage(): waiting.." +
		    //	       " (" + currentThread().getName() + ")");
		    processLock.wait();
		} catch ( InterruptedException e ) {
		    /* do not handle */
		}
		//		System.out.println(getClass().getName() + ".fetchNextMessage(): wait ended" +
		//	   " (" + currentThread().getName() + ")");
	    }
	    messageData = (MessageData)messageQueue.remove(0);
	}
	    
	try {
	    //	    System.out.println(getClass().getName() + ".fetchNextMessage(): processing: " + 
	    //	       messageData.message.trailing + 
	    //	       " (" + currentThread().getName() + ")");
	    
	    processMessage(messageData.message,
			   messageData.serverConnection);
	} catch ( Exception e ) {
	    consumerException = e;
	}
    } 

    /**
     * Processes messages as they come in. Classes inheriting this 
     * class must override this method. 
     *
     * @param message the incoming IrcMessage
     */
    protected abstract void processMessage(IrcMessage message,ServerConnection serverConnection);

    /**
     * Thread loop. Continues execution until variable <b>alive</b> 
     * is set to value <b>false</b>.
     *
     */
    public final void run() 
    {
	while( alive ) {
	    fetchNextMessage();
	}
	putlog("run(): module thread exiting..");
    }

    /**
     * Write a log message to stdout.
     *
     * @param msg log message to write
     */
    protected void putlog(String msg) 
    {
	System.out.println(getClass().getName() + ": " + msg);
    }
}










