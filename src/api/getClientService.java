package api;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
			jsonObject.put(Integer.toString(i), clients.get(i).getClientAddress());
		}
		String result = "@Produces(\"application/json\") Output: \n\nRemove Client Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 }

}