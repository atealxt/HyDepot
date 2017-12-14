package projectm.consensus.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
	private static Map<ReplicateIdentity, BlockingQueue<Object>> runnablePool = new ConcurrentHashMap<>();
	private static final Lock lockRunnable = new ReentrantLock();

	public TaskReplicate(ConsensusServer server, Resource resource) {
		this.server = server;
		this.states = server.getStates();
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

	private class ReplicateIdentity {
		private String resourceKey;
		private ConsensusServer server;

		public ReplicateIdentity(String resourceKey, ConsensusServer server) {
			super();
			this.resourceKey = resourceKey;
			this.server = server;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resourceKey == null) ? 0 : resourceKey.hashCode());
			result = prime * result + ((server == null) ? 0 : server.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReplicateIdentity other = (ReplicateIdentity) obj;
			if (resourceKey == null) {
				if (other.resourceKey != null)
					return false;
			} else if (!resourceKey.equals(other.resourceKey))
				return false;
			if (server == null) {
				if (other.server != null)
					return false;
			} else if (!server.equals(other.server))
				return false;
			return true;
		}
	}

	private class Replicate implements Runnable {

		private NodeAddress addr;

		public Replicate(NodeAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {

			try {
				server.replicate(addr, resource);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

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
			if (counter.getCount() == 0) {
//				logger.info(server.getClass().getSimpleName() + " complete replicate resource " + resource);
				runnablePool.get(new ReplicateIdentity(resource.getKey(), server)).offer(1);
			}
		}
	}

	@Override
	public void execute() {
		if (counter.getCount() == 0) {
			return;
		}
		ReplicateIdentity identity = new ReplicateIdentity(resource.getKey(), server);
		lockRunnable.lock();
		try {
			if (runnablePool.containsKey(identity)) {
				// which means same resource are running and not complete yet.
				try {
//					logger.info(this.server.getClass().getSimpleName() + " is waiting for ticket to replicate " + resource);
					runnablePool.get(identity).take();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
//				logger.info(this.server.getClass().getSimpleName() + " first meet resource " + resource.getKey());
				runnablePool.put(identity, new ArrayBlockingQueue<>(1, true));
			}
		} finally {
			lockRunnable.unlock();
		}
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
