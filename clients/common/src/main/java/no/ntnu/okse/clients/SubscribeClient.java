package no.ntnu.okse.clients;

public abstract class SubscribeClient extends CommandClient {
    public void run() {
        initLogger();
        createClient();
        TestClient client = getClient();
        client.connect();
        topics.forEach(this::subscribe);
        // Graceful disconnect
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                topics.forEach(client::unsubscribe);
                client.disconnect();
            }
        });
        System.out.println("Listening for messages...");
    }

    public void subscribe(String topic) {
        getClient().subscribe(topic);
    }
}
