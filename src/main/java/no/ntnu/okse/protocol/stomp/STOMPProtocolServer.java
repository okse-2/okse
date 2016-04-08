package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.message.interceptor.InterceptException;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;

public class STOMPProtocolServer extends AbstractProtocolServer {

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final Integer DEFAULT_PORT = 61613;

    public STOMPProtocolServer(String host, Integer port) {
        this.host = host;
        this.port = port;

        protocolServerType = "stomp";
        log = Logger.getLogger(STOMPProtocolServer.class.getName());
    }

    private STOMPServer server;

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
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager ();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        try {
            server.setSubscriptionManager(subscriptionManager);
            server.init(host, port);
            server.setProtocolServer(this);
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            server.sendMessage(message);
        } catch (InterceptException e) {
            log.error("Exception was thrown when sending message to the server", e);
            log.debug(e);
        }
    }

    public void setServer(STOMPServer server){
        this.server = server;
    }

    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementTotalBadRequests() {
        totalBadRequests.incrementAndGet();
    }

    public void incrementTotalErrors() {
        totalErrors.incrementAndGet();
    }

    public void incrementTotalMessagesReceived() {
        totalMessagesReceived.incrementAndGet();
    }

    public void incrementTotalMessagesSent() {
        totalMessagesSent.incrementAndGet();
    }



    public boolean isRunning() {
        return _running;
    }

}
