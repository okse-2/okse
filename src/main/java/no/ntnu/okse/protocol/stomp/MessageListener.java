package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.message.MessageMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String id;

    MessageListener(String id){
        this.id = id;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.MESSAGE};
    }
    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
//        System.out.println("Message: " + stampyMessage.toString() + "\n");
//        System.out.println("\n\n\n\n");
        System.out.println("Subscriber: " + id + "\n" + "Message says: " + ((MessageMessage)stampyMessage).getBody() + "\n");
//        System.out.println("\n\n\n\n");
    }

}
