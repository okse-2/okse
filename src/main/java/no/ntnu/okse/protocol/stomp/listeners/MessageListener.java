package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String protocol;
    private STOMPProtocolServer protocolServer;
    private MessageService messageService;

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

        //TODO: Stomp uses mime types and can send any data. Needs to be handled, will send an email to FFI about this issue
        Message okseMsg = new Message((String)sendMessage.getBody(), destination, null, protocol);
        sendMessageToOKSE(okseMsg);
        protocolServer.incrementTotalMessagesReceived();
    }

    private void sendMessageToOKSE(Message msg){
        messageService.distributeMessage(msg);
    }
    public void setMessageService(MessageService instance){
        messageService = instance;
    }
    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }
}
