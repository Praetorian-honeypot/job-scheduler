package server;

import java.util.TimerTask;

public class JobSender extends TimerTask {
	private Server server;
	
	public JobSender (Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		server.requestRunJobs();
	}

}
