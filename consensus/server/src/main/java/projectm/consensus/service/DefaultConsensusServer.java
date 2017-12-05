package projectm.consensus.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectm.consensus.ApplicationConfig;
import projectm.consensus.ConsensusServer;
import projectm.consensus.NodeAddress;
import projectm.consensus.State;

@Service
public class DefaultConsensusServer implements ConsensusServer {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private ApplicationConfig appConfig;
	private State state = State.INACTIVE;
	private Map<NodeAddress, State> states = new HashMap<>();
	private Map<NodeAddress, Long> candidateTenancy = new HashMap<>();

	@Override
	public void startUp() {
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
			if (noState(State.LEADER)) {
				transition(State.CANDIDATE);
			}
			break;
		case CANDIDATE:
			this.state = state;
			try {
				Thread.sleep(500 + new Random().nextInt(5000));
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			ping();
			int min = ticketstoWin(), voted = 0;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				boolean accept = notifyRemote(addr, state).isAccept();
				if (accept) {
					voted++;
				}
			}
			if (voted >= min) {
				transition(State.LEADER);
			} else if (noState(State.LEADER)) {
				transition(State.FOLLOWER);
			}
			break;
		case LEADER:
			this.state = state;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				notifyRemote(addr, state);
			}
			break;
		default:
			break;
		}

		return getState();
	}

	private int ticketstoWin() {
		int n = 0;
		for (Entry<NodeAddress, State> state : states.entrySet()) {
			if (state.getValue() != State.INACTIVE) {
				n++;
			}
		}
		return n;
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

	private NotifyResult notifyRemote(NodeAddress addr, State state) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensus/notify")//
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
//			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
			return new NotifyResult(false, State.INACTIVE);
		}
	}

	private State getRemoteState(NodeAddress addr) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensus/state")//
					.build();
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		HttpGet httpGet = new HttpGet(uri);
		try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent()).replace("\"", "");
//			logger.info(addr + " resp is " + body);
			return State.parse(body);
		} catch (Exception e) {
//			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
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
		for (NodeAddress addr : appConfig.cluster()) {
			State state = getRemoteState(addr);
			states.put(addr, state);
		}
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
						logger.info("No leader in cluster, start election...");
						transition(State.CANDIDATE);
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	// TODO lock service
}
