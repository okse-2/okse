package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import io.moquette.spi.impl.subscriptions.Subscription;
import io.netty.channel.Channel;
import no.ntnu.okse.core.messaging.Message;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.testng.AssertJUnit.assertEquals;


public class MQTTServerTest {
    private MQTTServer mqtt;
    private MQTTProtocolServer ps;
    private MQTTSubscriptionManager subManagerMock;

    @BeforeTest
    public void setUp() {
        String host = "localhost";
        int port = 4213;
        subManagerMock = Mockito.mock(MQTTSubscriptionManager.class);
        ps = new MQTTProtocolServer(host, port);
        mqtt = new MQTTServer(ps, host, port);
        mqtt.start();
        mqtt.setSubscriptionManager(subManagerMock);
    }

    @AfterTest
    public void tearDown() {
        mqtt.stopServer();
        mqtt = null;
    }


    @Test
    public void sendMessage() {
        MQTTServer mqtt = getInstance();
        Message message = new Message("Test message", "MQTT", null, "MQTT");
        PublishMessage msg = createMQTTMessage();

        MQTTServer spy = Mockito.spy(mqtt);

        ArrayList<MQTTSubscriber> subscribers = new ArrayList<>();
        subscribers.add(new MQTTSubscriber("localhost", 1234, "MQTT", "ogdans3", null));

        //Test that internalPublish is called with the correct parameter
        Mockito.doReturn(msg).when(spy).createMQTTMessage(message);
        Mockito.doReturn(subscribers).when(subManagerMock).getAllSubscribersFromTopic("MQTT");
        Mockito.doNothing().when(spy).internalPublish(msg);
        spy.sendMessage(message);
        Mockito.verify(spy, Mockito.atLeastOnce()).internalPublish(msg);
        Mockito.reset(spy);
    }

    @Test
    public void createMQTTMessageTest() {
        MQTTServer mqtt = getInstance();
        Message message = new Message("Payload", "testing", null, "MQTT");

        PublishMessage expectedMsg = new PublishMessage();
        ByteBuffer payload = ByteBuffer.wrap("Payload".getBytes());
        expectedMsg.setPayload(payload);
        expectedMsg.setTopicName("testing");
        expectedMsg.setQos(AbstractMessage.QOSType.LEAST_ONE);

        PublishMessage actualMsg = mqtt.createMQTTMessage(message);
        assertEquals(expectedMsg.getPayload(), actualMsg.getPayload());
        assertEquals(expectedMsg.getTopicName(), actualMsg.getTopicName());
    }

    @Test
    public void HandlePublish() {
        MQTTServer mqtt = getInstance();
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


        InterceptPublishMessage msg = new InterceptPublishMessage(pubMsg, clientID);
        mqtt_spy.HandlePublish(msg);
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        Mockito.verify(mqtt_spy).sendMessageToOKSE(messageArgument.capture());
        assertEquals(payload, messageArgument.getValue().getMessage());
        assertEquals(topic, messageArgument.getValue().getTopic());
    }

    @Test
    public void HandleSubscribe() throws InterruptedException {
        MQTTServer mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InterceptSubscribeMessage msg = new InterceptSubscribeMessage(new Subscription("ogdans3", "testing", AbstractMessage.QOSType.LEAST_ONE));

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleSubscribe(msg);

        Mockito.verify(subManagerMock).addSubscriber("127.0.0.1", 1883, "testing", "ogdans3");
        Mockito.reset(subManagerMock);
    }

    @Test
    public void ChannelIsNull() {
        MQTTServer mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);
        String clientID = "ogdans3";
        InterceptSubscribeMessage msg = new InterceptSubscribeMessage(new Subscription(clientID, "testing", AbstractMessage.QOSType.LEAST_ONE));
        InterceptPublishMessage pubMsg = new InterceptPublishMessage(new PublishMessage(), clientID);

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(null);

        //Test for the HandleSubscribe method
        mqtt_spy.HandleSubscribe(msg);
        Mockito.reset(subManagerMock);

        mqtt_spy.HandlePublish(pubMsg);
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);
        Mockito.verify(mqtt_spy, Mockito.never()).sendMessageToOKSE(messageArgument.capture());
        Mockito.reset(subManagerMock);
    }

    @Test
    public void HandleUnsubscribe() {
        MQTTServer mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InterceptUnsubscribeMessage msg = new InterceptUnsubscribeMessage("testing", "ogdans3");

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleUnsubscribe(msg);
//        Mockito.verify(subManagerMock).removeSubscriber(msg.getClientID());
        Mockito.reset(subManagerMock);
    }

    @Test
    public void HandleDisconnect() {
        MQTTServer mqtt = getInstance();
        MQTTServer mqtt_spy = Mockito.spy(mqtt);

        Channel channelMock = Mockito.mock(Channel.class);

        InterceptDisconnectMessage msg = new InterceptDisconnectMessage("ogdans3");

        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1883);
        String clientID = msg.getClientID();

        Mockito.when(channelMock.remoteAddress()).thenReturn(addr);
        Mockito.when(mqtt_spy.getChannelByClientId(clientID)).thenReturn(channelMock);

        mqtt_spy.HandleDisconnect(msg);
        Mockito.verify(subManagerMock).removeSubscribers(msg.getClientID());
        Mockito.reset(subManagerMock);
    }


    private MQTTServer getInstance() {
        return mqtt;
    }

    private PublishMessage createMQTTMessage() {
        PublishMessage msg = new PublishMessage();
        ByteBuffer payload = ByteBuffer.wrap("This is a test".getBytes());

        String topicName = "Test";

        msg.setPayload(payload);
        msg.setTopicName(topicName);
        msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
        return msg;

    }
}
