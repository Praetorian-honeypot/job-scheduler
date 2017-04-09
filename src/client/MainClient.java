package client;

import java.net.InetSocketAddress;

public class MainClient {
	public static void main(String args[]) {
		String address = "localhost";
		int port = 8901;
		int serverPort = 8900;
		if (args.length == 2) {
			port = Integer.parseInt(args[0]);
			serverPort = Integer.parseInt(args[1]);
		}
		
		InetSocketAddress clientAddress = new InetSocketAddress(address, port);
		Client client = new Client(clientAddress, serverPort);
		
		Thread thread = new Thread(client);
		ClientView view = new ClientView(client);
		
		thread.start();
		view.setVisible(true);
	}
}
