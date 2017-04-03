package jobs;

import java.rmi.*;

public interface JobDispatcher extends Remote {
	public void setJob(Job job) throws RemoteException;
	public int runJob() throws RemoteException;
	public void cancelJob(Job job) throws RemoteException;
}
