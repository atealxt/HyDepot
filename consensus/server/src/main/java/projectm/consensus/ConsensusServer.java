package projectm.consensus;

public interface ConsensusServer {

	void startUp();

	void tearDown();

	State transition(State state);

	State getState();
}
