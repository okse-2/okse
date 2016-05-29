package no.ntnu.okse.clients.wsn;

import com.beust.jcommander.Parameter;
import no.ntnu.okse.clients.SubscribeClient;
import no.ntnu.okse.clients.TestClient;
import org.apache.cxf.wsn.client.Consumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.w3c.dom.Element;

public class WSNSubscriber extends SubscribeClient {
    @Parameter(names = {"--port", "-p"}, description = "Port")
    public int port = 61000;

    @Parameter(names = {"--client-host", "-ch"}, description = "Client Port")
    public String clientHost = "localhost";

    @Parameter(names = {"--client-port", "-cp"}, description = "Client Port")
    public int clientPort = 9000;

    private WSNClient client;

    public static void main(String[] args) {
        launch(new WSNSubscriber(), args);
    }

    protected void createClient() {
        client = new WSNClient(host, port);
        client.setCallback(new WSNConsumer());
    }

    protected TestClient getClient() {
        return client;
    }

    public void subscribe(String topic) {
        client.subscribe(topic, clientHost, clientPort);
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
