package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.subscription.Subscriber;

/**
 * This class is represents a subscriber for MQTT
 */
public class MQTTSubscriber {
    String host;
    int port;
    String topic;
    String clientID;
    Subscriber subscriber;

    /**
     * Creates the MQTT subscriber
     * @param host the host of the subscriber
     * @param port the port of the subscriber
     * @param topic the topic of the subscriber
     * @param clientID the clientID of the subscriber
     * @param subscriber the OKSE subscriber instance of the subscriber
     */
    MQTTSubscriber(String host, int port, String topic, String clientID, Subscriber subscriber) {
        this.host = host;
        this.port = port;
        this.topic = topic;
        this.clientID = clientID;
        this.subscriber = subscriber;
    }

    /**
     * @return returns the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return returns the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return returns the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return returns the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @return returns the subscriber
     */
    public Subscriber getSubscriber() {
        return this.subscriber;
    }

}
