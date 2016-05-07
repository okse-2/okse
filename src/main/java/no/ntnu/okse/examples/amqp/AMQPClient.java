package no.ntnu.okse.examples.amqp;

import no.ntnu.okse.examples.TestClient;
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
    private boolean verbose = false;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private Messenger messenger;
    private String host;
    private int port;

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
            messenger.start();
        } catch (IOException e) {
            log.error("Failed to start client", e);
        }
    }

    @Override
    public void disconnect() {
        messenger.stop();
    }

    @Override
    public void subscribe(String topic) {
        messenger.subscribe(createAddress(topic));
    }

    @Override
    public void unsubscribe(String topic) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void publish(String topic, String content) {
        messenger.put(createMessage(topic, content));
    }

    public void listen() {
        int counter = 0;
        while(true) {
            messenger.recv();
            while (messenger.incoming() > 0) {
                counter++;
                Message message = messenger.get();
                print(counter, message);
            }
        }
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

    private static String safe(Object o) {
        return String.valueOf(o);
    }

    private void print(int i, Message msg) {
        StringBuilder b = new StringBuilder("message: ");
        b.append(i).append("\n");
        b.append("Address: ").append(msg.getAddress()).append("\n");
        b.append("Subject: ").append(msg.getSubject()).append("\n");
        if (verbose) {
            b.append("Props:     ").append(msg.getProperties()).append("\n");
            b.append("App Props: ").append(msg.getApplicationProperties()).append("\n");
            b.append("Msg Anno:  ").append(msg.getMessageAnnotations()).append("\n");
            b.append("Del Anno:  ").append(msg.getDeliveryAnnotations()).append("\n");
        } else {
            ApplicationProperties p = msg.getApplicationProperties();
            String s = (p == null) ? "null" : safe(p.getValue());
            b.append("Headers: ").append(s).append("\n");
        }
        b.append(msg.getBody()).append("\n");
        b.append("END").append("\n");
        System.out.println(b.toString());
    }

    public static void main(String args[]) {
        AMQPClient client = new AMQPClient();
        client.connect();
        client.subscribe("example");
        client.publish("example", "Hello, World");
        client.listen();
    }
}
