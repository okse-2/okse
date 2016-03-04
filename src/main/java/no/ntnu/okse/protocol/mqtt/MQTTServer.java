package no.ntnu.okse.protocol.mqtt;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.FilesystemConfig;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import no.ntnu.okse.core.messaging.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MQTTServer extends Server {

	private static Logger logger = Logger.getLogger(Server.class);

	static class MQTTListener extends AbstractInterceptHandler {
		@Override
		public void onPublish(InterceptPublishMessage message) {
			logger.info("Received message on topic " + message.getTopicName());
		}
	}

	public void init(String host, int port) {
		List<InterceptHandler> interceptHandlers = new ArrayList<>();
		interceptHandlers.add(new MQTTListener());

		Properties properties = new Properties();
		properties.setProperty(BrokerConstants.HOST_PROPERTY_NAME, host);
		properties.setProperty(BrokerConstants.PORT_PROPERTY_NAME, "" + port);
		properties.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, "25342");

		final IConfig config = new MemoryConfig(properties);
		try {
			startServer(config, interceptHandlers);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(Message message) {

	}
}
