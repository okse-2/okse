package no.ntnu.okse.protocol;

import com.rabbitmq.client.DefaultConsumer;
import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.core.topic.TopicService;
import no.ntnu.okse.examples.amqp.AMQPClient;
import no.ntnu.okse.examples.amqp091.AMQP091Client;
import no.ntnu.okse.examples.mqtt.MQTTClient;
import no.ntnu.okse.examples.wsn.WSNClient;
import org.apache.cxf.wsn.client.Consumer;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;

@Test(singleThreaded = true)
public class MessageSendingTest {
    @BeforeClass
    public void setUp() {
        CoreService cs = CoreService.getInstance();
        cs.registerService(MessageService.getInstance());
        cs.bootProtocolServers();
        cs.boot();
    }

    public void mqttToMqtt() throws Exception {
        MQTTClient subscriber = new MQTTClient("localhost", 1883, "client1");
        MQTTClient publisher = new MQTTClient("localhost", 1883, "client2");
        subscriber.connect();
        MqttCallback callback = mock(MqttCallback.class);
        subscriber.setCallback(callback);
        publisher.connect();
        subscriber.subscribe("mqtt");

        publisher.publish("mqtt", "Text content");
        Thread.sleep(2000);
        verify(callback).messageArrived(anyString(), any(MqttMessage.class));
        subscriber.disconnect();
        publisher.disconnect();
    }

    @Test(enabled = false)
    public void amqpToAmqp() throws Exception {
        AMQPClient subscriber = new AMQPClient();
        AMQPClient publisher = new AMQPClient();
        subscriber.connect();
        publisher.connect();
        subscriber.subscribe("amqp");

        publisher.publish("amqp", "Text content");
        Thread.sleep(2000);
        // Check if message was received
        assertNotNull(subscriber.getMessage());
        subscriber.disconnect();
        publisher.disconnect();
    }

    public void amqp091ToAmqp091() throws Exception {
        AMQP091Client subscriber = new AMQP091Client();
        AMQP091Client publisher = new AMQP091Client();
        subscriber.connect();
        publisher.connect();
        DefaultConsumer consumer = mock(DefaultConsumer.class);
        subscriber.setConsumer(consumer);
        subscriber.subscribe("amqp091");

        publisher.publish("amqp091", "Text content");
        Thread.sleep(2000);
        verify(consumer).handleDelivery(any(), any(), any(), any());
        subscriber.disconnect();
        publisher.disconnect();
    }

    public void wsnToWsn() throws InterruptedException {
        WSNClient subsciber = new WSNClient();
        WSNClient publisher = new WSNClient();
        Consumer.Callback callback = mock(Consumer.Callback.class);
        subsciber.setCallback(callback);
        subsciber.subscribe("wsn");


        publisher.publish("wsn", "Text content");
        Thread.sleep(2000);
        verify(callback).notify(any());
        subsciber.unsubscribe("wsn");
    }

    public void allToAll() throws Exception {
        WSNClient wsnClient = new WSNClient();
        MQTTClient mqttClient = new MQTTClient("localhost", 1883, "clientAll");
        AMQP091Client amqp091Client = new AMQP091Client();
        mqttClient.connect();
        amqp091Client.connect();

        Consumer.Callback wsnCallback = mock(Consumer.Callback.class);
        wsnClient.setCallback(wsnCallback);
        MqttCallback mqttCallback = mock(MqttCallback.class);
        mqttClient.setCallback(mqttCallback);
        DefaultConsumer amqp091Callback = mock(DefaultConsumer.class);
        amqp091Client.setConsumer(amqp091Callback);

        wsnClient.subscribe("all", "localhost", 9002);
        mqttClient.subscribe("all");
        amqp091Client.subscribe("all");

        wsnClient.publish("all", "1");
        mqttClient.publish("all", "2");
        amqp091Client.publish("all", "3");

        Thread.sleep(3000);

        verify(mqttCallback, times(3)).messageArrived(anyString(), any(MqttMessage.class));
        verify(amqp091Callback, times(3)).handleDelivery(any(), any(), any(), any());
        verify(wsnCallback, times(3)).notify(any());

        wsnClient.unsubscribe("all");
        mqttClient.disconnect();
        amqp091Client.disconnect();
    }
}
