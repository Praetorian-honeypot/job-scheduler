package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientInputHandler implements Runnable {
	private Client client;
	private ServerSocket socket;
	private Thread runner = null;
	private volatile boolean running = true;
	private volatile boolean suspended = false;
	private InputStream in;
	private DataInputStream dis;
	
	public ClientInputHandler(Client client) {
		this.client = client;
	}
	
	public void start() {
		client.log("Client is listening on: " + client.getServerSocket().getLocalSocketAddress());
		this.running = true;
		this.runner = new Thread(this);
		this.runner.start();
	}
	
	public void suspend() {
		suspended = true;
	}
	
	public synchronized void resume() {
		suspended = false;
	}
	
	public boolean isSuspended() {
		return suspended;
	}
	
	public void terminate() {
		running = false;
	}
	
	public boolean isTerminated() {
		return running == false;
	}
	
	public void run() {
		try {
			in = client.getServerSocket().getInputStream();
			dis = new DataInputStream(in);
			
			while (running) {
				int size = dis.readInt();
				byte[] readBytes = readBytes(size);
				
				if (readBytes != null) {
					String result = new String(readBytes);
					handleInput(result);
				}
				
				synchronized(this) {
	               while(suspended) {
	                  wait();
	               }
	            }
			}
		} catch (IOException | InterruptedException exception) {
			client.log(Level.SEVERE, exception.toString(), exception);
		} finally {
			try {
				dis.close();
				in.close();
				socket.close();
			} catch (IOException exception) {
				client.log(Level.SEVERE, exception.toString(), exception);
			}
		}
	}
	
	public byte[] readBytes(int size) {
		byte[] data = new byte[size];
		try {
	        dis.readFully(data);
	        client.log("Received " + size + " bytes.");
		} catch (IOException exception) {
			client.log( Level.SEVERE, exception.toString(), exception );
		}
		
	    return data;
	}
	
	public void handleInput(String result) {
		try {
			JSONObject json = new JSONObject(result);
			String type = json.getString("type");
			
			switch (type) {
				case "report":
					client.log("Server requests report");
					client.sendReport();
					break;
				case "spec":
					client.log("Server requests client hardware specifications");
					client.sendHardwareSpec();
				case "runJob":
					client.log("Server requests to run a job");
					client.runJob();
					break;
			}
		} catch (JSONException exception) {
			client.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public ServerSocket getSocket() {
		return socket;
	}

	public void setSocket(ServerSocket socket) {
		this.socket = socket;
	}
}
