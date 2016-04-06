package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.event.SubscriptionChangeEvent;
import no.ntnu.okse.core.event.listeners.SubscriptionChangeListener;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.wsn.WSNotificationServer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ogdans3 on 3/16/16.
 */
public class MQTTSubscriptionManager implements SubscriptionChangeListener {
    private static Logger log;
    private SubscriptionService subscriptionService = null;
    private ConcurrentHashMap<String, Publisher> localPublisherMap;
    private ArrayList<MQTTSubscriber> subscriberList;

    public MQTTSubscriptionManager () {
        log = Logger.getLogger(MQTTSubscriptionManager.class.getName());
        localPublisherMap= new ConcurrentHashMap<>();
        subscriberList = new ArrayList<MQTTSubscriber>();
    }

    public void initCoreSubscriptionService(SubscriptionService subService) {
        this.subscriptionService = subService;
    }

    public void addSubscriber(String host, int port, String topic, String clientID) {
        if(getSubscriberIndex(host, port, topic) != -1){
            log.warn("This subscriber is already added");
            return;
        }
        Subscriber sub = new Subscriber( host, port, topic, "mqtt");
        MQTTSubscriber mqttSub = new MQTTSubscriber(host, port, topic, clientID, sub);

        subscriptionService.addSubscriber(sub);
        subscriberList.add(mqttSub);
    }

    public void removeSubscriber(String host, int port, String topic){
        int index = getSubscriberIndex(host, port, topic);
        if(index > -1){
            subscriptionService.removeSubscriber(subscriberList.get(index).getSubscriber());
            subscriberList.remove(index);
        }
    }

    public void removeSubscriber(Subscriber sub){
        for(int i = 0; i < subscriberList.size(); i++){
            MQTTSubscriber mqtt_sub = subscriberList.get(i);
            if(mqtt_sub.getSubscriber() == sub){
                removeSubscriber(mqtt_sub.getHost(), mqtt_sub.getPort(), mqtt_sub.getTopic());
                return;
            }
        }
    }

    public void removeSubscribers(String clientID){
        ArrayList<Integer> indexes = getSubscriberIndexes(clientID);
        for(int i = indexes.size() - 1; i >= 0; i--){
            int index = indexes.get(i);
            subscriptionService.removeSubscriber(subscriberList.get(index).getSubscriber());
            subscriberList.remove(index);
        }
    }

    public int getSubscriberIndex(String host, int port, String topic){
        for(int i = 0; i < subscriberList.size(); i++){
            MQTTSubscriber sub = subscriberList.get(i);
            if(sub.getHost().equals(host) && sub.getPort() == port && sub.getTopic().equals(topic))
                return i;
        }
        return -1;
    }
    public ArrayList<Integer> getSubscriberIndexes(String clientID){
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for(int i = 0; i < subscriberList.size(); i++){
            MQTTSubscriber sub = subscriberList.get(i);
            if(sub.getClientID().equals(clientID))
                indexes.add(i);
        }
        return indexes;
    }

    public boolean containsSubscriber(String host, int port, String topic){
        if(getSubscriberIndex(host, port, topic) == -1)
            return false;
        return true;
    }

    public MQTTSubscriber getSubscriber(String host, int port, String topic){
        int index = getSubscriberIndex(host, port, topic);
        if(index == -1)
            return null;
        return subscriberList.get(index);
    }

    public ArrayList<MQTTSubscriber> getAllSubscribersFromTopic(String topic){
        ArrayList<MQTTSubscriber> subscribers = new ArrayList<MQTTSubscriber>();
        for(int i = 0; i < subscriberList.size(); i++){
            MQTTSubscriber sub = subscriberList.get(i);
            if(sub.getTopic().equals(topic)){
                subscribers.add(sub);
            }
        }
        return subscribers;
    }

    public void addPublisher(Publisher p, String clientID) {
        if(containsPublisher(clientID)){
            log.warn("This publisher is already added");
            return;
        }
        subscriptionService.addPublisher(p);
        log.debug("Adding Subscriber to local mappings: " + clientID);
        localPublisherMap.put(clientID, p);
    }


    public void removePublisher(String clientID) {
        if(containsPublisher(clientID)){
            subscriptionService.removePublisher(getPublisher(clientID));
            localPublisherMap.remove(clientID);
        }
    }

    public boolean containsPublisher(String clientID) {
        return localPublisherMap.containsKey(clientID);
    }

    public Publisher getPublisher(String clientID){
        return localPublisherMap.get(clientID);
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
