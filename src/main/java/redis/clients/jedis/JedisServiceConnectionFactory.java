package redis.clients.jedis;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PoolableObjectFactory custom impl.
 */
public class JedisServiceConnectionFactory extends
		BaseKeyedPoolableObjectFactory {
	private final Sentinel sentinel;

	private static final Logger logger = LoggerFactory
			.getLogger(JedisServiceConnectionFactory.class);

	public JedisServiceConnectionFactory(final Sentinel sentinel) {
		this.sentinel = sentinel;
	}

	@Override
	public Object makeObject(final Object key) throws Exception {
		try {
			return sentinel.findMaster((String) key);
		} catch (Exception ex) {
			logger.error("Error making Jedis object (makeObject): "
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
						logger.error("Unable to quit the jedis instance :"
								+ e.getMessage());
					}
					jedis.disconnect();
				} catch (Exception e) {
					logger.error("Unable to disconnect the jedis instance :"
							+ e.getMessage());
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
				logger.info("Validating Jedis - " + jedis.getClient().getHost()
						+ ":" + jedis.getClient().getPort());

				if (jedis.isConnected() && jedis.ping().equals("PONG")
						&& sentinel.validate(key.toString(), jedis))
					return true;
			} catch (final Exception e) {
				logger.error("Exception validating - " + e.getMessage());
				return false;
			}
		}

		return false;
	}
}
