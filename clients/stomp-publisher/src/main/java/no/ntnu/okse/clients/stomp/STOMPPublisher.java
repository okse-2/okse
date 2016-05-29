package no.ntnu.okse.clients.stomp;

import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.PublishClient;
import no.ntnu.okse.clients.TestClient;

public class STOMPPublisher extends PublishClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 61613;

    private StompClient client;

    public static void main(String[] args) {
        launch(new STOMPPublisher(), args);
    }

    protected void createClient() {
        client = new StompClient(host, port);
    }

    protected TestClient getClient() {
        return client;
    }
}
