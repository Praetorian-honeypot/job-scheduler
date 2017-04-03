package server;

import java.util.TimerTask;

public class ReportDispatcher extends TimerTask {
	private Server server;
	
	public ReportDispatcher (Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		server.requestReport();
	}

}
