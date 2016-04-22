package no.ntnu.okse.protocol.xmpp;


import java.util.logging.Logger;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.vysper.mina.S2SEndpoint;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PublishSubscribeModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.server.ServerFeatures;
import org.apache.vysper.xmpp.server.XMPPServer;

public class XMPPProtocolServer extends AbstractProtocolServer {
    protected static final String SERVERTYPE = "xmpp";

    private static Logger log = Logger.getLogger(XMPPProtocolServer.class.getName());

    public XMPPProtocolServer(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void init() {
        String domain = "okse.ntnu.no";

        StorageProviderRegistry providerRegistry = new OpenStorageProviderRegistry();
        providerRegistry.add(new Authorization());
        providerRegistry.add(new MemoryRosterManager());

        final String pathToTLSCertificate = "/keystore.jks";

        XMPPServer server = new XMPPServer(domain);

        // allow XMPP federation
        server.addEndpoint(new S2SEndpoint());
        // enable classic TCP bases access
        server.addEndpoint(new TCPEndpoint());




        server.setStorageProviderRegistry(providerRegistry);

        server.setTLSCertificateInfo(XMPPProtocolServer.class.getResourceAsStream(pathToTLSCertificate), "password1");

        try {
            server.start();
            System.out.println("vysper server is running...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new VcardTempModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        server.addModule(new PublishSubscribeModule());
    }

    @Override
    public void boot() {
        if (!_running) {
            _serverThread = new Thread(this::run);
            _serverThread.setName("XMPPProtocolServer");
            _serverThread.start();
            _running = true;
            log.info("XMPPProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        init();
    }

    @Override
    public void stopServer() {
        _running = false;
    }

    @Override
    public String getProtocolServerType() {
            return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
    }

    public boolean isRunning() {
        return _running;
    }
}