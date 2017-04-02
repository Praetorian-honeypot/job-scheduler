package jobs;

import java.util.Date;

public class JobSchedulingEvent {
	private static String[] schedStatuses = {"entered", "scheduled", "running", "finished", "failed", "cancelled", "killed"};
	private int job;
	private Date eventDate;
	private int schedStatus;
	private int client;
	
	public JobSchedulingEvent(int job, int eventDate, int schedStatus, int client) {
		this.setJob(job);
		this.setEventDate(new Date(eventDate * 1000L));
		this.setSchedStatus(schedStatus);
		this.setClient(client);
	}
	
	public static int getStatusCode(String searchStatus) {
		int code = 0, i = 0;
		for (String status : schedStatuses) {
			if (searchStatus.equals(status))
				code = i;
			i++;
		}
		return code;
	}
	
	public static String getStatus(int code) {
		return (code < schedStatuses.length - 1) ? schedStatuses[code] : null;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public int getSchedStatus() {
		return schedStatus;
	}

	public void setSchedStatus(int schedStatus) {
		this.schedStatus = schedStatus;
	}

	public int getClient() {
		return client;
	}

	public void setClient(int client) {
		this.client = client;
	}
}
