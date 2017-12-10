package projectm.consensus.client.impl;

import projectm.consensus.client.LockService;
import projectm.consensus.client.LockServiceFactory;
import projectm.consensus.client.NodeAddress;

public class DefaultLockServiceFactory implements LockServiceFactory {

	@Override
	public LockService buildLockService(NodeAddress... nodes) {
		return new DefaultLockService(nodes);
	}

}
