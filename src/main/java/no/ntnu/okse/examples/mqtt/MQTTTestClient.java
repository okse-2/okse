package no.ntnu.okse.examples.mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Test client for MQTT
 */
public class MQTTTestClient {

    private static Logger log;
    private MqttClient mqttClient;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1883;
    private static final int DEFAULT_QOS = 0;

    /**
     * MQTT client
     *
     * @param host hostname
     * @param port port
     * @param clientId client id
     */
    public MQTTTestClient(String host, int port, String clientId) {
        log = Logger.getLogger(MQTTTestClient.class);
        String broker = String.format("tcp://%s:%d", host, port);
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(broker, clientId, persistence);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MQTTTestClient(String host, int port) {
        this(host, port, "MQTTTestClient");
    }

    public MQTTTestClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Connect to broker
     */
    public void connect() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        log.debug("Connecting to broker");
        try {
            mqttClient.connect(connOpts);
        } catch (MqttException e) {
            log.error("Failed to connect to broker", e);
        }
        log.debug("Connected");
    }

    /**
     * Disconnect from broker
     */
    public void disconnect() {
        try {
            mqttClient.disconnect();
            log.debug("Disconnected");
        } catch (MqttException e) {
            log.error("Failed to disconnect", e);
        }
    }

    /**
     * Publish to broker
     *
     * @param topic topic
     * @param content message content
     * @param qos Quality of Service level
     */
    public void publish(String topic, String content, int qos) {
        log.debug("Publishing message: " + content);
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        try {
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("Failed to publish", e);
        }
        log.debug("Message published");
    }

    /**
     * Publish to broker
     *
     * @param topic topic
     * @param content message content
     */
    public void publish(String topic, String content) {
        publish(topic, content, DEFAULT_QOS);
    }

    public void subscribe(String topic) {
        log.debug("Subscribing to topic: " + topic);
        try {
            mqttClient.subscribe(topic);
        } catch (MqttException e) {
            log.error("Failed to subscribe", e);
        }
        log.debug("Subscribed to topic: " + topic);
    }

    /**
     * Sett Paho MQTT callback
     *
     * @param callback callback instance
     */
    public void setCallback(MqttCallback callback) {
        mqttClient.setCallback(callback);
    }

    /**
     * Start an MQTT client which subscribes to "example" topic
     *
     * @param args
     */
    public static void main(String[] args) {
        MQTTTestClient client = new MQTTTestClient();
        client.connect();
        client.setCallback(new ExampleCallback());
        client.subscribe("example");
    }

    /**
     * Example callback class
     */
    private static class ExampleCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            log.warn("Connection lost", throwable);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.println(String.format("Message arrived on topic %s with content %s", topic, message));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            log.debug("Message delivered");
        }
    }
}
