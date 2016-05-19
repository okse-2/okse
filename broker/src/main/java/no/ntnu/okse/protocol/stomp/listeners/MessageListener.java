package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.topic.TopicService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class will listen to the MESSAGE message type
 * and whenever one is received it will send that message into
 * OKSE. It also has special handling for adding any user defined
 * headers to the OKSE message.
 *
 * Also increments the total number of messages received
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String protocol;
    private STOMPProtocolServer protocolServer;
    private MessageService messageService;

    /**
     * Constructor for the class, simply sets the protocol type
     */
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
        SendMessage sendMessage = (SendMessage) stampyMessage;
        String destination = sendMessage.getHeader().getDestination();

        TopicService.getInstance().addTopic(destination);

        Message okseMsg = new Message((String)sendMessage.getBody(), destination, null, protocol);

        Map<String, List<String>> headers = sendMessage.getHeader().getHeaders();
        Iterator it = headers.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            //Skip the headers that are in the STOMP specification
            //We do not skip content-type and content-length as we want to send these
            //with the message when it comes back from OKSE
            switch(key){
                case "destination":
                case "receipt":
                case "transaction":
                    break;
                default:
                    okseMsg.setAttribute(key, String.join(",", headers.get(key)));
                    break;
            }
        }

        sendMessageToOKSE(okseMsg);
        protocolServer.incrementTotalMessagesReceived();
    }

    /**
     * Sends a message into OKSE
     * @param msg the message to send
     */
    private void sendMessageToOKSE(Message msg){
        messageService.distributeMessage(msg);
    }

    /**
     * Sets the message service for the class, used for sending message
     * into OKSE
     * @param instance the message service instance
     */
    public void setMessageService(MessageService instance){
        messageService = instance;
    }

    /**
     * Sets the protocol server, used to increment total number of message
     * received
     * @param protocolServer the protocol server
     */
    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }
}
