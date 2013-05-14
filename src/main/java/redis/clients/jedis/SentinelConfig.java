package redis.clients.jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SentinelConfig {
	private final ArrayList<SentinelInstance> instances = new ArrayList<SentinelInstance>();

	public void addSentinelInstance(String host) {
		addSentinelInstance(host, Protocol.DEFAULT_PORT);
	}

	public void addSentinelInstance(String host, int port) {
		addSentinelInstance(host, port, Protocol.DEFAULT_TIMEOUT);
	}

	public void addSentinelInstance(String host, int port, int timeout) {
		addSentinelInstance(host, port, timeout, "");
	}

	public void addSentinelInstance(String host, int port, int timeout,
			String password) {
		addSentinelInstance(host, port, timeout, "", Protocol.DEFAULT_DATABASE);
	}

	public void addSentinelInstance(String host, int port, int timeout,
			String password, int database) {
		// Ensure we don't have an instance, and if we do, update it
		for (SentinelInstance instance : instances) {
			if (instance.getHost().equals(host) && instance.getPort() == port) {
				instance.setTimeout(timeout);
				instance.setPassword(password);
				instance.setDatabase(database);
				return;
			}
		}

		// Otherwise, add a new instance
		instances.add(new SentinelInstance(host, port, timeout, password,
				Protocol.DEFAULT_DATABASE));
	}

	public List<SentinelInstance> getSentinelInstances() {
		return instances;
	}

	public void electLeader(SentinelInstance leaderInstance) {
		try {
			int idx = instances.indexOf(leaderInstance);

			Collections.swap(instances, 0, idx);
		} catch (Exception ex) {

		}
	}

	public void deprioritizeSentinelInstance(
			SentinelInstance lowestPriorityInstance) {
		try {
			int idx = instances.indexOf(lowestPriorityInstance);

			Collections.swap(instances, instances.size() - 1, idx);
		} catch (Exception ex) {

		}
	}

	public class SentinelInstance {
		private final String host;
		private final int port;
		private int timeout;
		private String password;
		private int database;

		public SentinelInstance(String host, int port, int timeout,
				String password, int database) {
			this.host = host;
			this.port = port;
			this.timeout = timeout;
			this.password = password;
			this.database = database;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public void setTimeout(int t) {
			this.timeout = t;
		}

		public int getTimeout() {
			return timeout;
		}

		public void setPassword(String p) {
			this.password = p;
		}

		public String getPassword() {
			return password;
		}

		public void setDatabase(int d) {
			this.database = d;
		}

		public int getDatabase() {
			return database;
		}
	}
}
