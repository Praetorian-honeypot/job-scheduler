package server;

import java.util.TimerTask;

public class ReportSender extends TimerTask {
	private Server server;
	
	public ReportSender (Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		server.requestReport();
	}

}
