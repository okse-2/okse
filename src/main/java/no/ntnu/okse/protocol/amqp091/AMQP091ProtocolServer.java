package no.ntnu.okse.protocol.amqp091;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
            //TODO change getLocalHost to host variable
        ServerSocket serverSocket = new ServerSocket(port, 1000, InetAddress.getByName(host));
        while(_running){
            Socket sock = serverSocket.accept();
            System.out.println("Inncomming AMQP-0091 connection: " + sock.getInetAddress().getHostAddress());
            log.info("Inncomming AMQP-0091 connection: ");
            sock.getInputStream();
            }
        }
        catch (IOException e) {
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
        System.out.println("Send message in AMQP091 is not implemented");
    }
}
