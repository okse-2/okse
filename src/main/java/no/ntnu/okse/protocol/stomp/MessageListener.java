package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static StompMessageType[] TYPES;

    static {
        TYPES = StompMessageType.values();
    }

    private static final String VERSION = "1.2";

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
        System.out.println("Sub: Message type: " + stampyMessage.getMessageType().toString());
        System.out.println("Message: " + stampyMessage.toString() + "\n");
    }

}
