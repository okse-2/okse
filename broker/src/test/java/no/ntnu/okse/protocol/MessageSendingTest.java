package no.ntnu.okse.protocol;

import com.rabbitmq.client.DefaultConsumer;
import no.ntnu.okse.clients.stomp.StompCallback;
import no.ntnu.okse.clients.stomp.StompClient;
import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.core.event.listeners.SubscriptionChangeListener;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.clients.amqp.AMQPCallback;
import no.ntnu.okse.clients.amqp.AMQPClient;
import no.ntnu.okse.clients.amqp091.AMQP091Client;
import no.ntnu.okse.clients.mqtt.MQTTClient;
import no.ntnu.okse.clients.wsn.WSNClient;
import org.apache.cxf.wsn.client.Consumer;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.testng.annotations.*;

import java.io.InputStream;

import static org.mockito.Mockito.*;

public class MessageSendingTest {
    private SubscriptionChangeListener subscriptionMock;
    private SubscriptionService subscriptionService = SubscriptionService.getInstance();

    @BeforeClass
    public void classSetUp() throws InterruptedException {
        CoreService cs = CoreService.getInstance();

        cs.registerService(MessageService.getInstance());
        cs.registerService(subscriptionService);

        InputStream resourceAsStream = CoreService.class.getResourceAsStream("/config/protocolservers.xml");
        cs.bootProtocolServers(resourceAsStream);
        cs.bootProtocolServers();
        cs.boot();

        // Make sure servers have booted properly
        Thread.sleep(3000);
    }

    @BeforeMethod
    public void setUp() {
        subscriptionMock = mock(SubscriptionChangeListener.class);
        subscriptionService.addSubscriptionChangeListener(subscriptionMock);
    }

    @AfterMethod
    public void tearDown() {
        subscriptionService.removeAllListeners();
    }

    @Test
    public void mqttToMqtt() throws Exception {
        MQTTClient subscriber = new MQTTClient("localhost", 1883, "client1");
        MQTTClient publisher = new MQTTClient("localhost", 1883, "client2");
        subscriber.connect();
        MqttCallback callback = mock(MqttCallback.class);
        subscriber.setCallback(callback);
        publisher.connect();
        subscriber.subscribe("mqtt");

        verify(subscriptionMock, timeout(500).atLeastOnce()).subscriptionChanged(any());

        publisher.publish("mqtt", "Text content");
        Thread.sleep(2000);
        subscriber.disconnect();
        publisher.disconnect();
        verify(callback).messageArrived(anyString(), any(MqttMessage.class));
    }

    @Test
    public void amqpToAmqp() throws Exception {
        AMQPClient publisher = new AMQPClient();
        AMQPClient subscriber = new AMQPClient();
        publisher.connect();
        subscriber.connect();
        AMQPCallback callback = mock(AMQPCallback.class);
        subscriber.setCallback(callback);
        subscriber.subscribe("amqp");

        verify(subscriptionMock, timeout(500).atLeastOnce()).subscriptionChanged(any());

        publisher.publish("amqp", "Text content");
        publisher.disconnect();
        Thread.sleep(2000);
        subscriber.disconnect();
        verify(callback).onReceive(any());
    }

    @Test
    public void amqp091ToAmqp091() throws Exception {
        AMQP091Client subscriber = new AMQP091Client();
        AMQP091Client publisher = new AMQP091Client();
        subscriber.connect();
        publisher.connect();
        DefaultConsumer consumer = mock(DefaultConsumer.class);
        subscriber.setConsumer(consumer);
        subscriber.subscribe("amqp091");

        verify(subscriptionMock, timeout(500).atLeastOnce()).subscriptionChanged(any());

        publisher.publish("amqp091", "Text content");
        Thread.sleep(2000);
        subscriber.disconnect();
        publisher.disconnect();
        verify(consumer).handleDelivery(any(), any(), any(), any());
    }

    @Test
    public void wsnToWsn() throws InterruptedException {
        WSNClient subscriber = new WSNClient();
        WSNClient publisher = new WSNClient();
        Consumer.Callback callback = mock(Consumer.Callback.class);
        subscriber.setCallback(callback);
        subscriber.subscribe("wsn");

        verify(subscriptionMock, timeout(1000).atLeastOnce()).subscriptionChanged(any());

        publisher.publish("wsn", "Text content");
        Thread.sleep(2000);
        subscriber.unsubscribe("wsn");
        verify(callback).notify(any());
    }

    @Test
    public void stompToStomp() throws InterruptedException {
        StompClient subscriber = new StompClient();
        StompClient publisher = new StompClient();
        subscriber.connect();
        publisher.connect();
        StompCallback callback = mock(StompCallback.class);
        subscriber.setCallback(callback);
        subscriber.subscribe("stomp");

        verify(subscriptionMock, timeout(1000).atLeastOnce()).subscriptionChanged(any());

        publisher.publish("stomp", "Text content");
        Thread.sleep(2000);
        subscriber.unsubscribe("stomp");
        subscriber.disconnect();
        publisher.disconnect();
        verify(callback).messageReceived(any());
    }

    @Test
    public void allToAll() throws Exception {
        int numberOfProtocols = 5;
        // WSN
        WSNClient wsnClient = new WSNClient();
        Consumer.Callback wsnCallback = mock(Consumer.Callback.class);
        wsnClient.setCallback(wsnCallback);

        // MQTT
        MQTTClient mqttClient = new MQTTClient("localhost", 1883, "clientAll");
        MqttCallback mqttCallback = mock(MqttCallback.class);
        mqttClient.setCallback(mqttCallback);

        // AMQP 0.9.1
        AMQP091Client amqp091Client = new AMQP091Client();
        DefaultConsumer amqp091Callback = mock(DefaultConsumer.class);
        amqp091Client.setConsumer(amqp091Callback);

        // AMQP 1.0
        AMQPClient amqpClient = new AMQPClient();
        // AMQP test client is unable to receive and send messages at the same time
        AMQPClient amqpSender = new AMQPClient();
        AMQPCallback amqpCallback = mock(AMQPCallback.class);
        amqpClient.setCallback(amqpCallback);

        // Stomp
        StompClient stompClient = new StompClient();
        StompCallback stompCallback = mock(StompCallback.class);
        stompClient.setCallback(stompCallback);

        // Connecting
        mqttClient.connect();
        amqp091Client.connect();
        amqpClient.connect();
        amqpSender.connect();
        stompClient.connect();


        // Subscribing
        wsnClient.subscribe("all", "localhost", 9002);
        mqttClient.subscribe("all");
        amqp091Client.subscribe("all");
        amqpClient.subscribe("all");
        stompClient.subscribe("all");

        verify(subscriptionMock, timeout(500).atLeast(numberOfProtocols)).subscriptionChanged(any());

        // Publishing
        wsnClient.publish("all", "WSN");
        mqttClient.publish("all", "MQTT");
        amqp091Client.publish("all", "AMQP 0.9.1");
        amqpSender.publish("all", "AMQP 1.0");
        stompClient.publish("all", "STOMP");

        // Wait for messages to arrive
        Thread.sleep(2000);

        // Unsubscribing/disconnecting
        wsnClient.unsubscribe("all");
        mqttClient.disconnect();
        amqp091Client.disconnect();
        amqpClient.disconnect();
        amqpSender.disconnect();
        stompClient.disconnect();

        // Verifying that all messages were sent
        verify(amqpCallback, times(numberOfProtocols)).onReceive(any());
        verify(mqttCallback, times(numberOfProtocols)).messageArrived(anyString(), any(MqttMessage.class));
        verify(amqp091Callback, times(numberOfProtocols)).handleDelivery(any(), any(), any(), any());
        verify(wsnCallback, times(numberOfProtocols)).notify(any());
        verify(stompCallback, times(numberOfProtocols)).messageReceived(any());
    }
}
