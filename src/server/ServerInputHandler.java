package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerInputHandler extends Thread {
	private Server server;
	private ServerSocket socket;
	Thread runner;
	
	public ServerInputHandler(Server server) {
		this.server = server;
	}
	
	public void start() {
		try {
			socket = new ServerSocket(server.getAddress().getPort());
			server.log("Server is listening");
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
		this.runner = new Thread(this);
		this.runner.start();
	}
	
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			byte[] readBytes = readBytes();
			
			if (readBytes != null) {
				server.log("Received bytes: " + readBytes.length);
				String result = new String(readBytes);
				handleInput(result);
			}
		}
	}
	
	public byte[] readBytes() {
		InputStream in;
		byte[] data = null;
		try {
			Socket clientSocket = socket.accept();
			in = clientSocket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			
			int len = dis.readInt();
		    data = new byte[len];
		    if (len > 0) {
		        dis.readFully(data);
		    }
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
	    return data;
	}
	
	public void handleInput (String result) {
		try {
			JSONObject json = new JSONObject(result);
			String type = json.getString("type");
			String clientAddress = json.getString("address").replace("localhost/", "");
			int clientPort = Integer.parseInt(json.getString("port"));
			InetSocketAddress client = new InetSocketAddress(clientAddress, clientPort);
			switch (type) {
				case "connect":
					handleConnectClient(client);
					break;
				case "report":
					handleClientReport(client, json);
					break;				
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	private void handleConnectClient(InetSocketAddress client) {
		server.addClient(client);
	}

	private void handleClientReport(InetSocketAddress client, JSONObject json) {
		server.log("Client reporting not implemented...");
	}
}
