package no.ntnu.okse.clients.mqtt;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.PublishClient;
import no.ntnu.okse.clients.TestClient;

public class MQTTPublisher extends PublishClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 1883;
    @Parameter(names = {"--qos", "-q"}, description = "Quality of Service")
    public int qos = 0;

    private MQTTClient client;

    public static void main(String[] args) {
        MQTTPublisher client = new MQTTPublisher();
        new JCommander(client, args);
        client.run();
    }

    protected void createClient() {
        client = new MQTTClient(host, port, "MQTTPublisher");
    }

    public void publish(String topic, String message) {
        client.publish(topic, message, qos);
    }

    protected TestClient getClient() {
        return client;
    }
}
