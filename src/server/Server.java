package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import api.RestAPI;
import database.SQLite;
import jobs.JobDispatcher;
import jobs.JobDispatcherRemote;  

public class Server extends Observable implements Runnable {
	private transient static final Logger logger = Logger.getLogger( Server.class.getName() );
	private static List<String> serverLogger = new ArrayList<String>();
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private InetSocketAddress address;
	private ServerInputHandler serverInputHandler;
	private ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();
	private SQLite database;
	private JobDispatcher jobDispatcherRemote;

	public static final String API_URI = "http://localhost:8080/api/";
	
	public Server(InetSocketAddress address) {
		this.address = address;
		initLogger();		
		initRMI();
	}
	
	private void initLogger() {
		logger.setLevel(Level.ALL);
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.FINER);
		logger.addHandler(consoleHandler);
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

	private void initRMI() {
		System.setProperty( "java.rmi.server.hostname", getFixedAddress(address) ) ;
		log("Initiated RMI on: "+getFixedAddress(address));
		try {
			jobDispatcherRemote = new JobDispatcherRemote(this);
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind("jobDispatcher", jobDispatcherRemote);
		} catch (RemoteException e) {
			log(Level.SEVERE, e.toString(), e);
		}
	}

	@Override
	public void run() {
		update();
		
		serverInputHandler = new ServerInputHandler(this);
		serverInputHandler.start();
		this.setDatabase(new SQLite(this));
		new RestAPI(this, API_URI);
		logger.log(Level.FINE, "Server is initiated");
		
		Timer timer = new Timer();
		timer.schedule(new ReportSender(this), 0, 60000);
		
		try {
			jobDispatcherRemote.scheduleJobs();
		} catch (RemoteException e) {
			log(Level.SEVERE, e.toString(), e);
		}
		
		Timer timer2 = new Timer();
		timer2.schedule(new JobSender(this), 0, 1000);
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
	
	public void addClient(Socket client, String cpuName, int cpuCores, String os, int memory, String displayName, int performance) {
		InetSocketAddress clientAddress = new InetSocketAddress(getFixedAddress(client.getInetAddress()), client.getPort());
		if (!clientExists(clientAddress)) {
			log("Adding client: " + client.getInetAddress() + " on port: " + client.getPort());
			int time = (int) (new Date().getTime() / 1000);
			int clientId = database.findClient(clientAddress);
			if (clientId == 0) 
				clientId = database.addClient(clientAddress, cpuName, cpuCores, os, memory, displayName, performance, time);
			ConnectedClient connectedClient = new ConnectedClient(this, client, clientAddress, cpuName, cpuCores, os, memory, displayName, performance, time, clientId);
			
			clients.add(connectedClient);
			update();
		}
	}
	
	public String getFixedAddress(InetSocketAddress fixAddress) {
		String s = fixAddress.getAddress().toString();
		if (s.indexOf("/") != -1)
			s = s.substring(s.indexOf("/")+1);
		
		if (s.equals("127.0.0.1"))
			s = "localhost";
		return s;
	}
	
	public String getFixedAddress(InetAddress fixAddress) {
		String s = fixAddress.toString();
		if (s.indexOf("/") != -1)
			s = s.substring(s.indexOf("/")+1);
		if (s.equals("127.0.0.1"))
			s = "localhost";
		return s;
	}
	
	public synchronized void removeClient (InetSocketAddress client) {
		if (clientExists(client)) {
			log("Removing client: " + client.getHostName() + " on port: " + client.getPort());
			serverInputHandler.disconnectClient(client);
			ConnectedClient removeClient = getClient(client);
			clients.remove(removeClient);
			update();
		}		
	}
	
	public synchronized boolean clientExists (InetSocketAddress client) {
		return (getClient(client) != null);
	}
	
	public synchronized ConnectedClient getClient (InetSocketAddress searchClient) {
		ConnectedClient findClient = null;
		
		for (ConnectedClient client : clients) {
			String searchClientAddress = getFixedAddress(searchClient);
			String clientAddress = getFixedAddress(client.getClientAddress());
			
			if (searchClientAddress.equals(clientAddress) && searchClient.getPort() == client.getClientAddress().getPort())
				findClient = client;
		}
		
		return findClient;
	}
	
	public ConnectedClient getClientById(int clientId) {
		ConnectedClient findClient = null;
		
		for (ConnectedClient client : clients) {
			if (client.getId() == clientId)
				findClient = client;
		}
		
		return findClient;
	}
	
	public ArrayList<ConnectedClient> getClients() {
		return clients;
	}

	public synchronized void requestReport() {
		for (ConnectedClient client : clients) {
			client.requestReport();
		}
	}

	public String getClientsText() {
		String messageText = "";
		
		if (clients.isEmpty())
			messageText = "No clients";
        
		int i = 1;
        for (ConnectedClient client : clients) {
        	messageText += "(" + i + ") "
    				+ client.getClientAddress().getHostName() 
    				+ ":" + client.getClientAddress().getPort();
        	
        	ClientReport report = client.getLatestReport();
        	final String DEGREE  = "\u00b0";
        	if (report != null)
        		messageText += " (CPU:" + Math.round(report.getCpuLoad() * 100) + "%, "
        				+ " MEM:" + Math.round(report.getMemAvailable() / Math.pow(10,6)) + "MB, "
        				+ " TEMP:" + Math.round(report.getCpuTemp()) + DEGREE +"C)";
        	
        	messageText += "\n";
        	i++;
		}
		return messageText;
	}

	public SQLite getDatabase() {
		return database;
	}

	public void setDatabase(SQLite database) {
		this.database = database;
	}
	
	public JobDispatcher getJobDispatcherRemote() {
		return jobDispatcherRemote;
	}

	public void setJobDispatcherRemote(JobDispatcher jobDispatcherRemote) {
		this.jobDispatcherRemote = jobDispatcherRemote;
	}

	public void requestRunJobs() {
		try {
			if (jobDispatcherRemote.hasJobs()) {
				for (ConnectedClient client : clients) {
					if (client.isAvailable()) {
						client.requestRunJob();
					}
				}
			}
		} catch (RemoteException e) {
			log(Level.SEVERE, e.toString(), e);
		}
	}
}
