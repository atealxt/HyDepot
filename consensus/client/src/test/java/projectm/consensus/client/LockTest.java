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

	@Test
	public void testLockPerformance() {
		NodeAddress node1 = new NodeAddress("127.0.0.1", 8080);
		NodeAddress node2 = new NodeAddress("127.0.0.1", 8081);
		NodeAddress node3 = new NodeAddress("127.0.0.1", 8082);
		long start = System.currentTimeMillis();
		LockService lockService = new DefaultLockServiceFactory()//
				.buildLockService(node1, node2, node3);
		long timeGetLockSerivce = System.currentTimeMillis() - start;
		System.out.println("Init lockService spent: " + timeGetLockSerivce + "ms.");
		long lockTime = 0, unlockTime = 0;
		int cnt = 1000;
		for (int i = 0; i < cnt; i++) {
			Lock lock = lockService.getLock("testLock" + i);
			try {
				long startLock = System.currentTimeMillis();
				lock.lock();
				lockTime += System.currentTimeMillis() - startLock;
			} finally {
				long startUnlock = System.currentTimeMillis();
				lock.unlock();
				unlockTime += System.currentTimeMillis() - startUnlock;
			}
		}
		System.out.println("Loop " + cnt + " time, lock spent " + lockTime + "ms, unlock spent " + unlockTime + "ms.");
	}
}
