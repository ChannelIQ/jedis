package redis.clients.util;

import java.util.NoSuchElementException;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

public abstract class KeyedPool<K, T> {
	protected GenericKeyedObjectPool internalPool;

	protected KeyedPool() {
		this.internalPool = null;
	}

	public KeyedPool(final GenericKeyedObjectPool.Config poolConfig,
			KeyedPoolableObjectFactory factory) {
		this.internalPool = new GenericKeyedObjectPool(factory, poolConfig);
	}

	@SuppressWarnings("unchecked")
	public T getResource(String key) {
		try {
			return (T) internalPool.borrowObject(key);
		} catch (NoSuchElementException nsee) {
			throw nsee;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new JedisConnectionException(
					"Could not get a resource from the pool", e);
		}
	}

	public void returnResource(final String key, final T resource) {
		returnResourceObject(key, resource);
	}

	public void returnResourceObject(final String key, final Object resource) {
		try {
			internalPool.returnObject(key, resource);
		} catch (Exception e) {
			throw new JedisException(
					"Could not return the resource to the pool", e);
		}
	}

	public void returnBrokenResource(final String key, final T resource) {
		returnBrokenResourceObject(key, resource);
	}

	public void returnBrokenResourceObject(final String key,
			final Object resource) {
		try {
			internalPool.invalidateObject(key, resource);
		} catch (Exception e) {
			throw new JedisException(
					"Could not return the resource to the pool", e);
		}
	}

	public void destory() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new JedisException("Could not destroy the pool", e);
		}
	}
}
