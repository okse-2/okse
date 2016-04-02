package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class SubscriptionManager {
    private static Logger log;
    private SubscriptionService subscriptionService = null;
    private ConcurrentHashMap<String, Subscriber> localSubscriberMap;
    private ConcurrentHashMap<String, Publisher> localPublisherMap;

    public SubscriptionManager () {
        log = Logger.getLogger(SubscriptionManager.class.getName());
        localSubscriberMap = new ConcurrentHashMap<>();
        localPublisherMap= new ConcurrentHashMap<>();
    }

    public void initCoreSubscriptionService(SubscriptionService subService) {
        this.subscriptionService = subService;
    }

    public void addSubscriber(Subscriber s, String clientID) {
        if(containsSubscriber(clientID)){
            log.warn("This subscriber is already added");
            return;
        }
        subscriptionService.addSubscriber(s);
        log.debug("Adding Subscriber to local mappings: " + clientID);
        localSubscriberMap.put(clientID, s);
    }

    public void removeSubscriber(String clientID){
        if(containsSubscriber(clientID)){
            subscriptionService.removeSubscriber(getSubscriber(clientID));
            localSubscriberMap.remove(clientID);
        }
    }

    public boolean containsSubscriber(String clientID){
        return localSubscriberMap.containsKey(clientID);
    }

    public Subscriber getSubscriber(String clientID){
        return localSubscriberMap.get(clientID);
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

    public HashSet<Subscriber> getAllSubscribersForTopic(String topic){
        return subscriptionService.getInstance().getAllSubscribersForTopic(topic);
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
}
