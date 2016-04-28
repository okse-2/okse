package no.ntnu.okse.protocol.xmpp;


import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.*;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.core.session.IoSessionDataStructureFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.util.CommonEventFilter;
import org.apache.vysper.mina.S2SEndpoint;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.mina.codec.StanzaWriteInfo;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PublishSubscribeModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ItemRequestListener;
import org.apache.vysper.xmpp.server.ServerFeatures;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;

public class XMPPProtocolServer extends AbstractProtocolServer {
    private TCPEndpoint tcpEndpoint;
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
        tcpEndpoint
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
        //tcpEndpoint.getFilterChainBuilder().addLast( "codec", codecFilter);

        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new VcardTempModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        AdhocCommandsModule adhocCommandsModule = new AdhocCommandsModule();
        server.addModule(adhocCommandsModule);


        PublishSubscribeModule  publishSubscribeModule = new PublishSubscribeModule();
        server.addModule(publishSubscribeModule);
        publishSubscribeModule
        //the line under sets relaying messages on/off
        //server.getServerRuntimeContext().getServerFeatures().setRelayingMessages();
        //IoEvent event = new IoEvent()

        DefaultIoFilterChainBuilder filterChainBuilder = tcpEndpoint.getFilterChainBuilder();

        StanzaBuilder stanzaBuilder = new StanzaBuilder("fucku");
        stanzaBuilder.addAttribute("aa", "ad");

        System.out.println("aaaaa" + server.getServerRuntimeContext().getResourceRegistry().getSessionCount());
        //filterChainBuilder.addAfter("sss", );
//        List<IoFilterChain.Entry> entries = filterChainBuilder.getAll();
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        System.out.println("printing out size " + entries.size());
//        for(int i = 0; i < entries.size(); i++){
//            System.out.println(entries.toString());
//        }




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