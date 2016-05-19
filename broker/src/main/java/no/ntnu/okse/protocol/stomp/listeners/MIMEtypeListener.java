package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.commons.MIMEType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.ntnu.okse.protocol.stomp.commons.MIMETypeException;

import java.lang.invoke.MethodHandles;

/**
 * This class listens for the MIME types on incoming messages.
 * This class can throw a mime type exception if an invalid mime type is given with a message
 */
public class MIMEtypeListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.SEND};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        validate(stampyMessage);
    }

    /**
     * This method validates a message's MIME type
     * @param stampyMessage The message which should be validated
     * @throws MIMETypeException throws a mime type exception if the mime type is not valid
     */
    private void validate(StampyMessage<?> stampyMessage) throws MIMETypeException{
        SendMessage sendMessage = (SendMessage) stampyMessage;
        String contentType = sendMessage.getHeader().getContentType();
        MIMEType mime = new MIMEType(contentType);
        if(!mime.isValid()){
            throw new MIMETypeException("Invalid charset given with the message");
        }
    }
}
