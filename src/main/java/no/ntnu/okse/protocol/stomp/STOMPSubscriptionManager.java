package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles subscribers
 * It allows us to
 */
public class STOMPSubscriptionManager {
    private static Logger log;
    private SubscriptionService subscriptionService = null;
    public ConcurrentHashMap<String, Subscriber> localSubscriberMap;
    private ConcurrentHashMap<String, Publisher> localPublisherMap;

    /**
     * Setup of variables
     */
    public STOMPSubscriptionManager () {
        log = Logger.getLogger(SubscriptionManager.class.getName());
        localSubscriberMap = new ConcurrentHashMap<>();
        localPublisherMap= new ConcurrentHashMap<>();
    }

    /**
     * Inits the core subscription service. Basically a setter
     * @param subService subscripton service
     */
    public void initCoreSubscriptionService(SubscriptionService subService) {
        this.subscriptionService = subService;
    }

    /**
     * Adds a subscriber to the local map, and to OKSE
     * @param s OKSE subscriber
     * @param clientID Some specific clientID
     */
    public void addSubscriber(Subscriber s, String clientID) {
        if(containsSubscriber(clientID)){
            log.warn("This subscriber is already added");
            return;
        }
        subscriptionService.addSubscriber(s);
        log.debug("Adding Subscriber to local mappings: " + clientID);
        localSubscriberMap.put(clientID, s);
    }

    /**
     * Removes a subscriber
     * @param clientID the client id of the message
     */
    public void removeSubscriber(String clientID){
        if(containsSubscriber(clientID)){
            subscriptionService.removeSubscriber(getSubscriber(clientID));
            localSubscriberMap.remove(clientID);
        }
    }

    /**
     * Removes a subscriber
     * @param host the host of the connection
     * @param port the port of the connection
     */
    public void removeSubscriber(String host, int port){
        Enumeration<String> enum_keys = localSubscriberMap.keys();
        while(enum_keys.hasMoreElements()){
            String key = enum_keys.nextElement();
            Subscriber sub = localSubscriberMap.get(key);
            if(sub.getHost().equals(host) && sub.getPort() == port){
                subscriptionService.removeSubscriber(sub);
                localSubscriberMap.remove(key);
            }
        }
    }

    /**
     * Looks up if the subscriber is already in the local map
     * @param clientID the client id of the conneciton
     * @return
     */
    public boolean containsSubscriber(String clientID){
        return localSubscriberMap.containsKey(clientID);
    }

    /**
     * Gets the subscriber from the local map
     * @param clientID the client id of the connection
     * @return
     */
    public Subscriber getSubscriber(String clientID){
        return localSubscriberMap.get(clientID);
    }

    /**
     * Gets all subscribers for some specific topic
     * @param topic the topic to filter on
     * @return
     */
    public HashMap<String, Subscriber> getAllSubscribersForTopic(String topic){
        HashMap<String, Subscriber> newHashMap = new HashMap<String, Subscriber>();
        Object[] keyArr = localSubscriberMap.keySet().toArray();
        for(int i = 0; i < localSubscriberMap.size(); i++){
            String key = (String)keyArr[i];
            Subscriber sub = localSubscriberMap.get(key);
            if(sub.getTopic().equals(topic)){
                newHashMap.put(key, sub);
            }
        }
        return newHashMap;
    }

}
