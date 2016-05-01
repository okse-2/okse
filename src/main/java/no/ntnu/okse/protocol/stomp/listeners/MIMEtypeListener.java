package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.commons.MIMEType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MIMEtypeListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    class CharsetException extends Exception {
        CharsetException(String msg) {
            super(msg);
        }
    }
    
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
        test(stampyMessage, hostPort);
    }

    private void test(StampyMessage<?> stampyMessage, HostPort hostPort) throws CharsetException{
        SendMessage sendMessage = (SendMessage) stampyMessage;
        String contentType = sendMessage.getHeader().getContentType();
        MIMEType mime = new MIMEType(contentType);
        String charset = mime.getCharset();
        if(charset == null)
            return;
        if(charset.equals("")){
            throw new CharsetException("Invalid charset given with the message");
        }
    }
}
