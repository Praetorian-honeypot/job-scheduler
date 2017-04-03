package jobs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

public class JobDispatcherRemote extends UnicastRemoteObject implements JobDispatcher {
	private static final long serialVersionUID = -1052350105382199872L;
	private Job job = null;

	public JobDispatcherRemote() throws RemoteException {
		super();
	}

	@Override
	public int runJob() throws RemoteException {
		if (job == null)
			return -1000;
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return job.getId();
	}

	@Override
	public void cancelJob(Job job) throws RemoteException {
		// TODO: Cancel job
	}

	@Override
	public void setJob(Job job) throws RemoteException {
		this.job = job;
	}

}
