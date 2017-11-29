package projectm.consensus.client;

public interface LockServiceFactory {

	LockService buildLockService(NodeAddress... nodes);
}
