package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class STOMPSubscriptionManager {
    private static Logger log;
    private SubscriptionService subscriptionService = null;
    public ConcurrentHashMap<String, Subscriber> localSubscriberMap;
    private ConcurrentHashMap<String, Publisher> localPublisherMap;

    public STOMPSubscriptionManager () {
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

    public HashMap<String, Subscriber> getAllSubscribersForTopic(String topic){
        localSubscriberMap.size();
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
