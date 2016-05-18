package no.ntnu.okse.clients.wsn;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;
import org.apache.cxf.wsn.client.Consumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.w3c.dom.Element;

public class WSNSubscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 61000;

    private WSNClient client;

    public static void main(String[] args) {
        WSNSubscriber client = new WSNSubscriber();
        new JCommander(client, args);
        client.run();
    }

    protected void createClient() {
        client = new WSNClient(host, port);
    }

    protected TestClient getClient() {
        return client;
    }

    protected void listen() {
        client.setCallback(new WSNConsumer());
    }

    private class WSNConsumer implements Consumer.Callback {
        public void notify(NotificationMessageHolderType message) {
            Object o = message.getMessage().getAny();
            if (o instanceof Element) {
                System.out.println(((Element)o).getTextContent());
            }
        }
    }
}
