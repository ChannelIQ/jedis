package redis.clients.jedis.exceptions;

public class JedisSentinelConnectionException extends JedisConnectionException {
	private static final long serialVersionUID = 7884516992155018004L;

	public JedisSentinelConnectionException(String message) {
		super(message);
	}

	public JedisSentinelConnectionException(Throwable cause) {
		super(cause);
	}

	public JedisSentinelConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
