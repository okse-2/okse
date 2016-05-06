package no.ntnu.okse.examples;

public interface TestClient {
    /**
     * Connect to broker
     */
    void connect();

    /**
     * Disconnect from broker
     */
    void disconnect();

    /**
     * Subscribe to topic
     *
     * @param topic topic
     */
    void subscribe(String topic);

    /**
     * Unsubscribe from topic
     *
     * @param topic topic
     */
    void unsubscribe(String topic);

    /**
     * Publish to topic
     *
     * @param topic topic
     * @param content message content
     */
    void publish(String topic, String content);
}
