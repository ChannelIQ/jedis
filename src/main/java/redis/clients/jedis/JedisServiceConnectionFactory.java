package redis.clients.jedis;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * PoolableObjectFactory custom impl.
 */
public class JedisServiceConnectionFactory extends
		BaseKeyedPoolableObjectFactory {
	private final Sentinel sentinel;
	private final int timeout;
	private final String password;
	private final int database;

	public JedisServiceConnectionFactory(final Sentinel sentinel,
			final int defaultTimeout, final String password, final int database) {
		this.sentinel = sentinel;
		this.timeout = defaultTimeout;
		this.password = password;
		this.database = database;
	}

	@Override
	public Object makeObject(final Object key) throws Exception {
		try {
			return sentinel.findMaster((String) key, this.timeout,
					this.password, this.database);
		} catch (Exception ex) {
			System.out.println("Error making Jedis object (makeObject): "
					+ ex.getMessage());
			throw ex;
		} finally {

		}
	}

	@Override
	public void destroyObject(final Object key, final Object obj)
			throws Exception {
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			if (jedis.isConnected()) {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {
						// Log
					}
					jedis.disconnect();
				} catch (Exception e) {
					// Log
				}
			}
		}
	}

	@Override
	public boolean validateObject(final Object key, final Object obj) {
		// TODO: Verify this is a good approach
		if (obj instanceof Jedis) {
			final Jedis jedis = (Jedis) obj;
			try {
				System.out.println("Validating Jedis - "
						+ jedis.getClient().getHost() + ":"
						+ jedis.getClient().getPort());

				if (jedis.isConnected() && jedis.ping().equals("PONG")
						&& sentinel.validate(key.toString(), jedis))
					return true;
			} catch (final Exception e) {
				System.out.println("Exception validating - " + e.getMessage());
				return false;
			}
		}

		return false;
	}
}
