package api;

import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import jobs.Job;
import jobs.JobSchedulingEvent;
import server.Server;

@Path("/jobscheduleservice")
public class jobScheduleService{
	private transient static final Logger logger = Logger.getLogger( jobScheduleService.class.getName() );
	@Context
    Configuration config;

	@GET
	@Produces("application/json")
	public Response getJobSchedules(@QueryParam("date1") Date date1, @QueryParam("date2") Date date2, @QueryParam("job") Integer job,
									@QueryParam("status") Integer status, @QueryParam("client") Integer client) throws JSONException {

		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		ArrayList<JobSchedulingEvent> schedules = server.getDatabase().getAllJobSchedulingEvents(job);
		for(int i=0; i<schedules.size(); i++)
			getJobScheduleEvent(schedules.get(i), date1, date2, status, client, jsonObject, i);
		String result = "@Produces(\"application/json\") Output: \n\nJobScheduleService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 } 

	@Path("/addjobschedulingevent")
	@POST
	@Produces("application/json")
	public Response addJob(@QueryParam("date") Date date, @QueryParam("job") Integer job,
							@QueryParam("status") Integer status, @QueryParam("client") Integer client) throws JSONException {
		//todo
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");

		String result = "@Produces(\"application/json\") Output: \n\nJobScheduleService Output: \n\n" + jsonObject;
		return Response.status(200).entity(result).build();
	 }

	private void getJobScheduleEvent(JobSchedulingEvent j, Date date1, Date date2, Integer status,
			Integer client, JSONObject jsonObject, int i) {
		if(date1 != null && date2 != null)
			if(date1.after(j.getEventDate()) || date2.before(j.getEventDate()))
				return;
		if(status != null)
			if(status != j.getSchedStatus())
				return;
		if(client != null)
			if(j.getClient() != client)
				return;
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(j.getJob());
		data.add(j.getEventDate());
		data.add(j.getSchedStatus());
		data.add(j.getClient());
		try {
			jsonObject.put(Integer.toString(i), data);
		} catch (JSONException e) {
			logger.log( Level.SEVERE, e.toString(), e );
		}
	}
}