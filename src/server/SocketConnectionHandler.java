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
			
			switch (type) {
				case "connect":
					String cpuName = json.getString("cpuName");
					int cpuCores = Integer.parseInt(json.getString("cpuCores"));
					int memoryAmount = Integer.parseInt(json.getString("memoryAmount"));
					String operatingSystem = json.getString("operatingSystem");
					String displayName = json.getString("displayName");
					int performance = Integer.parseInt(json.getString("performance"));
					
					server.addClient(client, cpuName, cpuCores, operatingSystem, memoryAmount, displayName, performance);
					break;
				case "disconnect":
					server.removeClient(new InetSocketAddress(client.getInetAddress(), client.getPort()));
					break;
				default:
					server.log(Level.SEVERE, "ERROR: server doesn't recognize this input type: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

}
