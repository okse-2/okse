package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;

public class MQTTProtocolServer extends AbstractProtocolServer {
	private static final String DEFAULT_HOST = "0.0.0.0";
	private static final Integer DEFAULT_PORT = 1883;

	private static boolean _invoked = false;
	private static MQTTProtocolServer _singleton = null;

	private MQTTProtocolServer(String host, Integer port) {
		init(host, port);
	}

	private MQTTServer server;

	@Override
	protected void init(String host, Integer port) {
		this.host = host;
		this.port = port;
		_invoked = true;
		protocolServerType = "MQTT";
		log = Logger.getLogger(MQTTProtocolServer.class.getName());
	}

	public static MQTTProtocolServer getInstance() {
		if(!_invoked) {
			_singleton = new MQTTProtocolServer(DEFAULT_HOST, DEFAULT_PORT);
		}
		return _singleton;
	}

	public static MQTTProtocolServer getInstance(String host, Integer port) {
		if(!_invoked) {
			_singleton = new MQTTProtocolServer(host, port);
		}
		return _singleton;
	}


	@Override
	public void boot() {
		if(!_running) {
			_running = true;
			_serverThread = new Thread(this::run);
			_serverThread.setName("MQTTProtocolServer");
			_serverThread.start();
			log.info("MQTTProtocolServer booted successfully");
		}
	}

	@Override
	public void run() {
		server = new MQTTServer();
		server.init(host, port);
	}

	@Override
	public void stopServer() {
		log.info("Stopping MQTTProtocolServer");
		_running = false;
		_singleton = null;
		_invoked = false;
		server.stopServer();
		server = null;
		log.info("MQTTProtocolServer is stopped");
	}

	@Override
	public String getProtocolServerType() {
		return protocolServerType;
	}

	@Override
	public void sendMessage(Message message) {
		log.info("Received message on topic " + message.getMessage() );
		server.sendMessage( message );

	}

	public void incrementTotalRequests() {
		totalRequests.incrementAndGet();
	}

	public void incrementTotalBadRequests() {
		totalBadRequests.incrementAndGet();
	}

	public void incrementTotalErrors() {
		totalErrors.incrementAndGet();
	}

	public void incrementTotalMessagesReceived() {
		totalMessagesReceived.incrementAndGet();
	}

	public void incrementTotalMessagesSent() {
		totalMessagesSent.incrementAndGet();
	}



	public boolean isRunning() {
		return _running;
	}
}
