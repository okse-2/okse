package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;

public class MQTTProtocolServer extends AbstractProtocolServer {
	protected static final String SERVERTYPE = "mqtt";

	private static Logger log = Logger.getLogger(MQTTProtocolServer.class.getName());

	private MQTTServer server;

	public MQTTProtocolServer(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void boot() {
		if (!_running) {
			server = new MQTTServer(this, host, port);
			_serverThread = new Thread(this::run);
			_serverThread.setName("MQTTProtocolServer");
			_serverThread.start();
			_running = true;
			log.info("MQTTProtocolServer booted successfully");
		}
	}

	@Override
	public void run() {
		MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
		subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
		server.start();
		server.setSubscriptionManager(subscriptionManager);
	}

	@Override
	public void stopServer() {
		log.info("Stopping MQTTProtocolServer");
		server.stopServer();
		_running = false;
		server = null;
		log.info("MQTTProtocolServer is stopped");
	}

	@Override
	public String getProtocolServerType() {
		return SERVERTYPE;
	}

	@Override
	public void sendMessage(Message message) {
		log.info("Received message on topic " + message.getMessage());
		server.sendMessage(message);

	}

	public boolean isRunning() {
		return _running;
	}
}
