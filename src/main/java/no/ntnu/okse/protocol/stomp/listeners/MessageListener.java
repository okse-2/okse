package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import no.ntnu.okse.protocol.stomp.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ServerNettyMessageGateway gateway;
    private SubscriptionManager subscriptionManager;
    private String protocol;
    private STOMPProtocolServer protocolServer;

    public MessageListener(){
        this.protocol = "stomp";
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.SEND};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        sendMessage(stampyMessage, hostPort);
    }

    private void sendMessage(StampyMessage<?> stampyMessage, HostPort hostPort) throws InterceptException {
        SendMessage sendMessage = (SendMessage) stampyMessage;
        String destination = sendMessage.getHeader().getDestination();

        Publisher pub = new Publisher(destination, hostPort.getHost(), hostPort.getPort(), protocol);
        //TODO: Need to check if this will work, I think that stomp also uses binary data
        Message okseMsg = new Message((String)sendMessage.getBody(), sendMessage.getHeader().getDestination(), pub, protocol);
        sendMessageToOKSE(okseMsg);
        protocolServer.incrementTotalMessagesReceived();
    }


    public void sendMessageToOKSE(Message msg){
        MessageService.getInstance().distributeMessage(msg);
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }

    public void setGateway(ServerNettyMessageGateway gateway) {
        this.gateway = gateway;
    }

    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }
}
