package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.subscription.Subscriber;

import java.util.*;

public class SubscriberMap {

    private class Client {
        private String host;
        private Integer port;
        private String topic;

        Client(Subscriber subscriber) {
            this.host = subscriber.getHost();
            this.port = subscriber.getPort();
            this.topic = subscriber.getTopic();
        }

        Client(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        Client(String host, Integer port, String topic) {
            this.host = host;
            this.port = port;
            this.topic = topic;
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof Client)) return false;
            Client other = (Client) o;
            return Objects.equals(host, other.host) && Objects.equals(port, other.port);
        }

        public int hashCode() {
            int hash = 1;
            hash = hash * 13 + port;
            hash = hash * 7 + host.hashCode();
            hash = hash * 31 + (topic == null ? 0 : topic.hashCode());
            return hash;
        }
    }

    private Map<Client, Subscriber> topicSubscriberMap = new HashMap<>();
    private Map<Client, List<Subscriber>> subscribersMap = new HashMap<>();

    public void putSubscriber(Subscriber subscriber) {
        Client topicClient = new Client(subscriber);
        topicSubscriberMap.put(topicClient, subscriber);
        Client client = new Client(subscriber.getHost(), subscriber.getPort());
        List<Subscriber> subscribers;
        if(subscribersMap.containsKey(client)) {
            subscribers = subscribersMap.get(client);
        }
        else {
            subscribers = new ArrayList<>();
            subscribersMap.put(client, subscribers);
        }
        subscribers.add(subscriber);
    }

    public Subscriber getSubscriber(String host, int port, String topic) {
        return topicSubscriberMap.get(new Client(host, port, topic));
    }

    public List<Subscriber> getSubscribers(String host, int port) {
        List<Subscriber> subscribers = subscribersMap.get(new Client(host, port));
        if(subscribers == null) {
            subscribers = new ArrayList<>();
        }
        return subscribers;
    }
}
