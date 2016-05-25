package no.ntnu.okse.clients.amqp091;

import com.beust.jcommander.Parameter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;

import java.io.IOException;

public class AMQP091Subscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 56720;

    private AMQP091Client client;

    public static void main(String[] args) {
        launch(new AMQP091Subscriber(), args);
    }

    protected void createClient() {
        client = new AMQP091Client(host, port);
        client.setConsumer(new Callback(client.getChannel()));
    }

    protected TestClient getClient() {
        return client;
    }

    private static class Callback extends DefaultConsumer {
        public Callback(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
            String message = new String(body, "UTF-8");
            System.out.println("Received '" + message + "'");
        }
    }
}
