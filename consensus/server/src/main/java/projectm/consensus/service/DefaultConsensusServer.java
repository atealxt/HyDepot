package projectm.consensus.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
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

	@Override
	public void startUp() {
		for (NodeAddress addr : appConfig.cluster()) {
			State state = getRemoteState(addr);
			states.put(addr, state);
		}
		transition(State.RESERVE);
	}

	@Override
	public void tearDown() {
	}

	@Override
	public State transition(State state) {
		// TODO Auto-generated method stub
		// RESERVE->FOLLOWER->CANDIDATE->LEADER
		logger.info(appConfig.getIp() + ":" + appConfig.getPort() + " is transiting to " + state);

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
			if (noLeader()) {
				transition(State.CANDIDATE);
			}
			break;
		case CANDIDATE:
			try {
				Thread.sleep(500 + new Random().nextInt(5000));
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			int min = ticketstoWin(), voted = 0;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				boolean accept = notifyRemote(addr, state).isAccept();
				if (accept) {
					voted++;
				}
			}
			if (voted >= min) {
				this.state = state;
				transition(State.LEADER);
			} else if (noLeader()) {
				transition(State.CANDIDATE);
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

	private boolean noLeader() {
		if (this.state == State.LEADER) {
			return false;
		}
		boolean hasLeader = false;
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (entry.getValue() == State.LEADER) {
				hasLeader = true;
			}
		}
		return !hasLeader;
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
//			logger.info(addr + " resp is " + body);
			ObjectMapper mapper = new ObjectMapper();
			NotifyResult result = mapper.readValue(body, NotifyResult.class);
			if (!result.isAccept()) {
				logger.warn(addr + " not accept my state" + state);
			} else {
				states.put(addr, result.getState());
			}
			return result;
		} catch (Exception e) {
			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
			return new NotifyResult(false, State.INACTIVE); // TODO need to skip this vote.
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
			logger.info(addr + " resp is " + body);
			return State.parse(body);
		} catch (Exception e) {
			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
			return State.INACTIVE;
		}
	}


	@Override
	public NotifyResult notify(NodeAddress nodeAddress, State state) {

		logger.info(nodeAddress + " transit to " + state);

		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (!entry.getKey().equals(nodeAddress)) {
				continue;
			}
			switch (state) {
			case RESERVE:
				entry.setValue(state);
				break;
			case FOLLOWER:
				entry.setValue(state);
				break;
			case CANDIDATE:
				if (!noLeader()) {
					return new NotifyResult(false, getState());
				}
				// TODO remember the current CANDIDATE and not accept more for a while
				entry.setValue(state);
				break;
			case LEADER:
				entry.setValue(state);
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
}
