package redis.clients.jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.exceptions.JedisSentinelConnectionException;

public class Sentinel {
	private final ArrayList<Map<String, String>> sentinels;
	private final Map<String, HostAndPort> masterInstances = new HashMap<String, HostAndPort>();
	private static final int SENTINEL_TIMEOUT = 2;

	public Sentinel(List<Map<String, String>> sentinels) {
		this.sentinels = new ArrayList<Map<String, String>>(sentinels);
	}

	public Jedis findMaster(String masterName) {
		return findMaster(masterName, Protocol.DEFAULT_TIMEOUT, null,
				Protocol.DEFAULT_DATABASE);
	}

	public Jedis findMaster(String masterName, int timeout, String password,
			int database) {
		String lastIp = "";
		Integer lastPort = 0;

		for (Map<String, String> item : sentinels) {
			String ip = item.get("ip");
			Integer port = Integer.parseInt(item.get("port"));

			final Jedis client = new Jedis(ip, port, SENTINEL_TIMEOUT);

			try {
				client.ping();
				List<String> master = client
						.sentinelGetMasterAddrByName(masterName);

				System.out.println("Found master for " + masterName + " at "
						+ master.get(0) + ":" + master.get(1)
						+ " from Sentinel (" + ip + ":" + port + ")");

				try {
					if (master != null) {
						final String masterIp = master.get(0);
						final int masterPort = Integer.parseInt(master.get(1));

						// If we already tried this ip/port combination, skip
						// attempting again
						if (lastIp.equals(masterIp)
								&& lastPort.equals(masterPort))
							continue;

						// Save last seen values for optimization
						lastIp = masterIp;
						lastPort = masterPort;
						final Jedis masterClient = new Jedis(masterIp,
								masterPort, timeout);

						masterClient.connect();
						if (null != password) {
							masterClient.auth(password);
						}
						if (database != 0) {
							masterClient.select(database);
						}

						electLeader(item);

						setDefaultInstance(masterName, masterIp, masterPort);

						return masterClient;
					}
				} catch (Exception e) {
					// TODO: Add Logging
					System.out.println("Master found on sentinel, "
							+ item.get("ip") + ":" + item.get("port")
							+ " has thrown an exception - " + e.getMessage());
					continue;
				}
			} catch (Exception e) {
				// TODO: Add Logging
				System.out.println("Sentinel (" + item.get("ip") + ":"
						+ item.get("port") + ") has thrown an exception - "
						+ e.getMessage());
				deprioritizeSentinel(item);
				continue;
			}
		}

		// if we get to this point, it means that all the sentinels failed
		throw new JedisSentinelConnectionException(
				"All sentinels failed to connect.");
	}

	private void setDefaultInstance(String masterName, String masterIp,
			int masterPort) {
		masterInstances.put(masterName, new HostAndPort(masterIp, masterPort));
	}

	public void electLeader(Map<String, String> leaderInstance) {
		int idx = sentinels.indexOf(leaderInstance);

		Collections.swap(sentinels, 0, idx);
	}

	public void deprioritizeSentinel(Map<String, String> lowestPriorityInstance) {
		int idx = sentinels.indexOf(lowestPriorityInstance);

		Collections.swap(sentinels, sentinels.size() - 1, idx);
	}

	public void refreshListOfSentinels(String masterName) {
		// TODO: Should this overwrite or combine?
		try {
			final Jedis jedis = findMaster(masterName);

			List<Map<String, String>> _newSentinels = jedis
					.sentinelSentinels(masterName);

			for (Map<String, String> item : _newSentinels) {
				if (!sentinels.contains(item)) {
					sentinels.add(item);
				}
			}
		} catch (Exception ex) {
			// TODO: Add logging
		}
	}

	/*
	 * Lazy Attempt to determine if we are using the right master
	 */
	public boolean validate(String masterName, Jedis jedis) {
		if (!jedis.getClient().getHost()
				.equals(masterInstances.get(masterName).getHost())
				|| jedis.getClient().getPort() != (masterInstances
						.get(masterName).getPort())) {
			System.out
					.println("Validation found that "
							+ jedis.getClient().getHost()
							+ ":"
							+ jedis.getClient().getPort()
							+ " is not the same as the expected master determined by Sentinel at "
							+ masterInstances.get(masterName).getHost() + ":"
							+ masterInstances.get(masterName).getPort());
			return false;
		}
		return true;
	}

	private class HostAndPort {
		private String host;
		private Integer port;

		public HostAndPort(String h, Integer p) {
			this.host = h;
			this.port = p;
		}

		private String getHost() {
			return host;
		}

		private void setHost(String host) {
			this.host = host;
		}

		private Integer getPort() {
			return port;
		}

		private void setPort(Integer port) {
			this.port = port;
		}

	}
}