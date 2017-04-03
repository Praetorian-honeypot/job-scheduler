package jobs;

import java.io.Serializable;
import java.util.Date;

public class Job implements Serializable {
	private static final long serialVersionUID = -2507969953093203316L;
	//private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Date deadline;
	private String command;
	private int priority;
	private int id;
	private int currentStatus;
	
	public Job(String command, int priority, int deadline) {
		this.setCommand(command);
		this.setPriority(priority);
		this.setDeadline(new Date(deadline * 1000L));
	}
	
	public Job(int id, String command, int priority, int deadline) {
		this(command, priority, deadline);
		this.setId(id);
	}
	
	public Job(int id, String command, int priority, int deadline, int currentStatus) {
		this(id, command, priority, deadline);
		this.setCurrentStatus(currentStatus);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(int currentStatus) {
		this.currentStatus = currentStatus;
	}

	public void incrementPriority() {
		this.priority++;
	}
}
