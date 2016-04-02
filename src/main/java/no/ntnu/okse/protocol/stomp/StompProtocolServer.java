package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import no.ntnu.okse.protocol.mqtt.MQTTServer;
import no.ntnu.okse.protocol.mqtt.MQTTSubscriptionManager;
import org.apache.log4j.Logger;

/**
 * Created by ogdans3 on 3/30/16.
 */
public class STOMPProtocolServer extends AbstractProtocolServer {

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final Integer DEFAULT_PORT = 1883;

    private static boolean _invoked = false;
    private static STOMPProtocolServer _singleton = null;

    private STOMPProtocolServer(String host, Integer port) {
        init(host, port);
    }

    private MQTTServer server;

    @Override
    protected void init(String host, Integer port) {
        this.host = host;
        this.port = port;
        _invoked = true;
        protocolServerType = "MQTT";
        log = Logger.getLogger(STOMPProtocolServer.class.getName());
    }

    public static STOMPProtocolServer getInstance() {
        if(!_invoked) {
            _singleton = new STOMPProtocolServer(DEFAULT_HOST, DEFAULT_PORT);
        }
        return _singleton;
    }

    public static STOMPProtocolServer getInstance(String host, Integer port) {
        if(!_invoked) {
            _singleton = new STOMPProtocolServer(host, port);
        }
        return _singleton;
    }


    @Override
    public void boot() {
        if(!_running) {
            _running = true;
            _serverThread = new Thread(this::run);
            _serverThread.setName("MQTTProtocolServer");
            _serverThread.start();
            log.info("MQTTProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        server = new MQTTServer();
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        server.init(host, port);
        server.setSubscriptionManager(subscriptionManager);
    }

    @Override
    public void stopServer() {
        log.info("Stopping MQTTProtocolServer");
        _running = false;
        _singleton = null;
        _invoked = false;
        server.stopServer();
        server = null;
        log.info("MQTTProtocolServer is stopped");
    }

    @Override
    public String getProtocolServerType() {
        return protocolServerType;
    }

    @Override
    public void sendMessage(Message message) {
        log.info("Received message on topic " + message.getMessage() );
        server.sendMessage( message );

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
