package no.ntnu.okse.protocol;

import com.rabbitmq.client.DefaultConsumer;
import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.examples.amqp.AMQPCallback;
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
import static org.testng.Assert.*;

@Test(singleThreaded = true)
public class MessageSendingTest {
    @BeforeClass
    public void setUp() throws InterruptedException {
        CoreService cs = CoreService.getInstance();
        cs.registerService(MessageService.getInstance());
        cs.bootProtocolServers();
        cs.boot();
        // Make sure servers have booted properly
        Thread.sleep(3000);
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

    public void amqpToAmqp() throws Exception {
        AMQPClient publisher = new AMQPClient();
        AMQPClient subscriber = new AMQPClient();
        publisher.connect();
        subscriber.connect();
        AMQPCallback callback = mock(AMQPCallback.class);
        subscriber.setCallback(callback);
        subscriber.subscribe("amqp");

        publisher.publish("amqp", "Text content");
        Thread.sleep(2000);
        // Check if message was received
        // TODO: Remove times(2) when duplicate message bug is fixed
        verify(callback, times(2)).onReceive(any());
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
        WSNClient subscriber = new WSNClient();
        WSNClient publisher = new WSNClient();
        Consumer.Callback callback = mock(Consumer.Callback.class);
        subscriber.setCallback(callback);
        subscriber.subscribe("wsn");


        publisher.publish("wsn", "Text content");
        Thread.sleep(2000);
        verify(callback).notify(any());
        subscriber.unsubscribe("wsn");
    }

    public void allToAll() throws Exception {
        WSNClient wsnClient = new WSNClient();
        MQTTClient mqttClient = new MQTTClient("localhost", 1883, "clientAll");
        AMQP091Client amqp091Client = new AMQP091Client();
        AMQPClient amqpClient = new AMQPClient();
        mqttClient.connect();
        amqp091Client.connect();
        amqpClient.connect();

        Consumer.Callback wsnCallback = mock(Consumer.Callback.class);
        wsnClient.setCallback(wsnCallback);
        MqttCallback mqttCallback = mock(MqttCallback.class);
        mqttClient.setCallback(mqttCallback);
        DefaultConsumer amqp091Callback = mock(DefaultConsumer.class);
        amqp091Client.setConsumer(amqp091Callback);
        AMQPCallback amqpCallback = mock(AMQPCallback.class);
        amqpClient.setCallback(amqpCallback);

        wsnClient.subscribe("all", "localhost", 9002);
        mqttClient.subscribe("all");
        amqp091Client.subscribe("all");
        amqpClient.subscribe("all");

        mqttClient.publish("all", "MQTT");
        amqp091Client.publish("all", "AMQP 0.9.1");
        amqpClient.publish("all", "AMQP 1.0");
        wsnClient.publish("all", "WSN");

        // Wait for messages to arrive
        Thread.sleep(2000);

        // TODO: Subtract 1 from AMQP 1.0 when duplicate message bug is fixed
        verify(amqpCallback, times(5)).onReceive(any());
        verify(mqttCallback, times(4)).messageArrived(anyString(), any(MqttMessage.class));
        verify(amqp091Callback, times(4)).handleDelivery(any(), any(), any(), any());
        verify(wsnCallback, times(4)).notify(any());


        wsnClient.unsubscribe("all");
        mqttClient.disconnect();
        amqp091Client.disconnect();
        amqpClient.disconnect();
    }
}
