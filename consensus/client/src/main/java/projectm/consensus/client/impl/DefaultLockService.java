package projectm.consensus.client.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectm.consensus.client.ConsensusException;
import projectm.consensus.client.Lock;
import projectm.consensus.client.LockService;
import projectm.consensus.client.NodeAddress;

public class DefaultLockService implements LockService {

	private Map<NodeAddress, State> cluster = new HashMap<>();
	private NodeAddress leader;

	public DefaultLockService(NodeAddress[] nodes) {
		for (NodeAddress node : nodes) {
			State state = getState(node);
			cluster.put(node, state);
			if (state == State.LEADER) {
				leader = node;
			}
		}
	}

	private State getState(NodeAddress addr) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http").setHost(addr.getIp())//
					.setPort(addr.getPort()).setPath("/api/consensus/state")//
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		HttpGet httpGet = new HttpGet(uri);
		try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
			HttpEntity entity = resp.getEntity();
			String body = IOUtils.toString(entity.getContent());
			ObjectMapper mapper = new ObjectMapper();
			State result = mapper.readValue(body, State.class);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Lock getLock(String key) throws ConsensusException {
		return new DefaultLock(this, key);
	}

	public NodeAddress getLeader() {
		return leader;
	}
}
