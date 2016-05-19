package no.ntnu.okse.clients.amqp091;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.PublishClient;
import no.ntnu.okse.clients.TestClient;

public class AMQP091Publisher extends PublishClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 56720;

    private AMQP091Client client;

    public static void main(String[] args) {
        AMQP091Publisher client = new AMQP091Publisher();
        new JCommander(client, args);
        client.run();
    }

    protected void createClient() {
        client = new AMQP091Client(host, port);
    }

    protected TestClient getClient() {
        return client;
    }
}
