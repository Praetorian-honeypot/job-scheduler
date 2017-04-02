package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ServerInputHandler implements Runnable {
	private Server server;
	private ServerSocket socket;
	Thread runner;
	private volatile boolean running = true;
	private Channel channel;
	private static final String BROKER = readFile("server.txt");
	private Consumer reportConsumer;
	
	public ServerInputHandler(Server server) {
		this.server = server;
	}
	
	private static String readFile(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(encoded);
	}

	public void start() {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(BROKER);
		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare("serverIn", false, false, false, null);
			channel.exchangeDeclare("server", "direct");
			
			channel.queueBind("serverIn","server","report");
			channel.queueBind("serverIn","server","spec");
		    reportConsumer = new DefaultConsumer(channel) {
		    	@Override
		    	public void handleDelivery(String consumerTag, Envelope envelope,
		    			AMQP.BasicProperties properties, byte[] body) throws IOException {
		    		
		    		@SuppressWarnings("unused")
					String routingKey = envelope.getRoutingKey();
	    			String message = new String(body, "UTF-8");
		    		handleMessageQueueInput(message);
		    	}
		    };
		    server.log("Connected to MQ broker.");
		} catch (IOException | TimeoutException exception) {
			server.log(Level.SEVERE, exception.toString(), exception);
		}
		
		try {
			socket = new ServerSocket(server.getAddress().getPort());
			server.log("Server is listening on: " + server.getAddress().getAddress() + ":" + server.getAddress().getPort());
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
		this.running = true;
		this.runner = new Thread(this);
		this.runner.start();
		
		Runnable consume = new ReportHandler(this);
		Thread con = new Thread(consume);
		con.start();
	}
	
	public void terminate() {
		running = false;
	}
	
	private class ReportHandler implements Runnable {
		private final ServerInputHandler handler;
		public ReportHandler(ServerInputHandler handler){
			this.handler = handler;
		}
		
		@Override
		public void run() {
			try {
				while(handler.running){
					handler.channel.basicConsume("serverIn",true,handler.reportConsumer);
				}
			} catch (IOException exception) {
				handler.server.log( Level.SEVERE, exception.toString(), exception );
			}
			
		}
	}
	
	public void handleMessageQueueInput (String result) {
		try {
			JSONObject json = new JSONObject(result);
			String type = json.getString("type");
			
			String clientAddress = json.getString("address").trim();
			if (clientAddress.equals("localhost/127.0.0.1"))
				clientAddress = "localhost";
			int clientPort = Integer.parseInt(json.getString("port"));
			InetSocketAddress client = new InetSocketAddress(clientAddress, clientPort);
			
			ConnectedClient connectedClient = server.getClient(client);
			switch (type) {
				case "report":
					double cpuLoad = Double.parseDouble(json.getString("cpuLoad"));
					double memAvailable = Double.parseDouble(json.getString("memAvailable"));
					double cpuTemp = Double.parseDouble(json.getString("cpuTemp"));
					ClientReport report = new ClientReport(connectedClient.getClientAddress(), cpuLoad, memAvailable, cpuTemp);
					connectedClient.addReport(report);
					server.log("Received report from client on " + clientAddress);
					break;
				case "spec":
					String cpuName = json.getString("cpuName");
					int cpuCores = Integer.parseInt(json.getString("cpuCores"));
					String operatingSystem = json.getString("operatingSystem");
					int totalMemory = Integer.parseInt(json.getString("totalMemory"));
					String hostname = json.getString("hostname");
					int performance = json.getInt("performance");
					
					//TODO: store in DB or something.
					server.log("Received hardware specifications from client on " + clientAddress);
					break;
				default:
					server.log(Level.SEVERE, "ERROR: ServerInputHandler doesn't recognizes this type of input for the message queue: " + type, null);
			}
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public void run() {
		try {
			while (running) {
				Socket clientSocket = socket.accept();
				Runnable socketConnectionHandler = new SocketConnectionHandler(clientSocket, server);
				new Thread(socketConnectionHandler).start();
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
