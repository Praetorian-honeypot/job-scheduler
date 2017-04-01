package database;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

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
		return findSingle("clients", "address = '" + client.getAddress() + "' AND port = " + client.getPort());
	}
	
	public void addClient(InetSocketAddress client) {
		try {
			Statement stmt = c.createStatement();
			int time = (int) (new Date().getTime() / 1000);
			String sql = "INSERT INTO clients (address, hostname, port, createDate) " +
						 "VALUES ('"+client.getAddress()+"', '"+client.getHostName()+"', "+client.getPort()+", "+time+");";
			stmt.executeUpdate(sql);
			server.log("Succesfully added client");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
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
			Statement stmt = c.createStatement();
			int time = (int) (report.getCreateDate().getTime() / 1000);
			String sql = "INSERT INTO clientReports (client,date,cpuLoad,memAvailable,cpuTemp) " +
						 "VALUES ("+clientId+", "+time+", "+report.getCpuLoad()+", "+report.getMemAvailable()+", "+report.getCpuTemp()+");";
			stmt.executeUpdate(sql);
			server.log("Succesfully saved client report");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	private boolean tableExists(String table) {
		boolean tableExists = false;
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT * FROM sqlite_master WHERE name ='" + table + "' and type='table'; ";
			ResultSet rs = stmt.executeQuery(sql);
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
	                   " address        TEXT NOT NULL, " + 
	                   " hostname       TEXT NOT NULL, " + 
	                   " hostport       INTEGER NOT NULL, " + 
	                   " displayName    TEXT NOT NULL, " + 
	                   " clientGroup    INTEGER, " + 
	                   " cpuName        TEXT, " + 
	                   " cpuCores       INTEGER DEFAULT 0, " + 
	                   " memoryAmount   INTEGER DEFAULT 0, " + 
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

