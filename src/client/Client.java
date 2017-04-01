package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class Client extends Observable implements Runnable {
	private transient static final Logger logger = Logger.getLogger( Client.class.getName() );
	protected InetSocketAddress address;
	protected InetSocketAddress serverAddress;
	private ClientInputHandler clientInputHandler = null;
	private Socket serverSocket = null;
	private static List<String> clientLogger = new ArrayList<String>();
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final String BROKER = "asa";
	private Channel channel;
	
	
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
		logger.log(Level.FINE, "Client is initiated");
		this.setChanged();
		this.notifyObservers();
		
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
			serverAddress = server;
			log("Connecting to server at address: "+server.getAddress());
			if (serverSocket == null)
				serverSocket = new Socket(server.getAddress(), server.getPort());
			
			ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost(BROKER);
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare("server", "direct");
			
			channel.queueDeclare("serverIn",false,false,false,null);
			
			log("Connected to MQ broker.");
			
			if (clientInputHandler.isSuspended())
				clientInputHandler.resume();
			
			logger.log(Level.FINE, "Client is connected to: " + server.getHostName());
			send(hardwareSpec(getCommand("connect")));
		} catch (IOException | TimeoutException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	public void disconnect() {
		if (!isConnected()) {
			log("Already disconnected from server!");
			return;
		}
		
		sendCommand("disconnect");
		clientInputHandler.suspend();
		serverSocket = null;
		log("Client succesfully disconnected from server.");
	}

	public boolean isConnected() {
		return serverSocket != null && !serverSocket.isClosed();
	}
	
	public boolean isActive() {
		return clientInputHandler != null && !clientInputHandler.isSuspended() && isConnected();
	}
	
	public boolean isListening() {
		return clientInputHandler != null && !clientInputHandler.isTerminated();
	}

	public void sendReport() {
		JSONObject reportData = getCommand("report");
		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hw = sysInfo.getHardware();
		try {
			double cpuLoad = hw.getProcessor().getSystemLoadAverage();
			if(cpuLoad == -1.0){
				//Windows doesn't report load averages, fallback.
				cpuLoad = hw.getProcessor().getSystemCpuLoad();
			}
			reportData.put("cpuLoad", cpuLoad);
			
			double memAvailable = hw.getMemory().getAvailable();
			reportData.put("memAvailable", memAvailable);
			
			double cpuTemp = hw.getSensors().getCpuTemperature();
			reportData.put("cpuTemp", cpuTemp);
			
			String message = reportData.toString();
			channel.basicPublish("server","report", null, message.getBytes());
			
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		} catch (IOException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public void sendHardwareSpec(){
		send(hardwareSpec(null));
	}
	
	public JSONObject hardwareSpec(JSONObject in){
		JSONObject specData;
		if(in != null){
			specData = in;
		} else {
			specData = getCommand("spec");
		}
		
		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hw = sysInfo.getHardware();
		try {
			String cpuName = hw.getProcessor().getName();
			int cpuCores = hw.getProcessor().getPhysicalProcessorCount();
			String operatingSystem = sysInfo.getOperatingSystem().getManufacturer() +
					" " + sysInfo.getOperatingSystem().getFamily() +
					" " + sysInfo.getOperatingSystem().getVersion();
			long totalMemory = hw.getMemory().getTotal() / (1024*1024);
			String hostname = sysInfo.getOperatingSystem().getNetworkParams().getHostName();
			
			specData.put("cpuName", cpuName);
			specData.put("cpuCores", cpuCores);
			specData.put("operatingSystem", operatingSystem);
			specData.put("totalMemory",totalMemory);
			specData.put("hostname", hostname);
			specData.put("performance", benchmark());
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		
		return specData;
	}
	
	public void sendCommand(String type) {
		send(getCommand(type));
	}
	
	public JSONObject getCommand(String type) {
		JSONObject command = null;
		try {
			command = new JSONObject();
			command.put("type", type);
			command.put("address", address.getAddress().toString());
			command.put("port", address.getPort());
		} catch (JSONException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
		
		return command;
	}
	
	public void send(JSONObject json) {
		send(json.toString().getBytes());
	}
	
	public void send(byte[] sendData) {
		send(sendData, 0, sendData.length);
	}
	
	public void send(byte[] sendData, int start, int len) {
		if (!isConnected()) {
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
				log("Writing " + len + " bytes to the server.");
			}
			dos.flush();
			out.flush();
		} catch (IOException exception) {
			logger.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}
	
	public int benchmark(){
		// Returns a very crude measure of the system's single-core performance by
		// calculating pi using the Leibniz formula to a high number of terms
		// and measuring execution time.
		
		// Higher is faster.
		// As an example, an Intel Core i5-4690 gets a result of roughly 1100.
		
		double start = System.nanoTime();
		@SuppressWarnings("unused")
		double pi = 0;
		for(int k = 0; k < 1e7; k++){
			pi += 4.0 * (k % 2 == 0 ? 1 : -1) / (2 * k + 1);
		}
		return (int)(1e11 / (System.nanoTime() - start));
	}
}
