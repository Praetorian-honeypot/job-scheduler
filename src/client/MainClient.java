package client;

import java.net.InetSocketAddress;

public class MainClient {
	public static void main(String args[]) {
		String address = "localhost";
		int port = 8902;
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}
		
		InetSocketAddress clientAddress = new InetSocketAddress(address, port);
		Client client = new Client(clientAddress);
		
		Thread thread = new Thread(client);
		ClientView view = new ClientView(client);
		
		thread.start();
		view.setVisible(true);
	}
}
