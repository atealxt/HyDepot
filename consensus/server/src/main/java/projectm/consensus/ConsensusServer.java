package projectm.consensus;

import projectm.consensus.service.NotifyResult;
import projectm.consensus.service.Resource;

public interface ConsensusServer {

	void startUp();

	void tearDown();

	State transition(State state);

	State getState();

	NodeAddress getLeaderAddress();

	NotifyResult notify(NodeAddress nodeAddress, State state);

	Resource getResource(String key);

	Resource addResource(String key, String value);
}
