package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.json.JSONException;

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
	private static final String BROKER = "asa";
	private Consumer reportConsumer;
	
	public ServerInputHandler(Server server) {
		this.server = server;
	}
	
	public void start() {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(BROKER);
		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare("server",false,false,false,null);
			channel.exchangeDeclare("reports", "direct");
			
			channel.queueBind("server","reports","report");
		    reportConsumer = new DefaultConsumer(channel) {
		    	@Override
		    	public void handleDelivery(String consumerTag, Envelope envelope,
		    			AMQP.BasicProperties properties, byte[] body) throws IOException {
		    		//String message = new String(body, "UTF-8");
		    		//System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
		    		
		    		if(envelope.getRoutingKey().equals("report")){
		    			try {
		    				String message = new String(body, "UTF-8");
							server.handleReport(message);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}
		    	}
		    };
		    server.log("Connected to MQ broker.");
		    
		    
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					handler.channel.basicConsume("server",true,handler.reportConsumer);
				}
			} catch (IOException exception) {
				handler.server.log( Level.SEVERE, exception.toString(), exception );
			}
			
		}
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
