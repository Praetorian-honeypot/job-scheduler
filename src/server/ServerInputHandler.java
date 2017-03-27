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
	private volatile boolean running = true;
	
	public ServerInputHandler(Server server) {
		this.server = server;
	}
	
	public void start() {
		try {
			socket = new ServerSocket(server.getAddress().getPort());
			server.log("Server is listening...");
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
		this.running = true;
		this.runner = new Thread(this);
		this.runner.start();
	}
	
	public void terminate() {
		running = false;
	}
	
	public void run() {
		while (running) {
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
					server.addClient(client);
					break;
				case "disconnect":
					server.log("DISCONNECTING CLIENT");
					server.removeClient(client);
					break;
				case "report":
					server.log("Client reporting not implemented...");
					break;
				default:
					server.log(Level.SEVERE, "ERROR: server doesn't recognize this input type: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
}
