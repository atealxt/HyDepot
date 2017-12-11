package projectm.consensus.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import projectm.consensus.ConsensusServer;
import projectm.consensus.NodeAddress;
import projectm.consensus.State;

public class TaskReplicate implements Task {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private CountDownLatch counter;
	private ConsensusServer server;
	private Resource resource;
	private Map<NodeAddress, State> states = new HashMap<>();

	public TaskReplicate(ConsensusServer server, Resource resource) {
		this.server = server;
		this.states = server.geStates();
		int numFollowers = 0;
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (entry.getValue() == State.FOLLOWER) {
				numFollowers++;
			}
		}
		this.counter = new CountDownLatch(numFollowers);
		this.resource = resource;
	}

	private class Replicate implements Runnable {

		private NodeAddress addr;

		public Replicate(NodeAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {
			server.replicate(addr, resource);
			counter.countDown();
		}
	}

	@Override
	public void execute() {
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (entry.getValue() != State.FOLLOWER) {
				continue;
			}
			new Thread(new Replicate(entry.getKey())).start();
		}
		if (!server.strongConsist()) {
			return;
		}
		try {
			// TODO more than half is fine
			counter.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
