package no.ntnu.okse.protocol.xmpp;


import java.nio.charset.Charset;
import java.util.logging.Logger;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import no.ntnu.okse.protocol.xmpp.commons.PubSub;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubPublishHandler2;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.vysper.mina.S2SEndpoint;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;

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