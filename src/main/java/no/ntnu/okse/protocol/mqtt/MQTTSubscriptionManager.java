package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.event.SubscriptionChangeEvent;
import no.ntnu.okse.core.event.listeners.SubscriptionChangeListener;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.core.topic.TopicService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles subscriptions, will add and remove subscribers based on clientID, a subscriber object or a host, a port and a topic.
 */
public class MQTTSubscriptionManager implements SubscriptionChangeListener {
    private static Logger log;
    private SubscriptionService subscriptionService = null;
    private ArrayList<MQTTSubscriber> subscriberList;

    /**
     * Constructor
     * Instantiates the log
     * Instantiates the local publisher map
     * Instantiates the local subscriber map
     */
    public MQTTSubscriptionManager() {
        log = Logger.getLogger(MQTTSubscriptionManager.class.getName());
        subscriberList = new ArrayList<>();
    }

    /**
     * Sets the subscription service
     * @param subService the subscription service
     */
    public void initCoreSubscriptionService(SubscriptionService subService) {
        this.subscriptionService = subService;
    }

    /**
     * Adds a subscriber to the local map and to the OKSE core
     * @param host the host of the connection
     * @param port the port of the connection
     * @param topic the topic of the subscription
     * @param clientID the clientID of the connection
     */
    public void addSubscriber(String host, int port, String topic, String clientID) {
        if (getSubscriberIndex(host, port, topic) != -1) {
            log.warn("This subscriber is already added");
            return;
        }
        //Create the OKSE sub
        Subscriber sub = new Subscriber(host, port, topic, "mqtt");
        //Create the MQTT sub
        MQTTSubscriber mqttSub = new MQTTSubscriber(host, port, topic, clientID, sub);

        //Add the topic
        TopicService.getInstance().addTopic(topic);
        //Add the subscriber to OKSE
        subscriptionService.addSubscriber(sub);
        //Add the subscriber to the local map
        subscriberList.add(mqttSub);
    }

    /**
     * Removes a subscriber both from OKSE and from the local map
     * @param host the host of the connection
     * @param port the port of the connection
     * @param topic the topic of the subscription
     */
    public void removeSubscriber(String host, int port, String topic) {
        int index = getSubscriberIndex(host, port, topic);
        if (index > -1) {
            subscriptionService.removeSubscriber(subscriberList.get(index).getSubscriber());
            subscriberList.remove(index);
        }
    }

    /**
     * Removes a subscriber both from OKSE and from the local map
     * @param sub the OKSE subscriber instance to remove
     */
    public void removeSubscriber(Subscriber sub) {
        for (int i = 0; i < subscriberList.size(); i++) {
            MQTTSubscriber mqtt_sub = subscriberList.get(i);
            if (mqtt_sub.getSubscriber() == sub) {
                removeSubscriber(mqtt_sub.getHost(), mqtt_sub.getPort(), mqtt_sub.getTopic());
                return;
            }
        }
    }

    /**
     * Removes all subscribers that have subscriber under this clientID
     * @param clientID the clientID, used to remove subscribers
     */
    public void removeSubscribers(String clientID) {
        ArrayList<Integer> list = getSubscriberIndexes(clientID);
        int count = 0;
        for(int i = 0; i < list.size(); i++){
            int index = list.get(i);
            //Each time we delete from the lists we also have to subtract 1 from all the indexes
            //We achieve this easiest by counting the number of the removed subscribers and simply subsctracting
            //that number from the currect index.
            subscriptionService.removeSubscriber(subscriberList.get(index - count).getSubscriber());
            subscriberList.remove(index - count);
            count ++;
        }
    }

    /**
     * Returns the subscriber index, in the local map, based on the host, port and topic given
     * @param host the host of the connection
     * @param port the port of the connection
     * @param topic the topic of the subscription
     * @return the index of that subscriber in the local map
     */
    public int getSubscriberIndex(String host, int port, String topic) {
        for (int i = 0; i < subscriberList.size(); i++) {
            MQTTSubscriber sub = subscriberList.get(i);
            if (sub.getHost().equals(host) && sub.getPort() == port && sub.getTopic().equals(topic))
                return i;
        }
        return -1;
    }

    /**
     * Returns an ArrayList of indexes based on the clientID
     * @param clientID the clientID to return indexes for
     * @return returns an ArrayList of indexes
     */
    public ArrayList<Integer> getSubscriberIndexes(String clientID) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < subscriberList.size(); i++) {
            MQTTSubscriber sub = subscriberList.get(i);
            if (sub.getClientID().equals(clientID))
                indexes.add(i);
        }
        return indexes;
    }

    /**
     * Returns true if a subscriber is created with the same host, port and topic
     * @param host the host for the connection
     * @param port the port for the connection
     * @param topic the topic of the subscription
     * @return returns true if there exists a subscriber with these values.
     */
    public boolean containsSubscriber(String host, int port, String topic) {
        if (getSubscriberIndex(host, port, topic) == -1)
            return false;
        return true;
    }

    /**
     * Returns a subscriber based on the host, port and topic.
     * @param host the host of the connection
     * @param port the port of the connection
     * @param topic the topic of the subscription
     * @return returns the MQTTSubscriber instance
     */
    public MQTTSubscriber getSubscriber(String host, int port, String topic) {
        int index = getSubscriberIndex(host, port, topic);
        if (index == -1)
            return null;
        return subscriberList.get(index);
    }

    /**
     * Returns all subscribers from a certain topic
     * @param topic the topic to return subscribers for
     * @return returns an ArrayList of MQTTSubscriber instances
     */
    public ArrayList<MQTTSubscriber> getAllSubscribersFromTopic(String topic) {
        ArrayList<MQTTSubscriber> subscribers = new ArrayList<MQTTSubscriber>();
        for (int i = 0; i < subscriberList.size(); i++) {
            MQTTSubscriber sub = subscriberList.get(i);
            if (sub.getTopic().equals(topic)) {
                subscribers.add(sub);
            }
        }
        return subscribers;
    }

    @Override
    public void subscriptionChanged(SubscriptionChangeEvent e) {
        if (e.getData().getOriginProtocol().equals("mqtt")) {
            if (e.getType().equals(SubscriptionChangeEvent.Type.UNSUBSCRIBE)) {
                log.debug("Received a UNSUBSCRIBE event");
                removeSubscriber(e.getData());
            }
        }
    }
}
