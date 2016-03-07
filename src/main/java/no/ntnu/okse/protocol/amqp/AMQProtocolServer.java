/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Norwegian Defence Research Establishment / NTNU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package no.ntnu.okse.protocol.amqp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;
import org.apache.qpid.proton.engine.Collector;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

public class AMQProtocolServer extends AbstractProtocolServer {

    protected static final String SERVERTYPE = "amqp";

    private Logger log;
    private Thread _serverThread;

    private SubscriptionHandler sh;
    private boolean shuttingdown = false;

    public boolean useQueue;
    protected boolean useSASL;

    private Driver driver;

    /**
     * Constructor that takes in configuration options for the AMQProtocolServer
     * server.
     * <p>
     *
     * @param host A String representing the host the WSNServer should bind to
     * @param port An int representing the port the WSNServer should bind to.
     * @param queue A boolean specifying whether to use queueing behaviour
     * @param sasl A boolean specifying whether to use SASL for its connections
     */
    public AMQProtocolServer(String host, int port, boolean queue, boolean sasl) {
        useQueue = queue;
        useSASL = sasl;
        this.port = port;
        this.host = host;
        log = Logger.getLogger(AMQProtocolServer.class.getName());
    }

    @Override
    public void boot() {
        if (!_running) {
            _running = true;
            Collector collector = Collector.Factory.create();
            this.sh = new SubscriptionHandler(this);
            SubscriptionService.getInstance().addSubscriptionChangeListener(sh);
            server = new AMQPServer(this, sh, false);
            try {
                driver = new Driver(this, collector, new Handshaker(),
                        new FlowController(1024), sh,
                        server);
                driver.listen(this.host, this.port);
            } catch(UnresolvedAddressException e) {
                throw new BootErrorException("Unresolved address");
            } catch (IOException e) {
                throw new BootErrorException("Unable to bind to " + host + ":" + port);
            }
            _serverThread = new Thread(() -> this.run());
            _serverThread.setName("AMQProtocolServer");
            _serverThread.start();
            log.info("AMQProtocolServer booted successfully");
        }

    }

    @Override
    public void run() {
        try {
            driver.run();
        } catch (IOException e) {
            totalErrors.incrementAndGet();
            log.error("I/O exception during accept(): " + e.getMessage());
        }
    }

    @Override
    public void stopServer() {
        log.info("Stopping AMQProtocolServer");
        shuttingdown = true;
        driver.stop();
        sh.unsubscribeAll();
        sh = null;
        _running = false;
        server = null;
        driver = null;
        log.info("AMQProtocolServer is stopped");
    }

    @Override
    public String getProtocolServerType() {
        return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
        if (!message.getOriginProtocol().equals(protocolServerType) || message.getAttribute("duplicate") != null) {
            server.addMessageToQueue(message);
        }
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

    private AMQPServer server;

    public Driver getDriver() {
        return driver;
    }

    public SubscriptionHandler getSubscriptionHandler() {
        return sh;
    }

    public boolean isShuttingDown() {
        return shuttingdown;
    }
}
