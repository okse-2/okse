package no.ntnu.okse.protocol.amqp091;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import no.ntnu.okse.protocol.ProtocolServer;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class AMQP091ProtocolServer extends AbstractProtocolServer {


    protected static final String SERVERTYPE = "amqp091";
    private AMQP091Service amqpService;
    private SubscriptionService subscriptionService;
    private static AtomicBoolean running = new AtomicBoolean(false);


    public AMQP091ProtocolServer(String host, int port) {
        this.port = port;
        this.host = host;
        log = Logger.getLogger(AMQP091ProtocolServer.class.getName());
        subscriptionService = SubscriptionService.getInstance();
    }

    @Override
    public void boot() {
        if (running.compareAndSet(false, true)) {
            amqpService = new AMQP091Service(this);
            _serverThread = new Thread(this::run);
            _serverThread.setName("AMQ091ProtocolServer");
            _serverThread.start();
            log.info("AMQ091ProtocolServer booted successfully");
        }
        else {
            throw new ProtocolServer.BootErrorException(
                    "Another AMQP 0.9.1 server is already running. Only one server can be running at the same time."
            );
        }
    }

    @Override
    public void run() {
        log.debug(String.format("Starting AMQP 0.9.1 service on %s:%d", host, port));
        amqpService.start();
    }

    @Override
    public void stopServer() {
        if(running.compareAndSet(true, false)) {
            if(amqpService != null) {
                amqpService.stop();
            }
            amqpService = null;
        }
        else {
            log.error("Server was already stopped");
        }
    }

    @Override
    public String getProtocolServerType() {
        return SERVERTYPE;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void sendMessage(Message message) {
        amqpService.sendMessage(message);
        incrementMessageSentForTopic(message.getTopic());
    }

    private void incrementMessageSentForTopic(String topic) {
        HashSet<Subscriber> allSubscribers = subscriptionService.getAllSubscribers();
        allSubscribers.stream()
                .filter(subscriber -> subscriber.getOriginProtocol().equals(getProtocolServerType()))
                .filter(subscriber -> subscriber.getTopic().equals(topic))
                .forEach(subscriber -> incrementTotalMessagesSent());
    }

    void setAmqpService(AMQP091Service amqpService) {
        this.amqpService = amqpService;
    }

    void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
}
