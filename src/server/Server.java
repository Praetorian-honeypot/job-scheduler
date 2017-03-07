package server;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.logging.Logger;

public class Server extends Observable implements Runnable {
	private transient static final Logger logger = Logger.getLogger( Server.class.getName() );
	private InetSocketAddress address;
	private DatagramSocket datagramSocket;
	
	public Server(InetSocketAddress address) {
		this.address = address;
	}
	
	@Override
	public void run() {
		this.setChanged();
		this.notifyObservers();
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void setDatagramSocket(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}
	
}
