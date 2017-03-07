package client;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class Client implements Runnable {

	private transient static final Logger logger = Logger.getLogger( Client.class.getName() );
	protected InetSocketAddress serverAddress;
	protected DatagramSocket clientSocket;
	
	
	/**
	 * Constructs a new frame for the client.
	 * 
	 * @param frame the frame
	 */
	public Client() {
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
