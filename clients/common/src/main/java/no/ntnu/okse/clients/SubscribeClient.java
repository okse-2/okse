package no.ntnu.okse.clients;

public abstract class SubscribeClient extends CommandClient {
    public void run() {
        initLogger();
        createClient();
        TestClient client = getClient();
        client.connect();
        topics.forEach(client::subscribe);
        // Graceful disconnect
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                topics.forEach(client::unsubscribe);
                client.disconnect();
            }
        });
        System.out.println("Listening for messages...");
        listen();
    }

    protected abstract void listen();
}
