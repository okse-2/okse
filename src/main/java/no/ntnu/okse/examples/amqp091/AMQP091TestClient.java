package no.ntnu.okse.examples.amqp091;

import com.rabbitmq.client.*;
import no.ntnu.okse.examples.TestClient;
import no.ntnu.okse.examples.mqtt.MQTTTestClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * AMQP 0.9.1 Test Client
 */
public class AMQP091TestClient implements TestClient {
    private static Logger log = Logger.getLogger(MQTTTestClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 56720;
    private Channel channel;
    private Consumer consumer;
    private ConnectionFactory factory;
    private String queueName;

    /**
     * Create an instance of test client with default configuration
     */
    public AMQP091TestClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Create an instance of test client
     *
     * @param host host
     * @param port port
     */
    public AMQP091TestClient(String host, int port) {

        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

    }

    /**
     * Connect to the broker
     */
    public void connect() {
        try {
            log.debug("Connecting to broker");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            log.debug("Connected to broker");
        } catch (IOException | TimeoutException e) {
            log.error("Unable to connect to broker", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            channel.close();
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
            channel.exchangeDeclare(topic, "fanout");
            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, topic, "");
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            log.error("Unable to subscribe to topic", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        if(queueName != null) {
            try {
                channel.basicCancel(queueName);
            } catch (IOException e) {
                log.error("Failed to unsubscribe", e);
            }
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
        try {
            channel.basicPublish(topic, "", null, content.getBytes("UTF-8"));
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
        AMQP091TestClient client = new AMQP091TestClient();
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
