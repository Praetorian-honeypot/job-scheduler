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

import server.ClientReport;
import server.ConnectedClient;
import server.Server;

@Path("/reportservice")
public class reportService{
	@Context
    Configuration config;
	
	@GET
	@Produces("application/json")
	public Response getReports() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		ArrayList<ConnectedClient> clients = server.getClients();
		int j=0;
		for(ConnectedClient c: clients) {
			addReports(c, j, jsonObject);
		}
		jsonObject.put("test", "test");
		String result = "@Produces(\"application/json\") Output: \n\nReportService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 }
	
	@Path("{client}")
	@GET
	@Produces("application/json")
	public Response getReport(@PathParam("client") String client) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		String[] split = client.split("-");
		InetSocketAddress clientaddr = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		int j=0;
		addReports(server.getClient(clientaddr), j, jsonObject);
		
		String result = "@Produces(\"application/json\") Output: \n\nReportService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 } 
	
	private void addReports(ConnectedClient c, int j, JSONObject jsonObject) {
		ArrayList<ClientReport> reports = c.getReports();
		
		for (int i=0; i<reports.size(); i++) {
			ArrayList<Object> data = new ArrayList<Object>();
			data.add(reports.get(i).getClientAddress());
			data.add(reports.get(i).getCpuLoad());
			System.out.println(reports.get(i).getCpuLoad());
			System.out.println(reports.get(i).getCpuTemp());
			data.add(reports.get(i).getMemAvailable());
			data.add(reports.get(i).getCpuTemp());
			data.add(reports.get(i).getCreateDate());
			try {
				jsonObject.put(Integer.toString(j), data);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			j++;
		}
		
	}  

}
