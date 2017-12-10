package projectm.consensus.client;

import org.junit.Assert;
import org.junit.Test;

import projectm.consensus.client.impl.DefaultLockServiceFactory;

public class LockTest {

	@Test
	public void testLock() {
		NodeAddress node1 = new NodeAddress("127.0.0.1", 8080);
		NodeAddress node2 = new NodeAddress("127.0.0.1", 8081);
		NodeAddress node3 = new NodeAddress("127.0.0.1", 8082);
		LockService lockService = new DefaultLockServiceFactory()//
				.buildLockService(node1, node2, node3);
		Lock lock = lockService.getLock("testLock");
		try {
			lock.lock();
			System.out.println("Get lock " + lock);
			try {
				lock.lock();
				Assert.fail("Cannot get same lock twice.");
			} catch (Exception e) {
				// fine
			}
		} finally {
			lock.unlock();
			System.out.println("Free lock " + lock);
		}
		// test get lock again.
		try {
			lock.lock();
		} finally {
			lock.unlock();
		}
	}
}
