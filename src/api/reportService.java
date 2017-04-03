package api;


import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	private transient static final Logger logger = Logger.getLogger( reportService.class.getName() );
	@Context
    Configuration config;
	
	@GET
	@Produces("application/json")
	public Response getReport(	@QueryParam("address") String addr, @QueryParam("port") Integer port,
								@QueryParam("date1") String date1, @QueryParam("date2") String date2, 
								@QueryParam("load1") Integer load1, @QueryParam("load2") Integer load2) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		port = (port == null) ? 0 : port;
		addr = (addr == null) ? "" : addr;
		InetSocketAddress clientaddr = new InetSocketAddress(addr, port);
		int j=0;
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
		try {
			Date startDate = (date1 == null) ? null : formatter.parse(date1);
			Date endDate = (date2 == null) ? null : formatter.parse(date2);
			ArrayList<ConnectedClient> clients = server.getClients();
			if(!server.clientExists(clientaddr)) {
				for(ConnectedClient c: clients) {
					addReports(c, j, jsonObject, startDate, endDate, load1, load2);
				}
			} else {
				addReports(server.getClient(clientaddr), j, jsonObject, startDate, endDate, load1, load2);
			}
			
		} catch (ParseException e) {
			logger.log( Level.SEVERE, e.toString(), e );
		}

		
		String result = jsonObject.toString();//"@Produces(\"application/json\") Output: \n\nReportService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 } 
	
	private void addReports(ConnectedClient c, int j, JSONObject jsonObject, Date startDate, Date endDate, Integer load1, Integer load2) {
		ArrayList<ClientReport> reports = c.getReports();
		
		for (int i=0; i<reports.size(); i++) {
			if(startDate!= null && endDate!=null) 
				if (reports.get(i).getCreateDate().before(startDate) || reports.get(i).getCreateDate().after(endDate)) 
					continue;
			if(load1!= null && load2!= null) 
				if(reports.get(i).getCpuLoad() < load1 || reports.get(i).getCpuLoad() > load2) 
					continue;
			ArrayList<Object> data = new ArrayList<Object>();
			data.add(reports.get(i).getClientAddress());
			data.add(reports.get(i).getCpuLoad());
			data.add(reports.get(i).getMemAvailable());
			data.add(reports.get(i).getCpuTemp());
			data.add(reports.get(i).getCreateDate());
			try {
				jsonObject.put(Integer.toString(j), data);
			} catch (JSONException e) {
				logger.log( Level.SEVERE, e.toString(), e );
			}
			j++;
		}
		
	}  

}
