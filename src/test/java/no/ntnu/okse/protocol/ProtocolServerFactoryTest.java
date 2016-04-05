package no.ntnu.okse.protocol;

import no.ntnu.okse.protocol.amqp.AMQProtocolServer;
import no.ntnu.okse.protocol.wsn.WSNotificationServer;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.HashMap;

@Test(singleThreaded = true)
public class ProtocolServerFactoryTest {

    @BeforeMethod
    public void setup() {
        PropertyConfigurator.configure("config/log4j.properties");
    }

    final String AMQP_CLASS_NAME = AMQProtocolServer.class.toString();
    final String WSN_CLASS_NAME = WSNotificationServer.class.toString();

    private static Document parse(InputStream is) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Document parse(String str) {
        return parse(new BufferedInputStream(new ReaderInputStream(new StringReader(str))));
    }

    @Test
    public void testCreateMultiple() {
        final String xml_cfg =
                "<servers>" +
                    "<server type=\"amqp\" />" +
                    "<server type=\"wsn\" />" +
                "</servers>";

        final HashMap<String, String> protos = new HashMap<>();
        protos.put("amqp", AMQP_CLASS_NAME);
        protos.put("wsn", WSN_CLASS_NAME);

        Document cfg = parse(xml_cfg);

        NodeList servers = cfg.getElementsByTagName("server");
        for(int i = 0; i < servers.getLength(); i++) {
            ProtocolServer ps = ProtocolServerFactory.create(servers.item(i));
            String proto = servers.item(i).getAttributes().getNamedItem("type").getNodeValue();
            Assert.assertTrue(ps.getClass().toString().equals(protos.get(proto)));
        }
    }

    @Test
    public void testCreateAMQP() {
        final String xml_cfg =
                "<servers>" +
                        "<server type=\"amqp\" />" +
                        "<server type=\"amqp\" host=\"localhost\" port=\"5673\" />" +
                        "<server type=\"amqp\" sasl=\"true\" queue=\"false\" />" +
                        "<server type=\"amqp\" sasl=\"false\" queue=\"true\" />" +
                        "<server type=\"amqp\" sasl=\"invalid\" queue=\"invalid\" />" +
                "</servers>";

        Document cfg = parse(xml_cfg);

        NodeList servers = cfg.getElementsByTagName("server");
        for(int i = 0; i < servers.getLength(); i++) {
            ProtocolServer ps = ProtocolServerFactory.create(servers.item(i));
            Assert.assertTrue(ps.getClass().toString().equals(AMQP_CLASS_NAME));
        }
    }

    @Test
    public void testCreateWSN() {
        final String xml_cfg =
                "<servers>" +
                        "<server type=\"wsn\" />" +
                        "<server type=\"wsn\" host=\"localhost\" port=\"61000\" />" +
                        "<server type=\"wsn\" wan_host=\"localhost\" wan_port=\"61000\" />" +
                        "<server type=\"wsn\" timeout=\"500\" pool_size=\"400\" />" +
                        "<server type=\"wsn\" wrapper_name=\"test\" nat=\"true\" />" +
                "</servers>";

        Document cfg = parse(xml_cfg);

        NodeList servers = cfg.getElementsByTagName("server");
        ProtocolServer ps = ProtocolServerFactory.create(servers.item(0));
        Assert.assertTrue(ps.getClass().toString().equals(WSN_CLASS_NAME));
    }

    @Test
    public void testCreateInvalidType() {
        final String xml_cfg =
                "<servers><server type=\"invalid\" /></servers>";

        Document cfg = parse(xml_cfg);

        NodeList servers = cfg.getElementsByTagName("server");
        ProtocolServer ps = ProtocolServerFactory.create(servers.item(0));
        Assert.assertNull(ps);
    }

    @Test
    public void testCreateNoType() {
        final String xml_cfg =
                "<servers><server /></servers>";

        Document cfg = parse(xml_cfg);

        NodeList servers = cfg.getElementsByTagName("server");
        ProtocolServer ps = ProtocolServerFactory.create(servers.item(0));
        Assert.assertNull(ps);
    }
}
