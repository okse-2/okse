package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.common.Client;
import no.ntnu.okse.protocol.stomp.common.MessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MIMETypeTest {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init(new MessageListener("mime type"));
        client.testConnect(String.valueOf(0 + (int)(Math.random() * 500)));

        ArrayList<List<String>> mimetypes = new ArrayList<>();

        mimetypes.add(Arrays.asList("text/plain;charset=", ""));
        mimetypes.add(Arrays.asList("text/plain;charset=UTF-8", "UTF-8"));
        mimetypes.add(Arrays.asList("text/html;charset=UTF-16", "UTF-16"));
//        mimetypes.add(Arrays.asList("application/javascript;charset=ISO-8859-1", "ISO-8859-1"));
//        mimetypes.add(Arrays.asList("image/png", ""));

        for(int i = 0; i < 1; i++){
            System.out.println("Send message #" + i);
            int index = (int) Math.floor(Math.random() * mimetypes.size());
            index = 1;
            System.out.println(mimetypes.get(index).get(0));
            client.testMIMEtype(String.valueOf(i), mimetypes.get(index).get(0), mimetypes.get(index).get(1));
        }
    }
}