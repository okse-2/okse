package no.ntnu.okse.protocol;

import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.examples.amqp.AMQPClient;
import no.ntnu.okse.examples.mqtt.MQTTClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@Test(singleThreaded = true)
public class MessageSendingTest {
    @BeforeClass
    public void setUp() {
//        CoreService.getInstance().bootProtocolServers();
    }

    public void mqttToMqtt() throws Exception {
        MQTTClient subscriber = new MQTTClient("localhost", 1883, "client1");
        MQTTClient publisher = new MQTTClient("localhost", 1883, "client2");
        MqttCallback callback = mock(MqttCallback.class);
        subscriber.connect();
        subscriber.setCallback(callback);
        publisher.connect();
        subscriber.subscribe("mqtt");

        publisher.publish("mqtt", "Text content");
        Thread.sleep(1000);
        verify(callback).messageArrived(anyString(), any(MqttMessage.class));
        subscriber.disconnect();
        publisher.disconnect();
    }

    public void amqpToAmqp() throws Exception {
        AMQPClient subscriber = new AMQPClient();
        AMQPClient publisher = new AMQPClient();
        subscriber.connect();
        publisher.connect();
        subscriber.subscribe("amqp");

        publisher.publish("amqp", "Text content");
        Thread.sleep(1000);
        // Check if message was received
        assertNotNull(subscriber.getMessage());
        subscriber.disconnect();
        publisher.disconnect();
    }
}
