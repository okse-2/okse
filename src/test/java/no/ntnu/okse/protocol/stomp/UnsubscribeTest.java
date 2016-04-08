package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.common.Client;

public class UnsubscribeTest {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init(new no.ntnu.okse.protocol.stomp.listeners.MessageListener());
        client.testConnect(String.valueOf(0 + (int)(Math.random() * 500)));
        System.out.println("Subscribe");
        client.testSubscription("gabrielb", "test");
        Thread.sleep(1000);
        System.out.println("UnSubscribe");
        client.testUnsubscribe("gabrielb");
        Thread.sleep(1000);
        System.out.println("Subscribe");
        client.testSubscription("gabrielb", "test");

        System.out.println("Set up complete");
    }
}
