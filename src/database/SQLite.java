package database;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import jobs.Job;
import jobs.JobSchedulingEvent;
import server.ClientReport;
import server.Server;

public class SQLite {
	private Server server;
	private Connection c = null;

	public SQLite(Server server) {
		this.server = server;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:server.db");
			c.setAutoCommit(true);
			server.log("Opened database succesfully");
			
			if (!tableExists("clients"))
				createClientsTable();
			if (!tableExists("clientReports"))
				createClientReportsTable();
			if (!tableExists("clientGroups"))
				createClientGroupsTable();
			if (!tableExists("jobs"))
				createJobsTable();
			if (!tableExists("jobSchedulingEvents"))
				createJobSchedulingEventsTable();
			
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public int findSingle(String table, String whereStatement) {
		int id = 0;
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT * FROM " + table + " WHERE " + whereStatement;
			ResultSet found = stmt.executeQuery(sql);
			if (found.next())
				id = found.getInt("id");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return id;
	}

	public ResultSet find(String table, String whereStatement) {
		ResultSet found = null;
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT * FROM " + table + " WHERE " + whereStatement;
			found = stmt.executeQuery(sql);
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return found;
	}
	
	public int findClient(InetSocketAddress client) {
		return findSingle("clients", "address = '" + client.getAddress() + "' AND hostport = " + client.getPort());
	}
	
	public void addClient(InetSocketAddress client) {
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO clients (address, hostname, hostport, createDate) VALUES (?,?,?,?)");
			int time = (int) (new Date().getTime() / 1000);
			stmt.setString(1, client.getAddress().toString());
			stmt.setString(2, client.getHostName());
			stmt.setInt(3, client.getPort());
			stmt.setInt(4, time);
			stmt.executeUpdate();
			server.log("Succesfully added client");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public int addClient(InetSocketAddress client, String cpuName, int cpuCores, String os, int memory,
			String displayName, int performance, int time) {
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO clients (client, address, hostname, hostport, cpuName, cpuCores, operatingSystem, memoryAmount, displayName, performance, createDate) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			int clientId = stmt.getGeneratedKeys().getInt(1);
			stmt.setInt(1, clientId);
			stmt.setString(2, client.getAddress().toString());
			stmt.setString(3, client.getHostName());
			stmt.setInt(4, client.getPort());
			stmt.setString(5, cpuName);
			stmt.setInt(6, cpuCores);
			stmt.setString(7, os);
			stmt.setInt(8, memory);
			stmt.setString(9, displayName);
			stmt.setInt(10, performance);
			stmt.setInt(11, time);
			stmt.executeUpdate();
			server.log("Succesfully added client with specs");
			return clientId;
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return 0;
	}
	
	public Collection<? extends ClientReport> getClientReports(int clientId, InetSocketAddress clientAddress) {
		ArrayList<ClientReport> clientReports = new ArrayList<ClientReport>();
		
		try {
			ResultSet dbClientReports = find("clientReports", "client = " + clientId);
			while (dbClientReports.next()) {
				double cpuLoad = dbClientReports.getDouble("cpuLoad");
				double memAvailable = dbClientReports.getDouble("memAvailable");
				double cpuTemp = dbClientReports.getDouble("cpuTemp");
				int createDate = dbClientReports.getInt("date");
				ClientReport clientReport = new ClientReport(clientAddress, cpuLoad, memAvailable, cpuTemp, createDate);
				clientReports.add(clientReport);
			}
		} catch (SQLException exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
		return clientReports;
	}
	
	public void addReport(ClientReport report) {
		int clientId = findClient(report.getClientAddress());
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO clientReports (client,date,cpuLoad,memAvailable,cpuTemp) VALUES (?,?,?,?,?)");
			int time = (int) (report.getCreateDate().getTime() / 1000);
			stmt.setInt(1, clientId);
			stmt.setInt(2, time);
			stmt.setDouble(3, report.getCpuLoad());
			stmt.setDouble(4, report.getMemAvailable());
			stmt.setDouble(5, report.getCpuTemp());
			stmt.executeUpdate();
			server.log("Succesfully saved client report");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public int addJob(String command, int priority, int deadline) {
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO jobs (command, priority, deadline) VALUES (?,?,?)");
			stmt.setString(1, command);
			stmt.setInt(2, priority);
			stmt.setInt(3, deadline);
			stmt.executeUpdate();
			
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	            	PreparedStatement stmt2 = c.prepareStatement("INSERT INTO jobSchedulingEvents (job, eventDate, schedStatus) VALUES (?,?,?)"); 
	    			int jobId = stmt.getGeneratedKeys().getInt(1);
	    			int schedStatus = JobSchedulingEvent.getStatusCode("entered");
	    			int time = (int) (new Date().getTime() / 1000);
	    			stmt2.setInt(1, jobId);
	    			stmt2.setInt(2, time);
	    			stmt2.setInt(3, schedStatus);
	    			stmt2.executeUpdate();
	    			server.log("Succesfully added job with id: " + jobId);
	    			return jobId;
	            }
	            else {
	                throw new SQLException("Creating job failed, no ID obtained.");
	            }
	        }
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return Integer.MAX_VALUE;
	}
	
	public Job getJob(int jobId) {
		Job job = null;
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM jobs WHERE id = ?");
			stmt.setInt(1, jobId);
			ResultSet found = stmt.executeQuery();
			if (found.next())
				job = new Job(found.getInt("id"), found.getString("command"), found.getInt("priority"), found.getInt("deadline"));
			else
				server.log("Cannot find job with id: " + jobId);
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return job;
	}
	
	public ArrayList<Job> getAllJobs() {
		ArrayList<Job> jobs = new ArrayList<Job>();
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM jobs");
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				jobs.add(new Job(result.getInt("id"), result.getString("command"), result.getInt("priority"), result.getInt("deadline")));
			}
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return jobs;
	}
	
	public void setJobStatus(int jobId, int schedStatus, int clientId) {
		Job job = getJob(jobId);
		
		if (job == null) {
			return;
		}
		
		if (JobSchedulingEvent.getStatus(schedStatus) == null) {
			server.log("ERROR: this is an invalid scheduling status");
			return;
		}			
		
		try {
			PreparedStatement stmt = c.prepareStatement("INSERT INTO jobSchedulingEvents (job, eventDate, schedStatus, client) VALUES (?,?,?,?)"); 
			int time = (int) (new Date().getTime() / 1000);
			stmt.setInt(1, jobId);
			stmt.setInt(2, time);
			stmt.setInt(3, schedStatus);
			stmt.setInt(4, clientId);
			stmt.executeUpdate();
			server.log("Succesfully modified job status to: " + JobSchedulingEvent.getStatus(schedStatus));
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	public void setJobStatus(int jobId, String schedStatus, int clientId) {
		int statusId = JobSchedulingEvent.getStatusCode(schedStatus);
		setJobStatus(jobId, statusId, clientId);
	}
	
	public void setJobStatus(int jobId, String schedStatus) {
		setJobStatus(jobId, schedStatus, 0);
	}
	
	public void setJobStatus(int jobId, int schedStatus) {
		setJobStatus(jobId, schedStatus, 0);
	}
	
	public int getJobStatus(int jobId) {
		int status = -1;
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT jse.schedStatus FROM jobSchedulingEvents AS jse "
					+ "INNER JOIN (SELECT jse2.id, max(eventDate) FROM jobSchedulingEvents AS jse2 WHERE job = ?) AS jse2 "
					+ "ON jse.id = jse2.id "
					+ "WHERE job = ?");
			stmt.setInt(1, jobId);
			stmt.setInt(2, jobId);
			ResultSet found = stmt.executeQuery();
			if (found.next())
				status = found.getInt("schedStatus");
			else
				server.log("Cannot find scheduling event status of job with id: " + jobId);
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return status;
	}
	
	public JobSchedulingEvent getJobSchedulingEvent(int jobId) {
		JobSchedulingEvent event = null;
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT jse.* FROM jobSchedulingEvents AS jse "
					+ "INNER JOIN (SELECT jse2.id, max(eventDate) FROM jobSchedulingEvents AS jse2 WHERE job = ?) AS jse2 "
					+ "ON jse.id = jse2.id "
					+ "WHERE job = ?");
			stmt.setInt(1, jobId);
			stmt.setInt(2, jobId);
			ResultSet found = stmt.executeQuery();
			if (found.next())
				event = new JobSchedulingEvent(jobId, found.getInt("eventDate"), found.getInt("schedStatus"), found.getInt("client"));
			else
				server.log("Cannot find scheduling event of job with id: " + jobId);
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return event;
	}
	
	public ArrayList<JobSchedulingEvent> getAllJobSchedulingEvents(int jobId) {
		ArrayList<JobSchedulingEvent> events = new ArrayList<JobSchedulingEvent>();
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM jobSchedulingEvents WHERE job = ?");
			stmt.setInt(1, jobId);
			ResultSet found = stmt.executeQuery();
			while (found.next())
				events.add(new JobSchedulingEvent(jobId, found.getInt("eventDate"), found.getInt("schedStatus"), found.getInt("client")));
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return events;
	}
	
	public ArrayList<JobSchedulingEvent> getAllJobSchedulingEvents() {
		ArrayList<JobSchedulingEvent> events = new ArrayList<JobSchedulingEvent>();
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM jobSchedulingEvents");
			ResultSet found = stmt.executeQuery();
			while (found.next())
				events.add(new JobSchedulingEvent(found.getInt("job"), found.getInt("eventDate"), found.getInt("schedStatus"), found.getInt("client")));
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		return events;
	}
	
	public void setSpecs(InetSocketAddress client, String cpuName, int cpuCores, String operatingSystem, int memoryAmount, String displayName,
			int performance) {
		int clientId = findClient(client);
		try {
			PreparedStatement stmt = c.prepareStatement("UPDATE clients (cpuName, cpuCores, operatingSystem, memoryAmount, displayName, performance) SET (?,?,?,?,?,?) WHERE id = ?");
			stmt.setString(1, cpuName);
			stmt.setInt(2, cpuCores);
			stmt.setString(3, operatingSystem);
			stmt.setInt(4, memoryAmount);
			stmt.setString(5, displayName);
			stmt.setInt(6, performance);
			stmt.setInt(7, clientId);
			stmt.executeUpdate();
			server.log("Succesfully saved client report");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
	}

	private boolean tableExists(String table) {
		boolean tableExists = false;
		try {
			PreparedStatement stmt = c.prepareStatement("SELECT * FROM sqlite_master WHERE name = ? and type='table'");
			stmt.setString(1, table);
			ResultSet rs = stmt.executeQuery();
			tableExists = rs.next();
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
		
		return tableExists;
	}

	private void createClientsTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE clients " +
	                   "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
	                   " client         INTEGER NOT NULL, " +
	                   " address        TEXT NOT NULL, " + 
	                   " hostname       TEXT NOT NULL, " + 
	                   " hostport       INTEGER NOT NULL, " + 
	                   " displayName    TEXT, " + 
	                   " clientGroup    INTEGER, " + 
	                   " cpuName        TEXT, " + 
	                   " cpuCores       INTEGER DEFAULT 0, " + 
	                   " operatingSystem TEXT, " + 
	                   " memoryAmount   INTEGER DEFAULT 0, " + 
	                   " performance    INTEGER DEFAULT 0, " + 
	                   " createDate     INTEGER)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created clients table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	private void createClientReportsTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE clientReports " +
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
					" client         	INTEGER NOT NULL, " + 
					" date           	INTEGER NOT NULL, " + 
					" cpuLoad     		REAL, " +
					" memAvailable     	REAL, " +
					" cpuTemp     		REAL)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created client reports table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	private void createClientGroupsTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE clientGroups " +
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
					" groupName    TEXT NOT NULL, " + 
					" superGroup   INTEGER)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created client groups table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	private void createJobsTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE jobs " +
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
					" command    TEXT NOT NULL, " + 
					" priority   INTEGER DEFAULT 1, " +
					" deadline   INTEGER DEFAULT 0)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created jobs table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	private void createJobSchedulingEventsTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE jobSchedulingEvents " +
					"(id INTEGER PRIMARY KEY AUTOINCREMENT," +
					" job    	 INTEGER NOT NULL, " + 
					" client  INTEGER, " +
					" eventDate  INTEGER DEFAULT 0, " +
					" schedStatus INTEGER DEFAULT 0)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created job scheduling events table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
}

