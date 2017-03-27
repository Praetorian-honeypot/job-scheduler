package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetSocketAddress;
import java.net.Socket;
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

import org.json.JSONException;
import org.json.JSONObject;

public class Client extends Observable implements Runnable {
	private transient static final Logger logger = Logger.getLogger( Client.class.getName() );
	protected InetSocketAddress address;
	protected InetSocketAddress serverAddress;
	private ClientInputHandler clientInputHandler;
	private Socket serverSocket = null;
	private static List<String> clientLogger = new ArrayList<String>();
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	/**
	 * Constructs a new frame for the client.
	 * 
	 * @param frame the frame
	 */
	
	public Client(InetSocketAddress address) {
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
				clientLogger.add(sdf.format(date) + ": "
						+ logRecord.getSourceClassName() + ": "
						+ logRecord.getSourceMethodName() + ": "
						+ "\"" + logRecord.getMessage() + "\""
						+ "\n");
				Client.this.setChanged();
				Client.this.notifyObservers();
			}
        	
        });
	}
	
	@Override
	public void run() {
		this.setChanged();
		this.notifyObservers();
		
		logger.log(Level.FINE, "Client is initiated");
		
		clientInputHandler = new ClientInputHandler(this);
		clientInputHandler.start();
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
        
        for (int i = clientLogger.size() - 1; i >= 0; i--) {
        	messageText += clientLogger.get(i);
		}
		return messageText;
	}
	
	public void connect(InetSocketAddress server) {
		try {
			serverSocket = new Socket(server.getAddress(), server.getPort());
			logger.log(Level.FINE, "Client is connected to: " + server.getHostName());
			serverAddress = server;
			sendConnect();
		} catch (IOException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	public void disconnect() throws InterruptedException {
		if (serverSocket.isClosed()) {
			log("Already disconnected from server!");
			return;
		}
		
		sendDisconnect();
		Thread.sleep(50);
		try {
			serverSocket.close();
		} catch (IOException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		clientInputHandler.terminate();
		log("Client succesfully disconnected from server.");
	}

	public void sendReport() {
		JSONObject reportData = null;
		try {
			reportData = new JSONObject();
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			reportData.put("type", "report");
			reportData.put("cpu", os.getSystemLoadAverage());
			logger.log(Level.FINE, String.valueOf(os.getSystemLoadAverage()));
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		send(reportData);
	}
	
	public void sendConnect() {
		JSONObject connectData = null;
		try {
			connectData = new JSONObject();
			connectData.put("type", "connect");
			connectData.put("address", address.getAddress().toString());
			connectData.put("port", address.getPort());
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		send(connectData);
	}
	
	public void sendDisconnect() {
		JSONObject disconnectData = null;
		try {
			disconnectData = new JSONObject();
			disconnectData.put("type", "disconnect");
			disconnectData.put("address", address.getAddress().toString());
			disconnectData.put("port", address.getPort());
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		send(disconnectData);
	}
	
	public void send(JSONObject json) {
		send(json.toString().getBytes());
	}
	
	public void send(byte[] sendData) {
		send(sendData, 0, sendData.length);
	}
	
	public void send(byte[] sendData, int start, int len) {
		if (serverSocket == null) {
			logger.log(Level.SEVERE, "No connection has been established with the server");
			return;
		}
		
		try {
			if (len < 0)
				throw new IllegalArgumentException("Negative byte packet length not allowed.");
			if (start < 0 || start >= sendData.length)
				throw new IndexOutOfBoundsException("Byte start index out of bounds: " + start);
			
			OutputStream out = serverSocket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			
			dos.writeInt(len);
			
			if (len > 0) {
				dos.write(sendData, start, len);
			}
		} catch (IOException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
}
