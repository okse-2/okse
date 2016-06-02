package no.ntnu.okse.clients.amqp091;

import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;

public class AMQP091Subscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 56720;

    private AMQP091Client client;

    public static void main(String[] args) {
        launch(new AMQP091Subscriber(), args);
    }

    protected void createClient() {
        client = new AMQP091Client(host, port);
        client.setCallback(new SubscriberCallback());
    }

    protected TestClient getClient() {
        return client;
    }

    private class SubscriberCallback implements AMQP091Callback {
        private int messagesReceived = 0;

        public void messageReceived(String topic, String message) {
            System.out.println(String.format("#%d [%s] %s", ++messagesReceived, topic, message));
        }
    }
}
