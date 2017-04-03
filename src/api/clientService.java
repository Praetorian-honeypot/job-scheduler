package api;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import server.ConnectedClient;
import server.Server;

@Path("/clientservice")
public class clientService{
	private transient static final Logger logger = Logger.getLogger( clientService.class.getName() );
	@Context
    Configuration config;

	@GET
	@Produces("application/json")
	public Response getClient(	@QueryParam("address") String addr, @QueryParam("port") Integer port,
								@QueryParam("cores") Integer cores, @QueryParam("memory") Integer memory) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		port = (port == null) ? 0 : port;
		addr = (addr == null) ? "" : addr;
		InetSocketAddress clientaddr = new InetSocketAddress(addr, port);
		System.out.println(clientaddr);
		ArrayList<ConnectedClient> clients = server.getClients();
		if(!server.clientExists(clientaddr)) {
			for(int i=0; i<clients.size(); i++) {
				getClient(clients.get(i), i, jsonObject, cores, memory);
			}
		} else {
			getClient(server.getClient(clientaddr), 0, jsonObject, cores, memory);
		}
		String result = jsonObject.toString(); 
		return Response.status(200).entity(result).build();
	}
	

	@Path("/addclient")
	@POST
	@Produces("application/json")
	public Response addClient(@QueryParam("address") String addr, @QueryParam("port") Integer port) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		InetSocketAddress clientaddr = new InetSocketAddress(addr, port);
		if(server.clientExists(clientaddr)) {
			jsonObject.put("result", "Client with address " + clientaddr + " already exists");
		} else { 
			server.addClient(clientaddr);
			if(server.clientExists(clientaddr)) {
				jsonObject.put("result", "addition succesful");
			} else { 
				jsonObject.put("result", "addition failed");
			}
		}
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	}

	@Path("/removeclient")
	@DELETE
	@Produces("application/json")
	public Response removeClient(@QueryParam("address") String addr, @QueryParam("port") Integer port) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		InetSocketAddress clientaddr = new InetSocketAddress(addr, port);
		if(!server.clientExists(clientaddr)) {
			jsonObject.put("result", "Client with address " + clientaddr + " doesn't exist");
		} else { 
			server.removeClient(clientaddr);
			if(!server.clientExists(clientaddr)) {
				jsonObject.put("result", "deletion succesful");
			} else { 
				jsonObject.put("result", "deletion failed");
			}
		}
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	}

	
	private void getClient(ConnectedClient client, int i, JSONObject jsonObject, Integer cores, Integer memory) {
		if(cores != null) 
			if(client.getCpuCores() < cores)
				return;
		if(memory != null)
			if(client.getTotalMemory() < memory)
				return;
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(client.getId());
		data.add(client.getClientAddress());
		data.add(client.getCpuName());
		data.add(client.getCpuCores());
		data.add(client.getTotalMemory());
		data.add(client.getOperatingSystem());
		data.add(client.getClientAddress().getHostName());
		data.add(client.getClientAddress().getPort());
		data.add(client.getPerformance());
		data.add(client.getDisplayName());
		data.add(client.getTime());
		
		try {
			jsonObject.put(Integer.toString(i), data);
		} catch (JSONException e) {
			logger.log( Level.SEVERE, e.toString(), e );
		}
	}

}
