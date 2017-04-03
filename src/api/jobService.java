package api;

import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import jobs.Job;
import server.Server;

@Path("/jobservice")
public class jobService{
	private transient static final Logger logger = Logger.getLogger( jobService.class.getName() );
	@Context
    Configuration config;
	
	@GET
	@Produces("application/json")
	public Response getJobs(@QueryParam("date1") Date date1, @QueryParam("date2") Date date2, @QueryParam("command") String command, 
							@QueryParam("priority") Integer priority, @QueryParam("jobid") Integer jobId) throws JSONException {

		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		ArrayList<Job> jobs = server.getDatabase().getAllJobs();
		for(int i=0; i<jobs.size(); i++)
			getJob(jobs.get(i), date1, date2, command, priority, jobId, jsonObject, i);
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	 } 
	
	@Path("/addjob")
	@POST
	@Produces("application/json")
	public Response addJob(@QueryParam("command") String command, @QueryParam("priority") Integer priority, @QueryParam("deadline") Integer deadline) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		int jobId = server.getDatabase().addJob(command, priority, deadline);
		if(server.getDatabase().getJob(jobId) == null) 
			jsonObject.put("status", "Adding job failed");
		else 
			jsonObject.put("status", "Job added succesfully with jobId " + jobId);
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	}
	
	@Path("/removejob")
	@DELETE
	@Produces("application/json")
	public Response removeJob(@QueryParam("job") Integer job) throws JSONException {
		//todo
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	} 
	
	@Path("/changejob")
	@PUT
	@Produces("application/json")
	public Response changeJob(	@QueryParam("job") Integer job, @QueryParam("command") String command, 
								@QueryParam("priority") Integer priority, @QueryParam("deadline") Date deadline) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		Server server = (Server) config.getProperty("server");
		Job j = server.getDatabase().getJob(job);
		if(server.getDatabase().getJobSchedulingEvent(job) != null && server.getDatabase().getJobSchedulingEvent(job).getSchedStatus() < 1) {
			if(command != null)
				j.setCommand(command);
			if(priority != null)
				j.setPriority(priority);
			if(deadline != null)
				j.setDeadline(deadline);
			jsonObject.put("status", "Job with jobId " + job + " changed successfully");
		} else {
			jsonObject.put("status", "Job with jobId " + job + " could not be changed");
		}
		
		String result = jsonObject.toString();
		return Response.status(200).entity(result).build();
	 } 
	
	
	private void getJob(Job j, Date date1, Date date2, String command, Integer priority, Integer jobId, JSONObject jsonObject, int i) {
		if(date1 != null && date2 != null) 
			if(date1.after(j.getDeadline()) || date2.before(j.getDeadline()))
				return;
		if(command != null)
			if(command != j.getCommand())
				return;
		if(priority != null)
			if(j.getPriority() < priority)
				return;
		if(jobId != null)
			if(jobId != j.getId())
				return;
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(j.getId());
		data.add(j.getDeadline());
		data.add(j.getCommand());
		data.add(j.getPriority());
		try {
			jsonObject.put(Integer.toString(i), data);
		} catch (JSONException e) {
			logger.log( Level.SEVERE, e.toString(), e );
		}
	}


}
