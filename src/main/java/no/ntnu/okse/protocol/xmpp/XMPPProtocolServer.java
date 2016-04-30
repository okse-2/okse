package no.ntnu.okse.protocol.xmpp;


import java.util.logging.Logger;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;

public class XMPPProtocolServer extends AbstractProtocolServer {
    protected static final String SERVERTYPE = "xmpp";

    no.ntnu.okse.protocol.xmpp.XMPPServer server;

    private static Logger log = Logger.getLogger(XMPPProtocolServer.class.getName());

    public XMPPProtocolServer(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void boot() {
        if (!_running) {
             server = new no.ntnu.okse.protocol.xmpp.XMPPServer(this, host,port);
            _serverThread = new Thread(this::run);
            _serverThread.setName("XMPPProtocolServer");
            _serverThread.start();
            _running = true;
            log.info("XMPPProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {}

    @Override
    public void stopServer() {
        log.info("Stopping XMPPProtocolServer");
        server.stopServer();
        _running = false;
    }

    @Override
    public String getProtocolServerType() {
            return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
        server.sendMessage(message);
    }

    public boolean isRunning() {
        return _running;
    }
}