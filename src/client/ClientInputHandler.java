package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientInputHandler extends Thread {
	private Client client;
	private ServerSocket socket;
	Thread runner;
	
	public ClientInputHandler(Client client) {
		this.client = client;
	}
	
	public void start() {
		try {
			socket = new ServerSocket(client.getAddress().getPort());
			client.log("Client is listening on port: " + client.getAddress().getPort());
		} catch (IOException exception) {
			client.log( Level.SEVERE, exception.toString(), exception );
		}
		
		this.runner = new Thread(this);
		this.runner.start();
	}
	
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			byte[] readBytes = readBytes();
			
			if (readBytes != null) {
				String result = new String(readBytes);
				handleInput(result);
			}
		}
	}
	
	public byte[] readBytes() {
		InputStream in;
		byte[] data = null;
		try {
			Socket serverSocket = socket.accept();
			in = serverSocket.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			
			int len = dis.readInt();
		    data = new byte[len];
		    if (len > 0) {
		        dis.readFully(data);
		    }
		} catch (IOException exception) {
			client.log( Level.SEVERE, exception.toString(), exception );
		}
		
	    return data;
	}
	
	public void handleInput (String result) {
		try {
			JSONObject json = new JSONObject(result);
			String type = json.getString("type");
			
			switch (type) {
				case "report":
					client.sendReport();
					break;				
			}
		} catch (JSONException exception) {
			client.log( Level.SEVERE, exception.toString(), exception );
		}
	}
}
