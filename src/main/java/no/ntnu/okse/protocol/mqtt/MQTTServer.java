package no.ntnu.okse.protocol.mqtt;

import com.sun.istack.internal.NotNull;
import io.netty.channel.Channel;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.core.topic.TopicService;
import org.apache.log4j.Logger;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class MQTTServer extends Server {

	private static Logger log = Logger.getLogger(Server.class);
	private static String protocolServerType;

	class MQTTListener extends AbstractInterceptHandler {
		@Override
		public void onPublish(InterceptPublishMessage message) {
			log.info("MQTT message received on topic: " + message.getTopicName() + " from ID: " + message.getClientID());

			distributeMessage(message);
			MQTTProtocolServer.getInstance().incrementTotalMessagesReceived();
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage message) {
			log.info("Client subscribed to: "  + message.getTopicFilter() + "   ID: " + message.getClientID());

			TopicService.getInstance().addTopic(message.getTopicFilter());
			int port = getPort(message.getClientID());
			String host = getHost(message.getClientID());

			Subscriber sub = new Subscriber(host, port, message.getTopicFilter(), protocolServerType);
			SubscriptionService.getInstance().addSubscriber(sub);
		}

	}

	private void distributeMessage(InterceptPublishMessage message) {
		int port = getPort(message.getClientID());
		String host = getHost(message.getClientID());

		Publisher pub = new Publisher(message.getTopicName(), host, port, protocolServerType);
		String topic = message.getTopicName();
		String payload = getPayload(message);

		MessageService.getInstance().distributeMessage(
				new Message(payload, topic, pub, protocolServerType)
		);
	}

	private String getPayload(InterceptPublishMessage message) {
		ByteBuffer buffer = message.getPayload();
		String payload = new String(buffer.array(), buffer.position(), buffer.limit());
		return payload;
	}

	private int getPort(String clientID) {
		Channel channel = getChannelByClientId(clientID);
		String remote = channel.remoteAddress().toString();
		int port = Integer.parseInt(remote.split(":")[1]);
		return port;
	}
	private String getHost(String clientID) {
		Channel channel = getChannelByClientId(clientID);
		String remote = channel.remoteAddress().toString();
		String host = remote.split(":")[0];
		// Moquette has a weird address format, example: /127.0.0.1:1883
		if( host.indexOf('/') == 0)
			host = host.substring(1);
		return host;
	}

	public void init(String host, int port) {
		List<InterceptHandler> interceptHandlers = new ArrayList<>();
		interceptHandlers.add(new MQTTListener());

		protocolServerType = "MQTT";

		final IConfig config = new MemoryConfig(getConfig(host, port));
		try {
			startServer(config, interceptHandlers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Properties getConfig(String host, int port) {
		Properties properties = new Properties();
		properties.setProperty(BrokerConstants.HOST_PROPERTY_NAME, host);
		properties.setProperty(BrokerConstants.PORT_PROPERTY_NAME, "" + port);
		// Set random port for websockets instead of 8080
		properties.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, "25342");
		// Disable automatic publishing (handled by the broker instead)
		properties.setProperty(BrokerConstants.PUBLISH_TO_CONSUMERS, "false");
		return properties;
	}

	/**
	 * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
	 * @param message is the message that is sent from OKSE core
	 * */
	public void sendMessage(Message message) {
		PublishMessage msg = createMQTTMessage(message);
		internalPublish(msg);
	}

	/**
	 * Creates an MQTT message from the given arguments
	 *
	 * @param message The OKSE message to use when creating MQTT message
	 * */
	private PublishMessage createMQTTMessage(@NotNull Message message){
		PublishMessage msg = new PublishMessage();
		ByteBuffer payload = ByteBuffer.wrap(message.getMessage().getBytes());

		String topicName = message.getTopic();

		msg.setPayload(payload);
		msg.setTopicName(topicName);
		msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
		return msg;
	}
}
