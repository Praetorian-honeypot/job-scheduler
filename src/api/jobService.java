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

@Path("jobservice")
public class jobService{
	private transient static final Logger logger = Logger.getLogger( reportService.class.getName() );
	@Context
    Configuration config;
	
	@GET
	@Produces("application/json")
	public Response getJobs(	@QueryParam("address") String addr, @QueryParam("port") Integer port,
								@QueryParam("date1") String date1, @QueryParam("date2") String date2, 
								@QueryParam("load1") Integer load1, @QueryParam("load2") Integer load2) throws JSONException {


		
		String result = "@Produces(\"application/json\") Output: \n\nReportService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 } 


}
