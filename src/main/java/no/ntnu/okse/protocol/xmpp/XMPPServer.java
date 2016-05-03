package no.ntnu.okse.protocol.xmpp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.protocol.xmpp.modules.PubSub;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubPublishHandlerOKSE;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.vysper.mina.S2SEndpoint;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import java.nio.charset.Charset;
import java.util.logging.Logger;


/**
 *This class starts and stops the XMPP server.
 * It adds modules like the pubsub module
 * and adds listeners. It also handles sending
 * messages to and from OKSE
 */
public class XMPPServer {

    private static Logger log = Logger.getLogger(XMPPProtocolServer.class.getName());
    private TCPEndpoint tcpEndpoint;
    private ServerRuntimeContext serverRuntimeContext;
    private PubSubPublishHandlerOKSE pubSubPublishHandlerOKSE;
    private org.apache.vysper.xmpp.server.XMPPServer server;
    XMPPProtocolServer ps;

    /**
     *This class Starts the server and adds extra modules.
     * @param ps This is the XMPPProtocolServer in OKSE
     * @param host Curently not in use, it should be the host that the server listens on
     * @param port The port that the server listens on
     */
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DefaultIoFilterChainBuilder filterChainBuilder;
        ProtocolCodecFilter codecFilter = new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" )));
        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        PubSub publishSubscribeModule = new PubSub();
        server.addModule(publishSubscribeModule);
        pubSubPublishHandlerOKSE = publishSubscribeModule.getPubSubPublishHandlerOKSE();
        pubSubPublishHandlerOKSE.setXMPPServer(this);
        serverRuntimeContext = publishSubscribeModule.getServerRuntimeContext();

    }

    /**
     * Sends message to xmpp subscribers based on the topic of the message
     * Minimum one xmpp client must have subscribed on the topic
     * @param message OKSE message
     */
    public void sendMessage(Message message) {
        pubSubPublishHandlerOKSE.publishXMPPmessage(message, serverRuntimeContext);
        ps.incrementTotalMessagesSent();
    }
    public void stopServer() {
        server.stop();
    }

    /**
     * Takes in an OKSE message and sends it into OKSE
     * @param msg OKSE message
     */
    public void sendMessageToOKSE(Message msg){
        log.info("XMPP message received on topic: " + msg.getTopic() + " from ID: " + msg.getMessageID());
        MessageService.getInstance().distributeMessage(msg);
        ps.incrementTotalMessagesReceived();
    }
}
