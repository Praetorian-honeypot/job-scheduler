package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientInputHandler implements Runnable {
	private Client client;
	private ServerSocket socket;
	private Thread runner = null;
	private volatile boolean running = true;
	private volatile boolean suspended = false;
	private Socket serverSocket;
	private InputStream in;
	private DataInputStream dis;
	
	public ClientInputHandler(Client client) {
		this.client = client;
	}
	
	public void start() {
		try {
			socket = new ServerSocket(client.getAddress().getPort());
			client.log("Client is listening on: " + client.getAddress().getAddress() + ":" + client.getAddress().getPort());
			this.running = true;
			this.runner = new Thread(this);
			this.runner.start();
		} catch (IOException exception) {
			running = false;
			if (exception instanceof BindException)
				client.log("This address is already in use, aborting...");
			else
				client.log( Level.SEVERE, exception.toString(), exception );
		}
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
			serverSocket = socket.accept();
			in = serverSocket.getInputStream();
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
					client.sendReport();
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
