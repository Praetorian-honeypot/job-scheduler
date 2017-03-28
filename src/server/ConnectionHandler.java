package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionHandler implements Runnable {

	private Socket client;
	private volatile boolean running = true;
	private InputStream in;
	private DataInputStream dis;
	private Server server;

	public ConnectionHandler(Socket client, Server server) {
		this.client = client;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			in = client.getInputStream();
			dis = new DataInputStream(in);
			
			while (running) {
				byte[] readBytes = readBytes();
				
				if (readBytes != null) {
					String result = new String(readBytes);
					handleInput(result);
				}
			}
		} catch (IOException exception) {
			server.log(Level.SEVERE, exception.toString(), exception);
		} finally {
			try {
				dis.close();
				in.close();
			} catch (IOException exception) {
				server.log(Level.SEVERE, exception.toString(), exception);
			} 
		}
	}
	
	public byte[] readBytes() {
		byte[] data = null;
		try {
			if (dis.available() > 0) {
				int len = dis.readInt();
			    data = new byte[len];
			    if (len > 0) {
			        dis.readFully(data);
			        server.log("Received " + len + " bytes.");
			    }
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
			
			String clientAddress = json.getString("address").trim();
			if (clientAddress.equals("localhost/127.0.0.1"))
				clientAddress = "localhost";
			int clientPort = Integer.parseInt(json.getString("port"));
			InetSocketAddress client = new InetSocketAddress(clientAddress, clientPort);
			
			switch (type) {
				case "connect":
					server.addClient(client);
					break;
				case "disconnect":
					server.removeClient(client);
					break;
				case "report":
					double cpu = Double.parseDouble(json.getString("cpu"));
					server.log("Client on " + clientAddress + " reported a CPU usage of: " + cpu);
					break;
				default:
					server.log(Level.SEVERE, "ERROR: server doesn't recognize this input type: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

}
