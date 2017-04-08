package jobs;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.PriorityQueue;

import server.Server;

public class JobDispatcherRemote extends UnicastRemoteObject implements JobDispatcher {
	private static final long serialVersionUID = -1052350105382199872L;
	private PriorityQueue<Job> waitingJobs = new PriorityQueue<Job>(new JobComparator());
	private Server server;

	public JobDispatcherRemote(Server server) throws RemoteException {
		super();
		this.setServer(server);
	}

	@Override
	public Job getJob(InetSocketAddress client) throws RemoteException {
		if (waitingJobs.isEmpty())
			return null;
		
		int clientId = server.getDatabase().findClient(client);
		for (Job job : waitingJobs) {
			job.incrementPriority();
		}
		
		Job nextJob = waitingJobs.remove();
		server.getDatabase().setJobStatus(nextJob.getId(), JobSchedulingEvent.getStatusCode("running"), clientId);
		return nextJob;
	}

	@Override
	public void cancelJob(Job job, InetSocketAddress client) throws RemoteException {
		setJobStatus(job, client, "cancelled");
	}
	
	@Override
	public void failJob(Job job, InetSocketAddress client) throws RemoteException {
		setJobStatus(job, client, "failed");
	}
	
	@Override
	public void finishJob(Job job, InetSocketAddress client) throws RemoteException {
		setJobStatus(job, client, "finished");
	}
	
	private void setJobStatus(Job job, InetSocketAddress client, String status) {
		int clientId = server.getDatabase().findClient(client);
		server.getDatabase().setJobStatus(job.getId(), JobSchedulingEvent.getStatusCode(status), clientId);
	}

	@Override
	public void scheduleJobs() throws RemoteException {
		waitingJobs.addAll(server.getDatabase().getAllWaitingJobs());
	}
	
	@Override
	public void addJob(Job job) throws RemoteException {
		waitingJobs.add(job);
	}
	
	@Override
	public boolean hasJobs() throws RemoteException {
		return !waitingJobs.isEmpty();
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}


}
