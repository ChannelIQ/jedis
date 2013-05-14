package redis.clients.jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.SentinelConfig.SentinelInstance;
import redis.clients.jedis.exceptions.JedisSentinelConnectionException;

public class Sentinel {
	private final SentinelConfig config;
	private final Map<String, SentinelServiceConfig> services;

	private static final Logger logger = LoggerFactory
			.getLogger(Sentinel.class);

	public Sentinel(SentinelConfig conf, SentinelServiceConfig... svcs) {
		this.config = conf;

		this.services = new HashMap<String, SentinelServiceConfig>();
		for (SentinelServiceConfig service : svcs) {
			this.services.put(service.getServiceName(), service);
		}
	}

	public Jedis findMaster(String masterName) throws Exception {
		SentinelServiceConfig serviceConfig;
		if (services.containsKey(masterName)) {
			serviceConfig = services.get(masterName);
		} else {
			// Allow JIT initialization of services, but the assumption is that
			// the services have all default values (other than host and port
			// which are supplied by Sentinel)
			serviceConfig = new SentinelServiceConfig(masterName);
		}

		String lastIp = "";
		Integer lastPort = 0;

		for (SentinelInstance instance : config.getSentinelInstances()) {
			final Jedis client = new Jedis(instance.getHost(),
					instance.getPort(), instance.getTimeout());

			try {
				if (isSentinelInstanceValid(instance, client)) {

					List<String> master = client
							.sentinelGetMasterAddrByName(masterName);

					if (master != null) {
						try {
							final String masterIp = master.get(0);
							final int masterPort = Integer.parseInt(master
									.get(1));

							logger.debug("Found master for " + masterName
									+ " at " + masterIp + ":" + masterPort
									+ " from Sentinel (" + instance.getHost()
									+ ":" + instance.getPort() + ")");

							// If we already tried this ip/port combination,
							// skip
							// attempting again. This means that each Sentinel
							// is
							// returning the same ip/port combination, even
							// though
							// it failed to connect during the first attempt
							if (lastIp.equals(masterIp)
									&& lastPort.equals(masterPort))
								continue;

							// Save last seen values for optimization
							lastIp = masterIp;
							lastPort = masterPort;
							final Jedis masterClient = new Jedis(masterIp,
									masterPort, serviceConfig.getTimeout());

							masterClient.connect();
							if (serviceConfig.getPassword() != null
									&& !serviceConfig.getPassword().equals("")) {
								masterClient.auth(serviceConfig.getPassword());
							}
							if (serviceConfig.getDatabase() != 0) {
								masterClient
										.select(serviceConfig.getDatabase());
							}

							config.electLeader(instance);

							setDefaultMaster(masterName, masterIp, masterPort);

							return masterClient;
						} catch (Exception e) {
							logger.warn("Master found on sentinel, "
									+ instance.getHost() + ":"
									+ instance.getPort()
									+ " has thrown an exception - "
									+ e.getMessage());
							continue;
						}
					} else {
						logger.debug("Skipping sentinel instance because it either did not connect or respond to ping");
					}
				}
			} catch (Exception e) {
				logger.warn("Sentinel (" + instance.getHost() + ":"
						+ instance.getPort() + ") has thrown an exception - "
						+ e.getMessage());
				config.deprioritizeSentinelInstance(instance);
				continue;
			}
		}

		// if we get to this point, it means that all the sentinels failed
		throw new JedisSentinelConnectionException(
				"All sentinels failed to connect.");
	}

	private boolean isSentinelInstanceValid(SentinelInstance instance,
			final Jedis client) {
		client.connect();
		if (instance.getPassword() != null
				&& !instance.getPassword().equals("")) {
			client.auth(instance.getPassword());
		}
		if (instance.getDatabase() != 0) {
			client.select(instance.getDatabase());
		}

		return client.isConnected() && client.ping().equals("PONG");
	}

	private void setDefaultMaster(String masterName, String masterHost,
			int masterPort) {
		if (services.containsKey(masterName)) {
			// Update the service reference, with the current Master Host/Port
			SentinelServiceConfig service = services.get(masterName);
			service.setHost(masterHost);
			service.setPort(masterPort);
			services.put(masterName, service);
		} else {
			SentinelServiceConfig serviceConfig = new SentinelServiceConfig(
					masterName);
			serviceConfig.setHost(masterHost);
			serviceConfig.setPort(masterPort);
			services.put(masterName, serviceConfig);
		}
	}

	public void refreshListOfSentinels(String masterName) {
		try {
			final Jedis jedis = findMaster(masterName);

			List<Map<String, String>> currentSentinels = jedis
					.sentinelSentinels(masterName);

			for (Map<String, String> item : currentSentinels) {
				config.addSentinelInstance(item.get("ip"),
						Integer.valueOf(item.get("port").toString()));
			}
		} catch (Exception ex) {
			logger.error("An error occured while refreshing the list of Sentinels from the current Master at :"
					+ services.get(masterName).getHost()
					+ ":"
					+ services.get(masterName).getPort());
		}
	}

	/*
	 * Lazy Attempt to determine if we are using the right master
	 */
	public boolean validate(String masterName, Jedis jedis) {
		if (!services.containsKey(masterName))
			return false;

		if (!jedis.getClient().getHost()
				.equals(services.get(masterName).getHost())
				|| jedis.getClient().getPort() != (services.get(masterName)
						.getPort())) {

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder
					.append("Validation found that ")
					.append(jedis.getClient().getHost())
					.append(":")
					.append(jedis.getClient().getPort())
					.append(" is not the same as the expected master determined by Sentinel at ")
					.append(services.get(masterName).getHost()).append(":")
					.append(services.get(masterName).getPort());

			logger.warn(stringBuilder.toString());
			return false;
		}
		return true;
	}
}