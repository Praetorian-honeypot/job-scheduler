package server;

import java.net.InetSocketAddress;
import java.util.Date;

public class ClientReport {
	private InetSocketAddress clientAddress;
	private double cpuLoad;
	private double memAvailable;
	private double cpuTemp;
	private Date createDate;
	
	public ClientReport(InetSocketAddress clientAddress, double cpuLoad, double memAvailable, double cpuTemp) {
		this.clientAddress = clientAddress;
		this.cpuLoad = cpuLoad;
		this.memAvailable = memAvailable;
		this.cpuTemp = cpuTemp;
		this.createDate = new Date();
	}
	
	public ClientReport(InetSocketAddress clientAddress, double cpuLoad, double memAvailable, double cpuTemp, int date) {
		this.clientAddress = clientAddress;
		this.cpuLoad = cpuLoad;
		this.memAvailable = memAvailable;
		this.cpuTemp = cpuTemp;
		this.createDate = new Date(date * 1000L);
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

	public InetSocketAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetSocketAddress clientAddress) {
		this.clientAddress = clientAddress;
	}
	
}
