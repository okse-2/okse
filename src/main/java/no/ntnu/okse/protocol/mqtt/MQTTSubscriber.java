package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.subscription.Subscriber;

/**
 * Created by Ogdans3 on 04.04.2016.
 */
public class MQTTSubscriber {
    String host;
    int port;
    String topic;
    String clientID;
    Subscriber subscriber;

    MQTTSubscriber(String host, int port, String topic, String clientID, Subscriber subscriber){
        this.host = host;
        this.port = port;
        this.topic = topic;
        this.clientID = clientID;
        this.subscriber = subscriber;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getTopic() {
        return topic;
    }

    public String getClientID() {
        return clientID;
    }

    public Subscriber getSubscriber(){
        return this.subscriber;
    }

}
