package jobs;

import java.net.InetSocketAddress;
import java.rmi.*;

public interface JobDispatcher extends Remote {
	public void addJob(Job job) throws RemoteException;
	public void scheduleJobs() throws RemoteException;
	public Job getJob(InetSocketAddress client) throws RemoteException;
	public void cancelJob(Job job, InetSocketAddress client) throws RemoteException;
	public void failJob(Job job, InetSocketAddress client) throws RemoteException;
	public void finishJob(Job job, InetSocketAddress client) throws RemoteException;
	public boolean hasJobs() throws RemoteException;
}
