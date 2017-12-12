package projectm.consensus.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectm.consensus.ApplicationConfig;
import projectm.consensus.ConsensusException;
import projectm.consensus.ConsensusServer;
import projectm.consensus.NodeAddress;
import projectm.consensus.State;

@Service
public class DefaultConsensusServerL2 implements ConsensusServer {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;
	private State state = State.INACTIVE;
	private Map<NodeAddress, State> states = new HashMap<>();
	private Map<NodeAddress, Long> candidateTenancy = new HashMap<>();
	private Map<String, Resource> memoryResources = new HashMap<>();
	private final Lock lockResource = new ReentrantLock();

	@Override
	public void startUp() {
		if (getCluster().isEmpty()) {
			return;
		}
		ping();
		new Thread(new CandidateCleanup(), "CandidateCleanup").start();
		new Thread(new CheckAndElection(), "CheckAndElection").start();
		transition(State.RESERVE);
	}

	@Override
	public void tearDown() {
	}

	@Override
	public State transition(State state) {
		// RESERVE->FOLLOWER->CANDIDATE->LEADER
		logger.info("Transiting to " + state);

		switch (state) {
		case RESERVE:
			this.state = state;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				notifyRemote(addr, state);
			}
			transition(State.FOLLOWER);
			break;
		case FOLLOWER:
			this.state = state;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				notifyRemote(addr, state);
			}
			break;
		case CANDIDATE:
			try {
				Thread.sleep(500 + new Random().nextInt(5000));
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			this.state = state;
			ping();
			int min = ticketsToWin();
			// TODO base on the latest log
			TaskNotifyRemote notification = new TaskNotifyRemote(this, states, state).buildConsensus(min);
			notification.execute();
			if (notification.getVote() >= min) {
				transition(State.LEADER);
			} else {
				transition(State.FOLLOWER);
			}
			break;
		case LEADER:
			this.state = state;
			// TODO become the leader of l2.
			// 1)change dns
			// 2)get repli info from l1 leader. become the follower of l1.
			new TaskNotifyRemote(this, states, state).execute();
			break;
		default:
			break;
		}

		return getState();
	}

	private int ticketsToWin() {
		int n = 0;
		for (Entry<NodeAddress, State> state : states.entrySet()) {
			if (state.getValue() != State.INACTIVE) {
				n++;
			}
		}
		return Math.max(n, 1);
	}

	private boolean noState(State state) {
		if (this.state == state) {
			return false;
		}
		boolean hasState = false;
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (entry.getValue() == state) {
				hasState = true;
			}
		}
		return !hasState;
	}

	@Override
	public NotifyResult notifyRemote(NodeAddress addr, State state) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensusL2/notify")//
					.setParameter("ip", appConfig.getIp())//
					.setParameter("port", String.valueOf(appConfig.getPort()))//
					.setParameter("state", state.getVal())//
					.build();
			logger.info("Notify " + addr + " with my state " + state);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		HttpGet httpGet = new HttpGet(uri);
		try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent());
			// logger.info(addr + " resp is " + body);
			ObjectMapper mapper = new ObjectMapper();
			NotifyResult result = mapper.readValue(body, NotifyResult.class);
			if (!result.isAccept()) {
				logger.warn(addr + " not accept my state: " + state);
			} else {
				states.put(addr, result.getState());
			}
			return result;
		} catch (Exception e) {
			// logger.info("Failed to talk to " + addr + ": " + e.getMessage());
			return new NotifyResult(false, State.INACTIVE);
		}
	}

	private State getRemoteState(NodeAddress addr) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensusL2/state")//
					.build();
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		HttpGet httpGet = new HttpGet(uri);
		try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent()).replace("\"", "");
			// logger.info(addr + " resp is " + body);
			return State.parse(body);
		} catch (Exception e) {
			// logger.info("Failed to talk to " + addr + ": " + e.getMessage());
			return State.INACTIVE;
		}
	}

	@Override
	public synchronized NotifyResult notify(NodeAddress nodeAddress, State state) {

		logger.info(nodeAddress + " transit to " + state);

		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (!entry.getKey().equals(nodeAddress)) {
				continue;
			}
			switch (state) {
			case RESERVE:
				entry.setValue(state);
				candidateTenancy.remove(nodeAddress);
				break;
			case FOLLOWER:
				entry.setValue(state);
				candidateTenancy.remove(nodeAddress);
				break;
			case CANDIDATE:
				if (!noState(State.LEADER) || !noState(State.CANDIDATE)) {
					return new NotifyResult(false, getState());
				}
				entry.setValue(state);
				candidateTenancy.put(nodeAddress, System.currentTimeMillis());
				break;
			case LEADER:
				entry.setValue(state);
				candidateTenancy.remove(nodeAddress);
				break;
			default:
				break;
			}
			break;
		}
		return new NotifyResult(true, getState());
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public NodeAddress getLeaderAddress() {
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (entry.getValue() == State.LEADER) {
				return entry.getKey();
			}
		}
		if (state == State.LEADER) {
			logger.warn("I'm already the leader!");
			return new NodeAddress(appConfig.getIp(), appConfig.getPort());
		}
		throw new ConsensusException("No leader in cluster yet.");
	}

	private class CandidateCleanup implements Runnable {

		private int intervalMs = 5000;

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(intervalMs);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				for (Iterator<Entry<NodeAddress, Long>> it = candidateTenancy.entrySet().iterator(); it.hasNext();) {
					Entry<NodeAddress, Long> entry = it.next();
					if ((System.currentTimeMillis() - intervalMs) >= entry.getValue()) {
						it.remove();
						logger.info("Candidate " + entry.getKey() + " is expired.");
					}
				}
			}
		}
	}

	private void ping() {
		new TaskPing(this).execute();
	}

	private class CheckAndElection implements Runnable {

		private int intervalMs = 10000;

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(intervalMs);
					ping();
					if (noState(State.LEADER)) {
						logger.info("No Leader, start new election...");
						transition(State.CANDIDATE);
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	/** Only useful for leader. Follows request will redirect to Leader. */
	@Override
	public Resource getResource(String key) {
		lockResource.lock();
		try {
			if (!memoryResources.containsKey(key)) {
				return null;
			}
			return memoryResources.get(key);
		} finally {
			lockResource.unlock();
		}
	}

	@Override
	public Resource addResource(String key, String value, Long id) {
		lockResource.lock();
		try {
			Resource resource;

			if (getState() == State.LEADER) {
				if (id == null) {
					if (memoryResources.containsKey(key)) {
						throw new ConsensusException("Invalid state.");
					}
					resource = new Resource(key, value);
					memoryResources.put(key, resource);
					logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " created " + resource);
				} else {
					if (!memoryResources.containsKey(key)) {
						throw new ConsensusException("Invalid state.");
					}
					resource = memoryResources.get(key);
					if (resource.getId().get() != id) {
						logger.info("Fail to update value in concurrent case: " + key + ", " + id);
						return null;
					}
					resource.setValue(value);
					resource.getId().incrementAndGet();
					logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " updated " + resource);
				}
				// notify followers
				replicate(resource);
			} else {
				if (!memoryResources.containsKey(key)) {
					resource = new Resource(key, value);
					memoryResources.put(key, resource);
					logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " created " + resource);
				} else {
					resource = memoryResources.get(key);
					resource.setValue(value);
					resource.getId().incrementAndGet();
					logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " updated " + resource);
				}
			}

			return resource;
		} finally {
			lockResource.unlock();
		}
	}

	@Override
	public Resource deleteResource(String key, Long id) {
		lockResource.lock(); // TODO key level lock.
		try {
			if (!memoryResources.containsKey(key)) {
				throw new ConsensusException("Invalid state.");
			}
			Resource resource = memoryResources.get(key);
			if (getState() == State.LEADER) {
				if (id == null || resource.getId().get() != id) {
					logger.info("Fail to update value in concurrent case: " + key + ", " + id);
					return null;
				}
			}
			memoryResources.remove(key); // TODO incr global seq for followers for re-election comparing.
			resource.setDeleted(true);
			logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " deleted resource: " + key);
			if (getState() == State.LEADER) {
				// notify followers
				replicate(resource);
			}
			return resource;
		} finally {
			lockResource.unlock();
		}
	}

	private void replicate(Resource resource) {
		new TaskReplicate(this, resource).execute();
	}

	@Override
	public void replicate(NodeAddress addr, Resource resource) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensusL2/resource")//
					.build();
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		HttpPost httpPost = new HttpPost(uri);
		List<NameValuePair> parameters = new ArrayList<>();
		parameters.add(new BasicNameValuePair("key", resource.getKey()));
		parameters.add(new BasicNameValuePair("value", resource.getValue()));
		parameters.add(new BasicNameValuePair("delete", String.valueOf(resource.isDeleted())));
		parameters.add(new BasicNameValuePair("leader", "false"));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		try (CloseableHttpResponse resp = httpclient.execute(httpPost)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent());
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unused")
			Resource result = mapper.readValue(body, Resource.class);
		} catch (Exception e) {
			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
		}
	}

	@Override
	public void updateRemoteState(NodeAddress addr) {
		State state = getRemoteState(addr);
		states.put(addr, state);
	}

	@Override
	public List<NodeAddress> getCluster() {
		return appConfig.clusterL2();
	}

	@Override
	public boolean strongConsist() {
		return false;
	}

	@Override
	public Map<NodeAddress, State> geStates() {
		return states;
	}
}
