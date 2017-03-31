package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

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
					server.addClient(client);
					break;
				case "disconnect":
					server.removeClient(client);
					break;
				case "report":
					ConnectedClient connectedClient = server.getClient(client);
					double cpuLoad = Double.parseDouble(json.getString("cpuLoad"));
					double memAvailable = Double.parseDouble(json.getString("memAvailable"));
					double cpuTemp = Double.parseDouble(json.getString("cpuTemp"));
					ClientReport report = new ClientReport(connectedClient.getClientAddress(), cpuLoad, memAvailable, cpuTemp);
					connectedClient.addReport(report);
					server.log("Received report from client on " + clientAddress);
					break;
				default:
					server.log(Level.SEVERE, "ERROR: server doesn't recognize this input type: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

}
