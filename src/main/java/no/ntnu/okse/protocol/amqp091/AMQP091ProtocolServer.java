package no.ntnu.okse.protocol.amqp091;
import fr.dyade.aaa.agent.AgentServer;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;
import org.ow2.joram.mom.amqp.AMQPService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/**
 * Created by Andreas the time lord on 30/03/2516
 */
public class AMQP091ProtocolServer extends AbstractProtocolServer {


    protected static final String SERVERTYPE = "amqp091";
    private AMQP091Service amqpService;


    public AMQP091ProtocolServer(String host, int port) throws IOException {
        this.port = port;
        this.host = host;
        log = Logger.getLogger(AMQP091ProtocolServer.class.getName());
    }

    @Override
    public void boot() {
        if (!_running) {
            _running = true;
            amqpService = new AMQP091Service(this);
            _serverThread = new Thread(this::run);
            _serverThread.setName("AMQ091ProtocolServer");
            _serverThread.start();
            log.info("AMQ091ProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        log.debug(String.format("Starting AMQP 0.9.1 service on %s:%d", host, port));
        amqpService.start();
    }

    @Override
    public void stopServer() {
        amqpService.stop();
        amqpService = null;
        _running = false;
    }

    @Override
    public String getProtocolServerType() {
        return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
        AMQPService.internalPublish(message.getTopic(), "", message.getMessage().getBytes(StandardCharsets.UTF_8));
        incrementMessageSentForTopic(message.getTopic());
    }

    private void incrementMessageSentForTopic(String topic) {
        HashSet<Subscriber> allSubscribers = SubscriptionService.getInstance().getAllSubscribers();
        allSubscribers.stream()
                .filter(subscriber -> subscriber.getOriginProtocol().equals(getProtocolServerType()))
                .filter(subscriber -> subscriber.getTopic().equals(topic))
                .forEach(subscriber -> incrementTotalMessagesSent());
    }
}
