package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientConnector {
	private Server server;
	private InetSocketAddress clientAddress;
	private Socket clientSocket;
	
	public ClientConnector(Server server, InetSocketAddress clientAddress) {
		this.server = server;
		this.setClientAddress(clientAddress);
		
		try {
			InetAddress address = InetAddress.getByName(clientAddress.getHostName());
			clientSocket = new Socket(address, clientAddress.getPort());
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
	}
	
	public void send(JSONObject json) {
		send(json.toString().getBytes());
	}
	
	public void send(byte[] sendData) {
		send(sendData, 0, sendData.length);
	}
	
	public void send(byte[] sendData, int start, int len) {
		try {
			if (len < 0)
				throw new IllegalArgumentException("Negative byte packet length not allowed.");
			if (start < 0 || start >= sendData.length)
				throw new IndexOutOfBoundsException("Byte start index out of bounds: " + start);
			
			OutputStream out = clientSocket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			
			dos.writeInt(len);
			
			if (len > 0) {
				dos.write(sendData, start, len);
			}
		} catch (IOException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public void requestReport() {
		JSONObject reportData = null;
		try {
			reportData = new JSONObject();
			reportData.put("type", "report");
		} catch (JSONException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		send(reportData);
	}

	public InetSocketAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetSocketAddress clientAddress) {
		this.clientAddress = clientAddress;
	}
}
