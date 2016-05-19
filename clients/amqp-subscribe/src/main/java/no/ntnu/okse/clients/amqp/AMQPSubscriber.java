package no.ntnu.okse.clients.amqp;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

public class AMQPSubscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 5672;

    private AMQPClient client;

    public static void main(String[] args) {
        AMQPSubscriber client = new AMQPSubscriber();
        new JCommander(client, args);
        client.run();
    }

    protected void listen() {
        client.setCallback(new Callback());
    }

    protected void createClient() {
        client = new AMQPClient();
    }

    protected TestClient getClient() {
        return client;
    }

    private static class Callback implements AMQPCallback {
        private boolean verbose = false;
        private int counter = 0;

        public void onReceive(Message message) {
            counter++;
            print(counter, message, verbose);
        }
    }

    private static String safe(Object o) {
        return String.valueOf(o);
    }

    private static void print(int i, Message msg, boolean verbose) {
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
}
