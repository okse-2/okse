package no.ntnu.okse.clients.stomp;

import asia.stampy.server.message.message.MessageMessage;

public class StompCallback {

    public void messageReceived(MessageMessage message) {
        String topic = message.getHeader().getDestination();
        String content = message.getBody().toString();
        System.out.println(String.format("[%s] %s", topic, content));
    }
}
