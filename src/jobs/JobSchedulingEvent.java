package jobs;

public class JobSchedulingEvent {
	private static String[] schedStatuses = {"entered", "scheduled", "running", "finished", "failed", "cancelled", "killed"};
	
	public static int getStatusCode(String searchStatus) {
		int code = 0, i = 0;
		for (String status : schedStatuses) {
			if (searchStatus.equals(status))
				code = i;
			i++;
		}
		return code;
	}
}
