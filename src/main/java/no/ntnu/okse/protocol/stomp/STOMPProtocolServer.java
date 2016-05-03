package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;

public class STOMPProtocolServer extends AbstractProtocolServer {

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final Integer DEFAULT_PORT = 61613;

    private STOMPServer server;

    /**
     * Constructor for the class, sets the host and port for the gateway
     * Also initiates the logger and sets the server type to stomp
     * @param host the host to listen to
     * @param port the port to listen to
     */
    public STOMPProtocolServer(String host, Integer port) {
        this.host = host;
        this.port = port;

        protocolServerType = "stomp";
        log = Logger.getLogger(STOMPProtocolServer.class.getName());
    }


    @Override
    public void boot() {
        if(!_running) {
            server = new STOMPServer();
            _serverThread = new Thread(this::run);
            _serverThread.setName("STOMPProtocolServer");
            _serverThread.start();
            _running = true;
            log.info("STOMPProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        try {
            server.setSubscriptionManager(subscriptionManager);
            server.setProtocolServer(this);
            server.init(host, port);
        } catch (Exception e) {
            log.error("Error thrown when trying to initialize the server", e);
        }
    }

    @Override
    public void stopServer() {
        log.info("Stopping STOMPProtocolServer");
        server.stopServer();
        _running = false;
        server = null;
        log.info("STOMPProtocolServer is stopped");
    }

    @Override
    public String getProtocolServerType() {
        return protocolServerType;
    }

    @Override
    public void sendMessage(Message message) {
        log.info("Received message on topic " + message.getTopic() );
        server.sendMessage(message);
    }

    /**
     * Sets the server, this is used for testing and debugging
     * @param server
     */
    public void setServer(STOMPServer server){
        this.server = server;
    }

    /**
     * Increments the total number of requests by 1
     */
    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    /**
     * Increments the total number of bad requests by 1
     */
    public void incrementTotalBadRequests() {
        totalBadRequests.incrementAndGet();
    }

    /**
     * Increments the total number of errors by 1
     */
    public void incrementTotalErrors() {
        totalErrors.incrementAndGet();
    }

    /**
     * Increments the total number of messages received by 1
     */
    public void incrementTotalMessagesReceived() {
        totalMessagesReceived.incrementAndGet();
    }

    /**
     * Increments the total number of messages sent by 1
     */
    public void incrementTotalMessagesSent() {
        totalMessagesSent.incrementAndGet();
    }

    /**
     * Returns whether the server is running or not
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return _running;
    }

}
