package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import server.Server;

public class SQLite {
	private Server server;

	public SQLite(Server server) {
		this.server = server;
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:server.db");
			server.log("Opened database succesfully");
			
			if (!tableExists(c, "clients"))
				createClientsTable(c);
			if (!tableExists(c, "clientReports"))
				createClientReportsTable(c);
			if (!tableExists(c, "clientGroups"))
				createClientGroupsTable(c);
			if (!tableExists(c, "jobs"))
				createJobsTable(c);
			if (!tableExists(c, "jobSchedulingEvents"))
				createJobSchedulingEventsTable(c);
			
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}

	private boolean tableExists(Connection c, String table) {
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

	private void createClientsTable(Connection c) {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE clients " +
	                   "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
	                   " address        TEXT NOT NULL, " + 
	                   " hostname       TEXT NOT NULL, " + 
	                   " port           INTEGER NOT NULL, " + 
	                   " clientGroup    INTEGER, " + 
	                   " cpuName        TEXT NOT NULL, " + 
	                   " cpuCores       INTEGER DEFAULT 0, " + 
	                   " createDate     INTEGER)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			server.log("Succesfully created clients table");
		} catch (Exception exception) {
			server.log( Level.SEVERE, exception.toString(), exception );
		}
	}
	
	private void createClientReportsTable(Connection c) {
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
	
	private void createClientGroupsTable(Connection c) {
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
	
	private void createJobsTable(Connection c) {
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
	
	private void createJobSchedulingEventsTable(Connection c) {
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

