package no.ntnu.okse.clients.amqp091;

import com.rabbitmq.client.*;
import no.ntnu.okse.clients.TestClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * AMQP 0.9.1 Test Client
 */
public class AMQP091Client implements TestClient {
    private static Logger log = Logger.getLogger(AMQP091Client.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 56720;
    private Channel channel;
    private Consumer consumer;
    private ConnectionFactory factory;
    private String queueName;

    /**
     * Create an instance of test client with default configuration
     */
    public AMQP091Client() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Create an instance of test client
     *
     * @param host host
     * @param port port
     */
    public AMQP091Client(String host, int port) {

        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

    }

    /**
     * Connect to the broker
     */
    public void connect() {
        try {
            log.debug("Connecting");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            log.debug("Connected");
        } catch (IOException | TimeoutException e) {
            log.error("Unable to connect to broker", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            log.debug("Disconnecting");
            channel.close();
            log.debug("Disconnected");
        } catch (TimeoutException | IOException e) {
            log.error("Failed to disconnect", e);
        }
    }

    /**
     * Subscribe to topic
     *
     * @param topic topic
     */
    public void subscribe(String topic) {
        if(consumer == null) {
            log.error("Consumer not set");
            return;
        }
        try {
            log.debug("Subscribing to topic: " + topic);
            channel.exchangeDeclare(topic, "fanout");
            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, topic, "");
            channel.basicConsume(queueName, true, consumer);
            log.debug("Subscribed to topic: " + topic);
        } catch (IOException e) {
            log.error("Unable to subscribe to topic", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        log.debug("Unsubscribing from topic: " + topic);
        if(queueName != null) {
            try {
                channel.basicCancel(queueName);
                log.debug("Unsubscribed from topic: " + topic);
            } catch (IOException e) {
                log.error("Failed to unsubscribe", e);
            }
        }
        else {
            log.warn("Queue name not found, failed to unsubscribe");
        }
    }

    /**
     * Set consumer to handle callbacks
     *
     * @param consumer consumer
     */
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Get channel
     *
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Publish to topic
     *
     * @param topic topic
     * @param content message content
     */
    public void publish(String topic, String content) {
        log.debug(String.format("Publishing to topic %s with content %s", topic, content));
        try {
            channel.basicPublish(topic, "", null, content.getBytes("UTF-8"));
            log.debug("Published message");
        } catch (IOException e) {
            log.error("Failed to publish", e);
        }
    }

    /**
     * Create a simple test client subscribing to "example" topic
     *
     * @param args
     */
    public static void main(String[] args) {
        AMQP091Client client = new AMQP091Client();
        client.connect();
        // Callback
        client.setConsumer(new ExampleConsumer(client.getChannel()));
        client.subscribe("example");
        client.publish("example", "Hello, World");
    }

    /**
     * Example consumer class
     */
    private static class ExampleConsumer extends DefaultConsumer {
        public ExampleConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        }
    }
}
