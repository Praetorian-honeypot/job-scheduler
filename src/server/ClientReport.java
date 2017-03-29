package server;

import java.util.Date;

public class ClientReport {
	private double cpuLoad;
	private double memAvailable;
	private double cpuTemp;
	private Date createDate;
	
	public ClientReport(double cpuLoad, double memAvailable, double cpuTemp) {
		this.cpuLoad = cpuLoad;
		this.memAvailable = memAvailable;
		this.cpuTemp = cpuTemp;
		this.createDate = new Date();
	}
	
	public double getCpuLoad() {
		return cpuLoad;
	}
	
	public void setCpuLoad(double cpuLoad) {
		this.cpuLoad = cpuLoad;
	}
	
	public double getMemAvailable() {
		return memAvailable;
	}
	
	public void setMemAvailable(double memAvailable) {
		this.memAvailable = memAvailable;
	}
	
	public double getCpuTemp() {
		return cpuTemp;
	}
	
	public void setCpuTemp(double cpuTemp) {
		this.cpuTemp = cpuTemp;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	
}
