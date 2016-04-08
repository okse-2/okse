package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;

public class UnSubscriptionListener implements StampyMessageListener {
    private STOMPSubscriptionManager subscriptionManager;

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.UNSUBSCRIBE};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        UnsubscribeMessage unsubMessage = (UnsubscribeMessage )stampyMessage;
        subscriptionManager.removeSubscriber(unsubMessage.getHeader().getId());
    }

    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }
}
