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
    private TCPEndpoint tcpEndpoint;
    private ServerRuntimeContext serverRuntimeContext;
    private PubSubPublishHandler2 pubSubPublishHandler2;
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
        tcpEndpoint = new TCPEndpoint();
        tcpEndpoint.setPort(port);

        log.info("XMPP port it set to " + port);
        server.addEndpoint(tcpEndpoint);
        server.setStorageProviderRegistry(providerRegistry);
        server.setTLSCertificateInfo(XMPPProtocolServer.class.getResourceAsStream(pathToTLSCertificate), "password1");

        try {
            server.start();
            System.out.println("vysper server is running...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ProtocolCodecFilter codecFilter = new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" )));
        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new VcardTempModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        PubSub publishSubscribeModule = new PubSub();
        server.addModule(publishSubscribeModule);
        pubSubPublishHandler2 = publishSubscribeModule.getPubSubPublishHandler2();
        serverRuntimeContext = publishSubscribeModule.getServerRuntimeContext();

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
        pubSubPublishHandler2.publishXMPPmessage(message, serverRuntimeContext);
    }

    public boolean isRunning() {
        return _running;
    }
}