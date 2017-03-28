package server;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Server extends Observable implements Runnable {
	private transient static final Logger logger = Logger.getLogger( Server.class.getName() );
	private static List<String> serverLogger = new ArrayList<String>();
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private InetSocketAddress address;
	private ServerInputHandler serverInputHandler;
	private ArrayList<ClientConnector> clients = new ArrayList<ClientConnector>();
	
	public Server(InetSocketAddress address) {
		this.address = address;
		logger.setLevel(Level.ALL);
		logger.addHandler(new Handler() {
			@Override
			public void close() throws SecurityException {
			}

			@Override
			public void flush() {
			}

			@Override
			public void publish(LogRecord logRecord) {
				//StackTraceElement e = Thread.currentThread().getStackTrace()[2];
				Date date = new Date(logRecord.getMillis());
				serverLogger.add(sdf.format(date) + ": "
						+ logRecord.getSourceClassName() + ": "
						+ logRecord.getSourceMethodName() + ": "
						+ "\"" + logRecord.getMessage() + "\""
						+ "\n");
				Server.this.update();
			}
        	
        });
	}
	
	@Override
	public void run() {
		update();
		logger.log(Level.FINE, "Server is initiated");
		
		serverInputHandler = new ServerInputHandler(this);
		serverInputHandler.start();
	}
	
	public void update() {
		this.setChanged();
		this.notifyObservers();
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
	
	public void log(Level lvl, String msg, Throwable thrown) {
		logger.log(lvl, msg, thrown);
	}
	
	public void log(String text) {
		logger.log(Level.FINE, text);
	}
	
	public String getLoggerText() {
		String messageText = "";
        
        for (int i = serverLogger.size() - 1; i >= 0; i--) {
        	messageText += serverLogger.get(i);
		}
		return messageText;
	}

	public void addClient(InetSocketAddress client) {
		if (!clientExists(client)) {
			log("Adding client: " + client.getAddress() + " on port: " + client.getPort());
			ClientConnector clientConnector = new ClientConnector(this, client);
			clients.add(clientConnector);
			update();
		}
	}
	
	public synchronized void removeClient (InetSocketAddress client) {
		if (clientExists(client)) {
			log("Removing client: " + client.getHostName() + " on port: " + client.getPort());
			clients.remove(getClient(client));
			update();
		}		
	}
	
	public synchronized boolean clientExists (InetSocketAddress client) {
		return (getClient(client) != null);
	}
	
	public synchronized ClientConnector getClient (InetSocketAddress searchClient) {
		ClientConnector findClient = null;
		
		for (ClientConnector client : clients) {
			if (searchClient.equals(client.getClientAddress()))
				findClient = client;
		}
		
		return findClient;
	}
	
	public ArrayList<ClientConnector> getClients() {
		return clients;
	}

	public synchronized void requestCPULoad() {
		if (clients.isEmpty()) {
			log("Client list is empty");
			return;
		}
		
		for (ClientConnector client : clients) {
			client.requestReport();
		}
	}

	public String getClientsText() {
		String messageText = "";
		
		if (clients.isEmpty())
			messageText = "No clients";
        
		int i = 1;
        for (ClientConnector client : clients) {
        	messageText += i + ": " + client.getClientAddress().getHostName() + " (port " + client.getClientAddress().getPort() + ")\n";
        	i++;
		}
		return messageText;
	}
	
}