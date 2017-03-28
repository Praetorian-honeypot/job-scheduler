package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class ServerInputHandler implements Runnable {
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
			server.log("Server is listening on: " + server.getAddress().getAddress() + ":" + server.getAddress().getPort());
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
		try {
			while (running) {
				Socket clientSocket = socket.accept();
				Runnable connectionHandler = new ConnectionHandler(clientSocket, server);
				new Thread(connectionHandler).start();
			}
		} catch (IOException exception) {
			server.log(Level.SEVERE, exception.toString(), exception);
		} finally {
			try {
				socket.close();
			} catch (IOException exception) {
				server.log(Level.SEVERE, exception.toString(), exception);
			} 
		}
	}
}
