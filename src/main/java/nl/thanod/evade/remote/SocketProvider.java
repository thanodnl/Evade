/**
 * 
 */
package nl.thanod.evade.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nilsdijk
 */
public class SocketProvider
{
	private static final int DEFAULT_SSL_PORT = 2325;
	private static final int DEFAULT_PORT = 2225;

	private static final Logger log = LoggerFactory.getLogger(SocketProvider.class);

	public static ServerSocket open(Map<String, Object> config)
	{
		int backlog = 0;
		
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		int port = SocketProvider.DEFAULT_PORT;
		if (useSSL(config)) {
			port = SocketProvider.DEFAULT_SSL_PORT;
			factory = SSLServerSocketFactory.getDefault();
		}

		port = parseInt(config, "port", port);
		backlog = parseInt(config, "backlog", backlog);

		InetAddress host = null;
		if (config.containsKey("host")) {
			try {
				host = InetAddress.getByName(config.get("host").toString());
			} catch (UnknownHostException ball) {
				log.error("Could not parse the host (context:" + config + ")", ball);
			}
		}

		try {
			return factory.createServerSocket(port, backlog, host);
		} catch (IOException ball) {
			log.error("Could not open socket", ball);
			return null;
		}
	}

	private static int parseInt(Map<String, Object> config, String field, int def)
	{
		if (config.containsKey(field)) {
			try {
				return ((Number) config.get(field)).intValue();
			} catch (ClassCastException ball) {
				log.error("Could not parse the " + field + " (context:" + config + ")", ball);
			}
		}
		return def;
	}

	private static boolean useSSL(Map<String, Object> config)
	{
		if (!config.containsKey("ssl"))
			return false;
		return Boolean.TRUE.equals(config.get("ssl"));
	}
}
