package api;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import server.ConnectedClient;
import server.Server;

@Path("/getclientservice")
public class getClientService{
	@Context
    Configuration config;
	@GET
	@Produces("application/json")
	public Response getClients() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		ArrayList<ConnectedClient> clients = server.getClients();
		for(int i=0; i<clients.size(); i++) {
			getClient(clients.get(i), i, jsonObject);
		}
		String result = "@Produces(\"application/json\") Output: \n\nGet Client Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	}
	
	@Path("{address}")
	@GET
	@Produces("application/json")
	public Response getClient(@PathParam("address") String address) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		String[] split = address.split("-");
		InetSocketAddress clientaddr = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		if(!server.clientExists(clientaddr)) {
			jsonObject.put("status", "Client doesn't exist");
		} else {
		getClient(server.getClient(clientaddr), 0, jsonObject);
		}
		String result = "@Produces(\"application/json\") Output: \n\nGet Client Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	}
	
	private void getClient(ConnectedClient client, int i, JSONObject jsonObject) {
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(client.getClientAddress());
		data.add(client.getCpuName());
		data.add(client.getCpuCores());
		data.add(client.getTotalMemory());
		data.add(client.getOperatingSystem());
		data.add(client.getHostname());
		System.out.println(client.getClientAddress());
		System.out.println(client.getCpuName());
		System.out.println(client.getCpuCores());
		System.out.println(client.getTotalMemory());
		System.out.println(client.getOperatingSystem());
		System.out.println(client.getHostname());
		try {
			jsonObject.put(Integer.toString(i), data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
