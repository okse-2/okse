package no.ntnu.okse.protocol.amqp091;
import fr.dyade.aaa.agent.AgentServer;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;
import org.ow2.joram.mom.amqp.AMQPService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Andreas the time lord on 30/03/2516
 */
public class AMQP091ProtocolServer extends AbstractProtocolServer {


    protected static final String SERVERTYPE = "amqp091";


    public AMQP091ProtocolServer(String host, int port) throws IOException {
        this.port = port;
        this.host = host;
        log = Logger.getLogger(AMQP091ProtocolServer.class.getName());
    }

    @Override
    public void boot() {

        //_serverThread

        if (!_running) {
            _running = true;
            _serverThread = new Thread(() -> this.run());
            _serverThread.setName("AMQ091ProtocolServer");
            _serverThread.start();
            log.info("AMQ091ProtocolServer successfully");
        }
    }

    @Override
    public void run() {
        try {
            AgentServer.init((short) 0, "./s0", null);
            AMQPService.init("" + port, true);
            AMQPService.addMessageListener(new AMQP091MessageListener(this));
            AMQPService.setPublishing(false);
        } catch (Exception e) {
            // TODO: Properly handle exception
            e.printStackTrace();
        }
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
        AMQPService.internalPublish(message.getTopic(), "", message.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}
