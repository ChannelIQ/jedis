package redis.clients.jedis;

import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.util.KeyedPool;

public class JedisKeyedPool extends KeyedPool<String, Jedis> {

	private static final Logger logger = LoggerFactory
			.getLogger(Sentinel.class);

	public JedisKeyedPool(final Sentinel sentinel) {
		this(new Config(), sentinel);
	}

	public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel) {
		super(poolConfig, new JedisServiceConnectionFactory(sentinel));

		logger.debug("Pool config (testOnBorrow) - " + poolConfig.testOnBorrow);
		logger.debug("Pool config (maxTotal) - " + poolConfig.maxTotal);
	}

	/*
	 * public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel) {
	 * this(poolConfig, sentinel, Protocol.DEFAULT_PORT,
	 * Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE); }
	 * 
	 * public JedisKeyedPool(final Sentinel sentinel, final int port) { this(new
	 * Config(), sentinel, port, Protocol.DEFAULT_TIMEOUT, null,
	 * Protocol.DEFAULT_DATABASE); }
	 * 
	 * public JedisKeyedPool(final String host, final Sentinel sentinel) { URI
	 * uri = URI.create(host); if (uri.getScheme() != null &&
	 * uri.getScheme().equals("redis")) { String h = uri.getHost(); int port =
	 * uri.getPort(); String password = uri.getUserInfo().split(":", 2)[1]; int
	 * database = Integer.parseInt(uri.getPath().split("/", 2)[1]);
	 * this.internalPool = new GenericKeyedObjectPool( new
	 * JedisServiceConnectionFactory(sentinel, Protocol.DEFAULT_TIMEOUT,
	 * password, database), new Config()); } else { this.internalPool = new
	 * GenericKeyedObjectPool( new JedisServiceConnectionFactory(sentinel,
	 * Protocol.DEFAULT_TIMEOUT, null, Protocol.DEFAULT_DATABASE), new
	 * Config()); } }
	 * 
	 * public JedisKeyedPool(final URI uri, final Sentinel sentinel) { String h
	 * = uri.getHost(); int port = uri.getPort(); String password =
	 * uri.getUserInfo().split(":", 2)[1]; int database =
	 * Integer.parseInt(uri.getPath().split("/", 2)[1]); this.internalPool = new
	 * GenericKeyedObjectPool( new JedisServiceConnectionFactory(sentinel,
	 * Protocol.DEFAULT_TIMEOUT, password, database), new Config()); }
	 * 
	 * public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel,
	 * int timeout, final String password) { this(poolConfig, sentinel, timeout,
	 * password, Protocol.DEFAULT_DATABASE); }
	 * 
	 * public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel )
	 * { this(poolConfig, sentinel, Protocol.DEFAULT_TIMEOUT, null,
	 * Protocol.DEFAULT_DATABASE); }
	 * 
	 * public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel,
	 * final int timeout) { this(poolConfig, sentinel, timeout, null,
	 * Protocol.DEFAULT_DATABASE); }
	 * 
	 * public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel,
	 * int timeout, final String password, final int database) {
	 * super(poolConfig, new JedisServiceConnectionFactory(sentinel, timeout,
	 * password, database)); }
	 */
}
