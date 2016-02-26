package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;

public class MQTTProtocolServer extends AbstractProtocolServer {
	private static final String DEFAULT_HOST = "0.0.0.0";
	private static final Integer DEFAULT_PORT = 1234; // TODO: Replace with actual default port

	private static boolean _invoked = false;
	private static MQTTProtocolServer _singleton = null;

	private MQTTProtocolServer(String host, Integer port) {
		init(host, port);
	}

	@Override
	protected void init(String host, Integer port) {
		this.host = host;
		this.port = port;
		_invoked = true;
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
		MQTTProtocolServer mqtt = MQTTProtocolServer.getInstance();

	}

	@Override
	public void run() {

	}

	@Override
	public void stopServer() {

	}

	@Override
	public String getProtocolServerType() {
		return null;
	}

	@Override
	public void sendMessage(Message message) {

	}
}
