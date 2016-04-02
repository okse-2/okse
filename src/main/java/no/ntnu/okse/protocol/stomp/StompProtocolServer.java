package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;

import no.ntnu.okse.protocol.amqp.AMQPServer;
import no.ntnu.okse.protocol.amqp.Driver;
import no.ntnu.okse.protocol.amqp.SubscriptionHandler;
import org.apache.log4j.Logger;

import static org.bouncycastle.crypto.tls.ConnectionEnd.server;

public class StompProtocolServer extends AbstractProtocolServer {

    protected static final String SERVERTYPE = "stomp";

    private Logger log;
    private Thread _serverThread;

    public StompProtocolServer(String host, int port) {
        this.host = host;
        this.port = port;
        log = Logger.getLogger(StompProtocolServer.class.getName());
    }

    @Override
    public void boot() {
        if (!_running) {
            _serverThread = new Thread(this::run);
            _serverThread.setName("StompProtocolServer");
            _serverThread.start();
            _running = true;
            log.info("StompProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        System.out.println("stomp thread is running");
        while(true){

        }
    }

    @Override
    public void stopServer() {
        log.info("Stopping MQTTProtocolServer");
        _running = false;
        log.info("MQTTProtocolServer is stopped");
    }

    @Override
    public String getProtocolServerType() {
        return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
    }

    public void incrementTotalMessagesSent() {
        totalMessagesSent.incrementAndGet();
    }

    public void incrementTotalMessagesReceived() {
        totalMessagesReceived.incrementAndGet();
    }

    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementTotalBadRequest() {
        totalBadRequests.incrementAndGet();
    }

    public void incrementTotalErrors() {
        totalErrors.incrementAndGet();
    }

    public void decrementTotalErrors() {
        totalErrors.decrementAndGet();
    }

    public boolean isRunning() {
        return _running;
    }
}