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
import no.ntnu.okse.protocol.stomp.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static StompMessageType[] TYPES;
    private ServerNettyMessageGateway gateway;
    private SubscriptionManager subscriptionManager;
    private String protocol;

    static {
        TYPES = new StompMessageType[]{StompMessageType.SEND};
    }

    public MessageListener(){
        this.protocol = "stomp";
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return TYPES;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        System.out.println("Server: Message type: " + stampyMessage.getMessageType().toString());
        System.out.println(stampyMessage.toString() + "\n");
        sendMessage(stampyMessage, hostPort);
    }

    private void sendMessage(StampyMessage<?> stampyMessage, HostPort hostPort) throws InterceptException {
        Subscriber sub = subscriptionManager.getSubscriber("Test");
//        gateway.sendMessage("Test", new HostPort(sub.getHost(), sub.getPort()));
        HostPort subHostPort = new HostPort(sub.getHost(), sub.getPort());

        SendMessage sendMessage = (SendMessage) stampyMessage;
        String destination = sendMessage.getHeader().getDestination();


        Publisher pub = new Publisher(destination, hostPort.getHost(), hostPort.getPort(), protocol);
        //TODO: Need to check if this will work, I think that stomp also uses binary data
        Message okseMsg = new Message((String)sendMessage.getBody(), sendMessage.getHeader().getDestination(), pub, protocol);
        sendMessageToOKSE(okseMsg);
        System.out.println("Meesage sent to OKSE \n");
    }

/*    private void sendMessageToSomeone(){
        String msgId = "Some message ID";
        MessageMessage message = new MessageMessage("destination", msgId, "gabrielb");
        new MessageMessage();


        message.setBody(sendMessage.getBody());
        message.getHeader().setAck(msgId);
        gateway.sendMessage(message, hostPort);
    }*/

    public void sendMessageToOKSE(Message msg){
        MessageService.getInstance().distributeMessage(msg);
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }

    public void setGateway(ServerNettyMessageGateway gateway) {
        this.gateway = gateway;
    }
}
