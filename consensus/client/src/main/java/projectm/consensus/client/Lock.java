package projectm.consensus.client;

public interface Lock {

	void lock() throws LockException;

	void unlock() throws LockException;
}
