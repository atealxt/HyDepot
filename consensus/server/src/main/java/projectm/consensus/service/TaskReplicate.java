package projectm.consensus.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private final Lock lockConsensus = new ReentrantLock();
	private final Condition complete = lockConsensus.newCondition();
	private int minTickets;
	private AtomicInteger vote = new AtomicInteger(0);

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

	public TaskReplicate buildConsensus(int minTickets) {
		this.minTickets = minTickets;
		return this;
	}

	private class Replicate implements Runnable {

		private NodeAddress addr;

		public Replicate(NodeAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {
			server.replicate(addr, resource);
			vote.incrementAndGet();
			counter.countDown();
			if (vote.get() >= minTickets || counter.getCount() == 0) {
				lockConsensus.lock();
				try {
					complete.signal();
				} finally {
					lockConsensus.unlock();
				}
			}
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
		lockConsensus.lock();
		try {
			complete.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} finally {
			lockConsensus.unlock();
		}
	}

}
