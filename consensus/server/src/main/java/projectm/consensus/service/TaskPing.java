package projectm.consensus.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import projectm.consensus.ApplicationConfig;
import projectm.consensus.ConsensusServer;
import projectm.consensus.NodeAddress;

public class TaskPing implements Task {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private CountDownLatch counter;
	private ConsensusServer server;
	private ApplicationConfig appConfig;

	public TaskPing(ConsensusServer server, ApplicationConfig appConfig) {
		this.server = server;
		this.appConfig = appConfig;
		this.counter = new CountDownLatch(appConfig.cluster().size());
	}

	private class Ping implements Runnable {

		private NodeAddress addr;

		public Ping(NodeAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {
			server.updateRemoteState(addr);
			counter.countDown();
		}
	}

	@Override
	public void execute() {
		for (NodeAddress addr : appConfig.cluster()) {
			new Thread(new Ping(addr)).start();
		}
		try {
			counter.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
