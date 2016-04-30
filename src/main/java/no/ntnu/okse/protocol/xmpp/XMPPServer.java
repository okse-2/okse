package no.ntnu.okse.protocol.xmpp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.protocol.xmpp.commons.PubSub;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubPublishHandler2;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.vysper.mina.S2SEndpoint;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class XMPPServer {

    private static Logger log = Logger.getLogger(XMPPProtocolServer.class.getName());
    private TCPEndpoint tcpEndpoint;
    private ServerRuntimeContext serverRuntimeContext;
    private PubSubPublishHandler2 pubSubPublishHandler2;
    private org.apache.vysper.xmpp.server.XMPPServer server;
    XMPPProtocolServer ps;


    public XMPPServer(XMPPProtocolServer ps, String host, int port){
        String domain = "okse.ntnu.no";
        this.ps = ps;
        StorageProviderRegistry providerRegistry = new OpenStorageProviderRegistry();
        providerRegistry.add(new Authorization());

        providerRegistry.add(new MemoryRosterManager());
        final String pathToTLSCertificate = "/keystore.jks";
        server = new org.apache.vysper.xmpp.server.XMPPServer(domain);

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
        System.out.println("dpodpkopopopopopo");
        server.addModule(new VcardTempModule());
        System.out.println("dpodpkopopopopopo");
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        PubSub publishSubscribeModule = new PubSub();
        server.addModule(publishSubscribeModule);
        pubSubPublishHandler2 = publishSubscribeModule.getPubSubPublishHandler2();
        pubSubPublishHandler2.setXMPPServer(this);
        serverRuntimeContext = publishSubscribeModule.getServerRuntimeContext();

    }
    public void sendMessage(Message message) {
        pubSubPublishHandler2.publishXMPPmessage(message, serverRuntimeContext);
        ps.incrementTotalMessagesSent();
    }
    public void stopServer() {
        server.stop();
    }
    public void sendMessageToOKSE(Message msg){
        log.info("XMPP message received on topic: " + msg.getTopic() + " from ID: " + msg.getMessageID());
        MessageService.getInstance().distributeMessage(msg);
        ps.incrementTotalMessagesReceived();
    }
}
