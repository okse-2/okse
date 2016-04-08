package no.ntnu.okse.protocol.amqp091;

import fr.dyade.aaa.agent.AgentServer;
import no.ntnu.okse.core.messaging.Message;
import org.apache.log4j.Logger;
import org.ow2.joram.mom.amqp.AMQPService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AMQP091Service {

    private final String host;
    private final int port;
    private static Logger log = Logger.getLogger(AMQP091Service.class.getName());
    private AMQP091MessageListener messageListener;

    public AMQP091Service(AMQP091ProtocolServer amqp091ProtocolServer) {
        this.host = amqp091ProtocolServer.getHost();
        this.port = amqp091ProtocolServer.getPort();
        messageListener = new AMQP091MessageListener(amqp091ProtocolServer);
    }

    public void start() {
        log.debug("AMQP 0.9.1 service is starting");
        try {
            AgentServer.init((short) 0, createAgentFolder(), null);
        } catch (Exception e) {
            log.error("An exception was thrown when starting AMQP 0.9.1 Agent Server", e);
        }
        try {
            AMQPService.init(getHostPort(), true);
        } catch (Exception e) {
            log.error("An exception was thrown when starting AMQP 0.9.1 service", e);
        }
        AMQPService.addMessageListener(messageListener);
        AMQPService.setPublishing(false);
        log.debug("AMQP 0.9.1 service started successfully");
    }

    public void stop() {
        log.debug("AMQP 0.9.1 service is stopping");
        AMQPService.stopService();
        AgentServer.stop();
        log.debug("AMQP 0.9.1 service stopped");
    }

    private String getHostPort() {
        return "" + port + " " + host;
    }

    private String createAgentFolder() {
        String directoryName = "agent-server";
        try {
            Path agentFolder = Files.createTempDirectory(directoryName);
            return agentFolder.toString();
        } catch (IOException e) {
            log.warn("Failed to create temporary folder, creating folder in local directory");
            File localDirectory = new File(directoryName);
            if(!localDirectory.exists()) {
                localDirectory.mkdir();
            }
            return directoryName;
        }
    }

    public void sendMessage(Message message) {
        AMQPService.internalPublish(message.getTopic(), "", message.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}
