package server;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientReport {
	private InetSocketAddress clientAddress;
	private double cpuLoad;
	private double memAvailable;
	private double cpuTemp;
	private Date createDate;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public ClientReport(InetSocketAddress clientAddress, double cpuLoad, double memAvailable, double cpuTemp) {
		this.clientAddress = clientAddress;
		this.cpuLoad = cpuLoad;
		this.memAvailable = memAvailable;
		this.cpuTemp = cpuTemp;
		this.createDate = new Date();
	}
	
	public ClientReport(InetSocketAddress clientAddress, double cpuLoad, double memAvailable, double cpuTemp, Integer date) {
		this.clientAddress = clientAddress;
		this.cpuLoad = cpuLoad;
		this.memAvailable = memAvailable;
		this.cpuTemp = cpuTemp;
		try {
			this.createDate = sdf.parse(date.toString());
		} catch (ParseException exception) {
			exception.printStackTrace();
		}
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
