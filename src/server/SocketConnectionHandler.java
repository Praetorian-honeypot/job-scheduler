package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketConnectionHandler implements Runnable {

	private Socket client;
	private volatile boolean running = true;
	private InputStream in;
	private DataInputStream dis;
	private Server server;

	public SocketConnectionHandler(Socket client, Server server) {
		this.client = client;
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			in = client.getInputStream();
			dis = new DataInputStream(in);
			
			while (running) {
				int size = dis.readInt();
				byte[] readBytes = readBytes(size);
				
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
	
	public byte[] readBytes(int size) {
		byte[] data = new byte[size];
		try {
	        dis.readFully(data);
			server.log("Received " + size + " bytes.");
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
					String cpuName = json.getString("cpuName");
					int cpuCores = Integer.parseInt(json.getString("cpuCores"));
					int memory = Integer.parseInt(json.getString("totalMemory"));
					String os = json.getString("operatingSystem");
					String hostname = json.getString("hostname");
					int performance = Integer.parseInt(json.getString("performance"));
					
					server.addClient(client, cpuName, cpuCores, os, memory, hostname, performance);
					break;
				case "disconnect":
					server.removeClient(client);
					break;
				default:
					server.log(Level.SEVERE, "ERROR: server doesn't recognize this input type: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

}
