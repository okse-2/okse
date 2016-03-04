package no.ntnu.okse.protocol.mqtt;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.proto.messages.AbstractMessage;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.ClientSession;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.core.topic.TopicService;
import org.apache.log4j.Logger;

import io.moquette.spi.impl.ProtocolProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class MQTTServer extends Server {

	private static Logger log = Logger.getLogger(Server.class);
	private static String protocolServerType;

	class MQTTListener extends AbstractInterceptHandler {
		@Override
		public void onPublish(InterceptPublishMessage message) {
			//TODO: We need to get the publisher that sent the message, somehow
			//TODO: So that we get the host and can send the message to the correct address, same with the port
			Publisher pub = new Publisher( message.getTopicName(), "127.0.0.1", 1883, protocolServerType);
			ByteBuffer buffer = message.getPayload();
			String payload = new String(buffer.array(), buffer.position(), buffer.limit());
			String topic = message.getTopicName();
			MessageService.getInstance().distributeMessage(
					new Message( payload, topic, pub, protocolServerType )
			);
			MQTTProtocolServer.getInstance().incrementTotalMessagesReceived();
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage message) {
			log.info("Client subscribed to: "  + message.getTopicFilter() + "   ID: " + message.getClientID());

			//TODO: We need to get the publisher that sent the message, somehow
			//TODO: So that we get the host and can send the message to the correct address, same with the port
			TopicService.getInstance().addTopic( message.getTopicFilter() );
			Subscriber sub = new Subscriber( "127.0.0.1", 1309, message.getTopicFilter(), protocolServerType );
			SubscriptionService.getInstance().addSubscriber(sub);

			super.onSubscribe(message);
		}

	}

	public void init(String host, int port) {
		List<InterceptHandler> interceptHandlers = new ArrayList<>();
		interceptHandlers.add(new MQTTListener());

		protocolServerType = "MQTT";

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

	/**
	 * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
	 * @param message is the message that is sent from OKSE core
	 * */
	public void sendMessage(Message message) {
		HashSet<Subscriber> subscribers = SubscriptionService.getInstance().getAllSubscribersForTopic( message.getTopic() );
		for(Subscriber subscriber : subscribers){
			if( subscriber.getOriginProtocol() == protocolServerType ){
				PublishMessage msg = new PublishMessage();
				ByteBuffer payload = ByteBuffer.wrap( message.getMessage().getBytes() );
				String topicName = message.getTopic();

				msg.setPayload( payload );
				msg.setTopicName( topicName );

				msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
				internalPublish(msg);
				MQTTProtocolServer.getInstance().incrementTotalMessagesSent();
			}
		}

	}
}
