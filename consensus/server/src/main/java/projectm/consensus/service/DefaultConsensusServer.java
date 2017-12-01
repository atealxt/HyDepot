package projectm.consensus.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
			states.put(addr, State.INACTIVE);
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

		switch (state) {
		case RESERVE:
			this.state = state;
			for (Entry<NodeAddress, State> entry : states.entrySet()) {
				NodeAddress addr = entry.getKey();
				notifyRemote(addr, state);
			}
			break;
		case FOLLOWER:

			break;
		case CANDIDATE:

			break;
		case LEADER:

			break;

		default:
			break;
		}

		return getState();
	}

	private void notifyRemote(NodeAddress addr, State state) {
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
			e.printStackTrace();
		}
		HttpGet httpGet = new HttpGet(uri);
		try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent()).replace("\"", "");
			logger.debug("body: " + body);
			State remoteState = State.parse(body);
			states.put(addr, remoteState);
			logger.info("Updated " + addr + " state to " + remoteState);
		} catch (Exception e) {
			logger.info("Failed to talk to " + addr + ": " + e.getMessage());
		}
	}

	@Override
	public State notify(NodeAddress nodeAddress, State state) {
		for (Entry<NodeAddress, State> entry : states.entrySet()) {
			if (!entry.getKey().equals(nodeAddress)) {
				continue;
			}
			switch (state) {
			case RESERVE:
				entry.setValue(state);
				break;
			case FOLLOWER:

				break;
			case CANDIDATE:

				break;
			case LEADER:

				break;

			default:
				break;
			}
			break;
		}
		return getState();
	}

	@Override
	public State getState() {
		return state;
	}
}
