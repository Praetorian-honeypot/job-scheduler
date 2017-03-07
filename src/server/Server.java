package server;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Observable;

public class Server extends Observable implements Runnable {
	
	private InetSocketAddress serverAddress;
	private DatagramSocket serverDatagramSocket;
	
	public Server(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	@Override
	public void run() {
		this.setChanged();
		this.notifyObservers();
	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public DatagramSocket getServerDatagramSocket() {
		return serverDatagramSocket;
	}

	public void setServerDatagramSocket(DatagramSocket serverDatagramSocket) {
		this.serverDatagramSocket = serverDatagramSocket;
	}
	
}
