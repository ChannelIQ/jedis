package redis.clients.jedis;

public class SentinelServiceConfig {
	private final String serviceName;
	private String host;
	private int port;
	private final int timeout;
	private final String password;
	private final int database;

	public SentinelServiceConfig(String serviceName) {
		this(serviceName, Protocol.DEFAULT_TIMEOUT);
	}

	public SentinelServiceConfig(String serviceName, int timeout) {
		this(serviceName, timeout, "");
	}

	public SentinelServiceConfig(String serviceName, String password) {
		this(serviceName, Protocol.DEFAULT_TIMEOUT, password);
	}

	public SentinelServiceConfig(String serviceName, int timeout,
			String password) {
		this(serviceName, timeout, password, Protocol.DEFAULT_DATABASE);
	}

	public SentinelServiceConfig(String serviceName, int timeout,
			String password, int database) {
		this.serviceName = serviceName;
		this.timeout = timeout;
		this.password = password;
		this.database = database;

		// Default these to empty
		this.host = "";
		this.port = 0;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setHost(String h) {
		this.host = h;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int p) {
		this.port = p;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getPassword() {
		return password;
	}

	public int getDatabase() {
		return database;
	}

}
