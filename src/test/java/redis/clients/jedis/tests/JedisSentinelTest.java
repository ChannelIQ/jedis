package redis.clients.jedis.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisKeyedPool;
import redis.clients.jedis.Sentinel;

public class JedisSentinelTest {
	private static final String MASTER_NAME = "mymaster";
	private static final String MASTER2_NAME = "mymaster2";
	private static final int MASTER_COUNT = 2;
	private static final List<Map<String, String>> sentinels;

	static {
		sentinels = new ArrayList<Map<String, String>>();
		sentinels.add(getSentinel("localhost", "16379"));
		sentinels.add(getSentinel("localhost", "26379"));
		sentinels.add(getSentinel("localhost", "26380"));
		sentinels.add(getSentinel("localhost", "26381"));
	}

	private static Map<String, String> getSentinel(String ip, String port) {
		final Map<String, String> sentinel = new HashMap<String, String>();
		sentinel.put("ip", ip);
		sentinel.put("port", port);
		return sentinel;
	}

	@Before
	public void setup() throws InterruptedException {
		Thread.sleep(5000);
	}

	@After
	public void clear() {
	}

	@Test
	public void sentinel() {
		Jedis j = new Jedis("localhost", 26379);
		List<Map<String, String>> masters = j.sentinelMasters();

		assertEquals(MASTER_COUNT, masters.size());

		/*
		 * final String masterName = masters.get(0).get("name"); //
		 * assertEquals(MASTER_NAME, masterName);
		 * 
		 * List<String> masterHostAndPort = j
		 * .sentinelGetMasterAddrByName(masterName); assertEquals("127.0.0.1",
		 * masterHostAndPort.get(0)); assertEquals("6379",
		 * masterHostAndPort.get(1));
		 */

		List<Map<String, String>> slaves = j.sentinelSlaves(MASTER_NAME);
		assertEquals("6379", slaves.get(0).get("master-port"));

		List<? extends Object> isMasterDownByAddr = j
				.sentinelIsMasterDownByAddr("127.0.0.1", 6379);
		assertEquals(Long.valueOf(0), isMasterDownByAddr.get(0));
		assertFalse("?".equals(isMasterDownByAddr.get(1)));

		isMasterDownByAddr = j.sentinelIsMasterDownByAddr("127.0.0.1", 1);
		assertEquals(Long.valueOf(0), isMasterDownByAddr.get(0));
		assertTrue("?".equals(isMasterDownByAddr.get(1)));

		// DO NOT RE-RUN TEST TOO FAST, RESET TAKES SOME TIME TO... RESET
		assertEquals(Long.valueOf(1), j.sentinelReset(MASTER_NAME));
		assertEquals(Long.valueOf(0), j.sentinelReset("woof" + MASTER_NAME));

	}

	@Test
	public void sentinelWhenOverMaxConnectionReuseFromPool()
			throws InterruptedException {
		Config poolConfig = new Config();
		poolConfig.maxTotal = 1;
		poolConfig.maxIdle = 1;
		poolConfig.maxActive = 1;
		poolConfig.whenExhaustedAction = 0;
		poolConfig.testOnBorrow = true;

		Sentinel sentinel = new Sentinel(sentinels);
		JedisKeyedPool pool = new JedisKeyedPool(poolConfig, sentinel,
				"foobared");

		int i = 0;
		while (i < 5) {
			// mymaster
			Jedis j = pool.getResource(MASTER_NAME);
			assertEquals("127.0.0.1", j.getClient().getHost());
			assertEquals(6379, j.getClient().getPort());
			pool.returnResource(MASTER_NAME, j);

			i++;
			Thread.sleep(1000);
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void sentinelWhenMaximumTotalExceededThrowException()
			throws NoSuchElementException {
		Config poolConfig = new Config();
		poolConfig.maxTotal = 1;
		poolConfig.maxIdle = 1;
		poolConfig.maxActive = 1;
		poolConfig.whenExhaustedAction = 0;
		poolConfig.testOnBorrow = true;

		Sentinel sentinel = new Sentinel(sentinels);
		JedisKeyedPool pool = new JedisKeyedPool(poolConfig, sentinel,
				"foobared");

		// mymaster
		Jedis j = pool.getResource(MASTER_NAME);
		assertEquals("127.0.0.1", j.getClient().getHost());
		assertEquals(6379, j.getClient().getPort());

		// mymaster2
		Jedis j2 = pool.getResource(MASTER2_NAME);
		assertEquals("127.0.0.1", j2.getClient().getHost());
		assertEquals(6479, j2.getClient().getPort());
	}

	@Test
	public void testWhenMasterDownFailToSlave() throws InterruptedException {
		Config poolConfig = new Config();
		poolConfig.maxTotal = 1;
		poolConfig.maxIdle = 1;
		poolConfig.maxActive = 1;
		poolConfig.whenExhaustedAction = 0;
		poolConfig.testOnBorrow = true;

		Sentinel sentinel = new Sentinel(sentinels);
		JedisKeyedPool pool = new JedisKeyedPool(poolConfig, sentinel,
				"foobared");

		// mymaster
		Jedis j = pool.getResource(MASTER_NAME);
		assertEquals("127.0.0.1", j.getClient().getHost());
		assertEquals(6379, j.getClient().getPort());

		// Fail a master
		j.shutdown();
		System.out.println("Shutdown of mymaster service - MASTER");

		// Wait until the master is shut down
		try {
			while (j.ping().equals("PONG")) {
				Thread.sleep(2000);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		pool.returnResource(MASTER_NAME, j);
		int passes = 5;

		while (passes > 0) {
			Thread.sleep(3000);
			try {
				System.out.println("Testing connection to new slave");

				// Test failover to the new slave
				Jedis slave = pool.getResource(MASTER_NAME);
				assertEquals("127.0.0.1", slave.getClient().getHost());
				assertEquals(6380, slave.getClient().getPort());
				System.out.println(slave.getClient().getPort());
				passes = 0;
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			} finally {
				passes--;
			}
		}

		System.out.println("Trying a new pool");

		JedisKeyedPool pool2 = new JedisKeyedPool(sentinel);
		// Test failover to the new slave
		Jedis slave = pool2.getResource(MASTER_NAME);
		assertEquals("127.0.0.1", slave.getClient().getHost());
		assertEquals(6380, slave.getClient().getPort());
		System.out.println(slave.getClient().getPort());
	}

	/*
	 * This test may be redundant now?
	 */
	public void sentinelService() throws InterruptedException {
		Config poolConfig = new Config();
		poolConfig.maxTotal = 3;
		poolConfig.maxIdle = 1;
		poolConfig.maxActive = 1;
		poolConfig.whenExhaustedAction = 0;
		poolConfig.testOnBorrow = true;

		Sentinel sentinel = new Sentinel(sentinels);
		JedisKeyedPool pool = new JedisKeyedPool(poolConfig, sentinel,
				"foobared");

		// mymaster
		Jedis j = pool.getResource(MASTER_NAME);
		System.out.println("Got service: " + MASTER_NAME + " for "
				+ j.getClient().getHost());
		assertEquals("127.0.0.1", j.getClient().getHost());
		assertEquals(6379, j.getClient().getPort());

		// mymaster2
		System.out.println("Get another resource");
		Jedis j2 = pool.getResource(MASTER2_NAME);
		System.out.println("Got service: " + MASTER2_NAME + " for "
				+ j2.getClient().getHost());
		assertEquals("127.0.0.1", j2.getClient().getHost());
		assertEquals(6479, j2.getClient().getPort());

		// Fail a master
		j.shutdown();
		System.out.println("Shutdown of mymaster service - MASTER");

		// Wait until the master is shut down
		try {
			while (j.ping().equals("PONG")) {
				Thread.sleep(2000);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		int passes = 5;

		while (passes > 0) {
			Thread.sleep(2000);
			try {
				System.out.println("Testing connection to new slave");

				// Test failover to the new slave
				Jedis slave = pool.getResource(MASTER_NAME);
				assertEquals("127.0.0.1", slave.getClient().getHost());
				assertEquals(6380, slave.getClient().getPort());
				System.out.println(slave.getClient().getPort());
				passes = 0;
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			} finally {
				passes--;
			}
		}

		System.out.println("Trying a new pool");

		JedisKeyedPool pool2 = new JedisKeyedPool(sentinel);
		// Test failover to the new slave
		Jedis slave = pool2.getResource(MASTER_NAME);
		assertEquals("127.0.0.1", slave.getClient().getHost());
		assertEquals(6380, slave.getClient().getPort());
		System.out.println(slave.getClient().getPort());
	}
}
