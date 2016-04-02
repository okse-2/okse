package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import io.moquette.server.ConnectionDescriptor;
import io.moquette.spi.impl.subscriptions.Subscription;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.configuration.injection.MockInjection;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.*;


public class MQTTServerTest {



	enum Status {
		CONNECT,
		DISCONNECT,
		PUBLISH,
		SUBSCRIBE,
		UNSUBSCRIBE;
	}

	private static Status status;
	private static InterceptAbstractMessage interceptMessage;

	private static final class MockObserver implements InterceptHandler {
		@Override
		public void onConnect(InterceptConnectMessage interceptConnectMessage) {
			status = Status.CONNECT;
		}

		@Override
		public void onDisconnect(InterceptDisconnectMessage interceptDisconnectMessage) {
			status = Status.DISCONNECT;
		}

		@Override
		public void onPublish(InterceptPublishMessage interceptPublishMessage) {
			status = Status.PUBLISH;
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage interceptSubscribeMessage) {
			status = Status.SUBSCRIBE;
		}

		@Override
		public void onUnsubscribe(InterceptUnsubscribeMessage interceptUnsubscribeMessage) {
			status = Status.UNSUBSCRIBE;
		}
	}

	MQTTServer mqtt;
	MQTTProtocolServer ps;
    MQTTSubscriptionManager subManagerMock;

	@BeforeTest
	public void setUp() {
		String host = "localhost";
		int port = 1234;
        subManagerMock = Mockito.mock(MQTTSubscriptionManager.class);
		ps = new MQTTProtocolServer(host, port);
		mqtt = new MQTTServer(ps, host, port);
        mqtt.setSubscriptionManager(subManagerMock);
        mqtt.start();
	}

	@AfterTest
	public void tearDown() {
        mqtt.stopServer();
		mqtt = null;
		status = null;
	}


	@Test
	public void sendMessage() {
		MQTTServer mqtt = getInstance();
		Message message = new Message("Test message", "MQTT", null, "MQTT");
		PublishMessage msg = createMQTTMessage();

		MQTTServer spy = Mockito.spy(mqtt);

		//Test that internalPublish is called with the correct parameter
		Mockito.doReturn(msg).when(spy).createMQTTMessage(message);
		Mockito.doNothing().when(spy).internalPublish(msg);
		spy.sendMessage(message);
		Mockito.verify(spy, Mockito.atLeastOnce()).internalPublish(msg);
        Mockito.reset(spy);
	}

    @Test
    public void createMQTTMessageTest(){
        MQTTServer mqtt = getInstance();
        Message message = new Message("Payload", "testing", null, "MQTT");
        MQTTServer spy = Mockito.spy(mqtt);

        PublishMessage expectedMsg = new PublishMessage();
        ByteBuffer payload = ByteBuffer.wrap( "Payload".getBytes() );
        String topicName = message.getTopic();
        expectedMsg.setPayload( payload );
        expectedMsg.setTopicName( "testing" );
        expectedMsg.setQos(AbstractMessage.QOSType.LEAST_ONE);

        PublishMessage actualMsg= mqtt.createMQTTMessage(message);
        assertEquals(expectedMsg.getPayload(), actualMsg.getPayload());
        assertEquals(expectedMsg.getTopicName(), actualMsg.getTopicName());
        assertEquals(expectedMsg.getQos(), actualMsg.getQos());
    }

	@Test
	public void HandlePublish(){
        mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        String clientID = "ogdans3";

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        PublishMessage pubMsg = new PublishMessage();
        String payload = "This is a payload";
        String topic = "testing";
        pubMsg.setTopicName(topic);
        pubMsg.setPayload(ByteBuffer.wrap(payload.getBytes()));
        pubMsg.setQos(AbstractMessage.QOSType.LEAST_ONE);


        InterceptPublishMessage msg = new InterceptPublishMessage( pubMsg, clientID);
        mqtt_spy.HandlePublish(msg);
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        Mockito.verify(mqtt_spy).sendMessageToOKSE(messageArgument.capture());
        assertEquals( payload, messageArgument.getValue().getMessage());
        assertEquals( topic, messageArgument.getValue().getTopic());
    }

	@Test
	public void HandleSubscribe() throws InterruptedException {
		mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

		InterceptSubscribeMessage msg = new InterceptSubscribeMessage(new Subscription("ogdans3", "testing", AbstractMessage.QOSType.LEAST_ONE ));

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "MQTT");
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleSubscribe(msg);

        ArgumentCaptor<Subscriber> subscriberArgument = ArgumentCaptor.forClass(Subscriber.class);
        ArgumentCaptor<String> clientIDArgument= ArgumentCaptor.forClass(String.class);
        Mockito.verify(subManagerMock).addSubscriber(subscriberArgument .capture(), clientIDArgument.capture());
        assertEquals(clientID, clientIDArgument.getValue());
        Mockito.reset(subManagerMock);
    }

    @Test
    public void ChannelIsNull(){
        mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);
        String clientID = "ogdans3";
        InterceptSubscribeMessage msg = new InterceptSubscribeMessage(new Subscription(clientID, "testing", AbstractMessage.QOSType.LEAST_ONE ));
        InterceptPublishMessage pubMsg = new InterceptPublishMessage (new PublishMessage(), clientID);

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "MQTT");

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(null);

        //Test for the HandleSubscribe method
        mqtt_spy.HandleSubscribe(msg);
        ArgumentCaptor<Subscriber> subscriberArgument = ArgumentCaptor.forClass(Subscriber.class);
        ArgumentCaptor<String> clientIDArgument= ArgumentCaptor.forClass(String.class);
        Mockito.verify(subManagerMock, Mockito.never()).addSubscriber(subscriberArgument.capture(), clientIDArgument.capture());
        Mockito.reset(subManagerMock);

        mqtt_spy.HandlePublish(pubMsg);
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(mqtt_spy, Mockito.never()).sendMessageToOKSE(messageArgument.capture());
        Mockito.reset(subManagerMock);
    }

    @Test
    public void HandleUnsubscribe(){
        mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InterceptUnsubscribeMessage msg = new InterceptUnsubscribeMessage("testing", "ogdans3");

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "MQTT");
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleUnsubscribe(msg);
        Mockito.verify(subManagerMock).removeSubscriber(msg.getClientID());
        Mockito.reset(subManagerMock);
    }

    @Test
    public void HandleDisconnect(){
        mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InterceptDisconnectMessage msg = new InterceptDisconnectMessage("ogdans3");

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "MQTT");
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleDisconnect(msg);
        Mockito.verify(subManagerMock).removeSubscriber(msg.getClientID());
        Mockito.verify(subManagerMock).removePublisher(msg.getClientID());
        Mockito.reset(subManagerMock);
    }

	@Test
	public void receiveMessage() {
	}

	private MQTTServer getInstance(){
		return mqtt;
	}
	private PublishMessage createMQTTMessage(){
		PublishMessage msg = new PublishMessage();
		ByteBuffer payload = ByteBuffer.wrap( "This is a test".getBytes() );

		String topicName = "Test";

		msg.setPayload( payload );
		msg.setTopicName( topicName );
		msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
		return msg;

	}
}
