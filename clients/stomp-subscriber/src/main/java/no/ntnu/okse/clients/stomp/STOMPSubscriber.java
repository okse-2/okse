package no.ntnu.okse.clients.stomp;

import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;

public class STOMPSubscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 61613;

    private StompClient client;

    public static void main(String[] args) {
        launch(new STOMPSubscriber(), args);
    }

    protected void createClient() {
        client = new StompClient(host, port);
        client.setCallback(new StompCallback());
    }

    protected TestClient getClient() {
        return client;
    }
}
