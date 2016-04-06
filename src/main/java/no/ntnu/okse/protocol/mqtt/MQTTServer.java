package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.interception.messages.*;
import io.moquette.server.config.MemoryConfig;
import io.netty.channel.Channel;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.topic.TopicService;
import no.ntnu.okse.protocol.mqtt.MQTTSubscriptionManager;
import org.apache.log4j.Logger;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class MQTTServer extends Server {

	private static Logger log = Logger.getLogger(Server.class);
	private static String protocolServerType;
	private MQTTProtocolServer ps;
	private final IConfig config;
	private List<InterceptHandler> interceptHandlers;
	private MQTTSubscriptionManager subscriptionManager;

	protected class MQTTListener extends AbstractInterceptHandler {
		@Override
		public void onPublish(InterceptPublishMessage message) {
			HandlePublish(message);
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage message) {
			HandleSubscribe(message);
		}

		@Override
		public void onUnsubscribe(InterceptUnsubscribeMessage message) {
			HandleUnsubscribe(message);
		}

		@Override
		public void onDisconnect(InterceptDisconnectMessage message) {
			HandleDisconnect(message);
		}

	}

	void HandlePublish(InterceptPublishMessage message){
		log.info("MQTT message received on topic: " + message.getTopicName() + " from ID: " + message.getClientID());

		Channel channel = getChannelByClientId(message.getClientID());
		if(channel == null)
			return;
		int port = getPort(channel);
		String host = getHost(channel);

        //TODO: Finish discussing if we are going to pass in null instead of the publisher!
		//Publisher pub = new Publisher( message.getTopicName(), host, port, protocolServerType);

		//Adds the publisher to the subscriptionManager, if it is already added the subscription manager will not add it
		//subscriptionManager.addPublisher(pub, message.getClientID());

		String topic = message.getTopicName();
		String payload = getPayload(message);

        Message msg = new Message(payload, topic, null, protocolServerType);
		msg.setAttribute("qos", String.valueOf(message.getQos().byteValue()));
        sendMessageToOKSE(msg);
		ps.incrementTotalMessagesReceived();
	}

	public MQTTServer(MQTTProtocolServer ps, String host, int port) {
		this.ps = ps;
		interceptHandlers = new ArrayList<>();
		interceptHandlers.add(new MQTTListener());
		config = new MemoryConfig(getConfig(host, port));
	}

	public void start() {
		try {
			startServer(config, interceptHandlers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void HandleUnsubscribe(InterceptUnsubscribeMessage message) {
		log.info("Client unsubscribed from: "  + message.getTopicFilter() + "   ID: " + message.getClientID());
		Channel channel = getChannelByClientId(message.getClientID());

		int port = getPort(channel);
		String host = getHost(channel);
        String topic = message.getTopicFilter();
        subscriptionManager.removeSubscriber(host, port, topic);
	}

	void HandleDisconnect(InterceptDisconnectMessage message) {
		log.info("Client disconnected ID: " + message.getClientID());
        String clientID = message.getClientID();

		subscriptionManager.removeSubscribers(clientID);
	}

	void HandleSubscribe(InterceptSubscribeMessage message) {
		log.info("Client subscribed to: "  + message.getTopicFilter() + "   ID: " + message.getClientID());

		TopicService.getInstance().addTopic( message.getTopicFilter() );
		Channel channel = getChannelByClientId(message.getClientID());
		if(channel == null)
			return;

		int port = getPort(channel);
		String host = getHost(channel);

		subscriptionManager.addSubscriber(host, port, message.getTopicFilter(), message.getClientID());
	}

	public void sendMessageToOKSE(Message msg){
		MessageService.getInstance().distributeMessage(msg);
	}

	private String getPayload(InterceptPublishMessage message) {
		ByteBuffer buffer = message.getPayload();
		String payload = new String(buffer.array(), buffer.position(), buffer.limit());
		return payload;
	}

	private int getPort(Channel channel){
		return ((InetSocketAddress)channel.remoteAddress()).getPort();
	}
	private String getHost(Channel channel){
		return ((InetSocketAddress)channel.remoteAddress()).getHostString();
	}

	public void setSubscriptionManager(MQTTSubscriptionManager subscriptionManager){
		this.subscriptionManager = subscriptionManager;
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
	public void sendMessage(@NotNull Message message) {
		PublishMessage msg = createMQTTMessage(message);
		ArrayList<MQTTSubscriber> subscribers = subscriptionManager.getAllSubscribersFromTopic(message.getTopic());
		if(subscribers.size() > 0){
			//This will incremenet the total messages sent for each of the subscribers that the subscription manager found.
            //We should never send fewer or more messages than the number of subscriptions.
            for(int i = 0; i < subscribers.size(); i++){
                ps.incrementTotalMessagesSent();
            }
			internalPublish(msg);
		}
	}

	/**
	 * Creates an MQTT message from the given arguments
	 *
	 * @param message The OKSE message to use when creating MQTT message
	 * */
	protected PublishMessage createMQTTMessage(@NotNull Message message){
		PublishMessage msg = new PublishMessage();
		ByteBuffer payload = ByteBuffer.wrap(message.getMessage().getBytes());

		String topicName = message.getTopic();

		msg.setPayload(payload);
		msg.setTopicName(topicName);
        if(message.getAttribute("qos") == null)
            msg.setQos(AbstractMessage.QOSType.EXACTLY_ONCE);
        else
            msg.setQos(AbstractMessage.QOSType.valueOf(Byte.valueOf(message.getAttribute("qos"))));
		return msg;
	}
}
