package redis.clients.jedis;

import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;

import redis.clients.util.KeyedPool;

public class JedisKeyedPool extends KeyedPool<String, Jedis> {

	public JedisKeyedPool(final Sentinel sentinel) {
		this(new Config(), sentinel);
	}

	public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel) {
		this(poolConfig, sentinel, null);
		System.out.println("Pool config (testOnBorrow) - "
				+ poolConfig.testOnBorrow);
		System.out.println("Pool config (maxTotal) - " + poolConfig.maxTotal);
	}

	public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel,
			final String password) {
		super(poolConfig, new JedisServiceConnectionFactory(sentinel,
				Protocol.DEFAULT_TIMEOUT, password, Protocol.DEFAULT_DATABASE));
	}

	public JedisKeyedPool(final Config poolConfig, final Sentinel sentinel,
			final int timeout, final String password, final int database) {
		super(poolConfig, new JedisServiceConnectionFactory(sentinel, timeout,
				password, database));
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
