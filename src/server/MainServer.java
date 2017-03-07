package server;

import java.net.InetSocketAddress;

public class MainServer {
	public static void main(String args[]) {
		String address = "localhost";
		int port = 8900;
		
		InetSocketAddress serverAddress = new InetSocketAddress(address,port);
		Server server = new Server(serverAddress);
		
		Thread thread = new Thread(server);
		ServerView view = new ServerView(server);
		
		thread.start();
		view.setVisible(true);
	}
}
