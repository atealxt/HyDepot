package projectm.consensus;

import projectm.consensus.service.NotifyResult;

public interface ConsensusServer {

	void startUp();

	void tearDown();

	State transition(State state);

	State getState();

	NotifyResult notify(NodeAddress nodeAddress, State state);
}
