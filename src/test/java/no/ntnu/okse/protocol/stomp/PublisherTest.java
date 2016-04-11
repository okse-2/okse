package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.common.Client;

public class PublisherTest {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init(null);
        client.testConnect(String.valueOf(0 + (int)(Math.random() * 500)));

        for(int i = 0; i < 1; i++){
            System.out.println("Send message #" + i);
            client.testMessage(String.valueOf(i));
        }
    }
}