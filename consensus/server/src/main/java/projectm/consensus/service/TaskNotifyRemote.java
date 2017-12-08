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

public class TaskNotifyRemote implements Task {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private CountDownLatch counter;
	private ConsensusServer server;
	private Map<NodeAddress, State> states = new HashMap<>();
	private State state;
	private boolean consensus;
	private final Lock lockConsensus = new ReentrantLock();
	private final Condition complete = lockConsensus.newCondition();
	private int minTickets;
	private AtomicInteger vote = new AtomicInteger(0);

	public TaskNotifyRemote(ConsensusServer server, Map<NodeAddress, State> states, State state) {
		this.server = server;
		this.states = states;
		this.state = state;
		this.counter = new CountDownLatch(states.size());
	}

	public TaskNotifyRemote buildConsensus(int minTickets) {
		this.consensus = true;
		this.minTickets = minTickets;
		return this;
	}

	private class NotifyRemote implements Runnable {

		private NodeAddress addr;

		public NotifyRemote(NodeAddress addr) {
			this.addr = addr;
		}

		@Override
		public void run() {
			NotifyResult result = server.notifyRemote(addr, state);
			boolean accept = result.isAccept();
			if (accept) {
				vote.incrementAndGet();
			}
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
			new Thread(new NotifyRemote(entry.getKey())).start();
		}
		if (consensus) {
			lockConsensus.lock();
			try {
				complete.await();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			} finally {
				lockConsensus.unlock();
			}
		} else {
			try {
				counter.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public int getVote() {
		return vote.get();
	}
}
