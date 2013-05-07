package redis.clients.jedis;

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * PoolableObjectFactory custom impl.
 */
public class JedisConnectionFactory extends BasePoolableObjectFactory {
	private final String host;
	private final int port;
	private final int timeout;
	private final String password;
	private final int database;

	public JedisConnectionFactory(final String host, final int port,
			final int timeout, final String password, final int database) {
		super();
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		this.password = password;
		this.database = database;
	}

	@Override
	public Object makeObject() throws Exception {
		return makeConnection(this.host, this.port, this.timeout);
	}

	protected Jedis makeConnection(String h, int p) {
		return makeConnection(h, p, timeout);
	}

	protected Jedis makeConnection(String h, int p, int t) {
		final Jedis jedis = new Jedis(h, p, t);

		jedis.connect();
		if (null != this.password) {
			jedis.auth(this.password);
		}
		if (database != 0) {
			jedis.select(database);
		}

		return jedis;
	}

	@Override
	public void destroyObject(final Object obj) throws Exception {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			if (jedis.isConnected()) {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {
					}
					jedis.disconnect();
				} catch (Exception e) {

				}
			}
		}
	}

	@Override
	public boolean validateObject(final Object obj) {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			try {
				return jedis.isConnected() && jedis.ping().equals("PONG");
			} catch (final Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
}
