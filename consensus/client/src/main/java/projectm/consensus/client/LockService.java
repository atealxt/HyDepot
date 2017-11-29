package projectm.consensus.client;

public interface LockService {

	Lock getLock(String key) throws ConsensusException;
}
