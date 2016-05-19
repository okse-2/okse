package no.ntnu.okse.clients.amqp;

import no.ntnu.okse.clients.TestClient;
import org.apache.log4j.Logger;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;

import java.io.IOException;

/**
 * AMQP 1.0 Test Client
 */
public class AMQPClient implements TestClient {
    private static Logger log = Logger.getLogger(AMQPClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private Messenger messenger;
    private String host;
    private int port;
    private AMQPCallback callback;
    private AMQPClientListener listener;

    /**
     * Create an instance of test client
     *
     * @param host hostname
     * @param port port
     */
    public AMQPClient(String host, int port) {
        this.host = host;
        this.port = port;
        messenger = Messenger.Factory.create();
        messenger.setTimeout(10000);
        listener = new AMQPClientListener();
    }

    /**
     * Create an instance of test client with default configuration
     */
    public AMQPClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    @Override
    public void connect() {
        try {
            log.debug("Connecting to broker");
            messenger.start();
            log.debug("Connected to broker");
        } catch (IOException e) {
            log.error("Failed to start client", e);
        }
    }

    @Override
    public void disconnect() {
        log.debug("Disconnecting");
        listener.stopListener();
        messenger.stop();
        log.debug("Disconnected");
    }

    @Override
    public void subscribe(String topic) {
        log.debug("Subscribing to topic: " + topic);
        messenger.subscribe(createAddress(topic));
        listener.start();
        log.debug("Subscribed to topic: " + topic);
    }

    @Override
    public void unsubscribe(String topic) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void publish(String topic, String content) {
        log.debug(String.format("Publishing to topic %s with content %s", topic, content));
        messenger.put(createMessage(topic, content));
        messenger.send();
        log.debug("Successfully published");
    }

    public void setCallback(AMQPCallback callback) {
        this.callback = callback;
    }

    private Message createMessage(String topic, String content) {
        Message message = Message.Factory.create();
        message.setAddress(createAddress(topic));
        // TODO: Unsure how subject works
        message.setSubject(topic);
        message.setBody(new AmqpValue(content));
        return message;
    }

    private String createAddress(String topic) {
        return String.format("amqp://%s:%d/%s", host, port, topic);
    }

    private class AMQPClientListener extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            running = true;
            while(running) {
                messenger.recv();
                while(messenger.incoming() > 0) {
                    callback.onReceive(messenger.get());
                }
            }
        }

        public void stopListener() {
            running = false;
        }
    }
}
