package no.ntnu.okse.clients.wsn;

import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.PublishClient;
import no.ntnu.okse.clients.TestClient;

public class WSNPublisher extends PublishClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 61000;

    private WSNClient client;

    public static void main(String[] args) {
        launch(new WSNPublisher(), args);
    }

    public void createClient() {
        client = new WSNClient(host, port);
    }

    public TestClient getClient() {
        return client;
    }
}
