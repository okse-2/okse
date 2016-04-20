package no.ntnu.okse.web;

import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.web.controller.ConfigController;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Test(singleThreaded = true)
public class ConfigControllerTest {

    private ConfigController configController;
    private final int PORT1 = 40000;
    private final int PORT2 = 50000;

    @BeforeTest
    public void setup() {
        PropertyConfigurator.configure("config/log4j.properties");
        final String xml_cfg =
                "<servers>" +
                    "<server type=\"wsn\" host=\"127.0.0.1\" port=\""+PORT1+"\" />" +
                    "<server type=\"wsn\" host=\"127.0.0.1\" port=\""+PORT2+"\" />" +
                "</servers>";
        CoreService.getInstance().bootProtocolServers(new BufferedInputStream(new ReaderInputStream(new StringReader(xml_cfg))));
        configController = new ConfigController();
    }

    @Test
    public void testGetWsnServers() {
        List<Map<String, Object>> servers = configController.getWsnServers();
        Assert.assertEquals(servers.size(), 2);
        servers.forEach((val) -> {
            Assert.assertEquals(val.get("host"), "127.0.0.1");
            Assert.assertTrue(val.get("port").equals(PORT1) || val.get("port").equals(PORT2));
        });
    }

    @Test
    public void testWsnRelay() {
        String str;
        Set<String> relays = configController.getWsnRelays(0);

        str = configController.addRelay(0, "http://127.0.0.1:"+PORT2+"/", "testRelay").getBody();
        Assert.assertTrue(str.contains("Successfully"));
        Assert.assertEquals(relays.size(), 1);
        final String first = relays.stream().findFirst().get();
        Assert.assertTrue(first.matches(".*subscriptionManager.*"));

        str = configController.addRelay(0, "http://127.0.0.1:"+PORT2+"/", "testRelay").getBody();
        Assert.assertTrue(str.contains("Successfully"));
        Assert.assertEquals(relays.size(), 2);

        str = configController.deleteRelay(0, first).getBody();
        Assert.assertTrue(str.contains("Successfully"));
        Assert.assertEquals(relays.size(), 1);

        configController.deleteAllRelays().getBody();
        Assert.assertEquals(relays.size(), 0);
    }

    @Test
    public void testBadWsnRelay() {
        String str;
        final Set<String> relays = configController.getWsnRelays(0);

        // Attempt to get list of relays for an invalid WSN server
        Assert.assertNull(configController.getWsnRelays(42));

        configController.addRelay(0, "http://127.0.0.1:"+PORT2+"/", "testRelay");

        // Attempt to delete an invalid relay from a valid WSN server
        str = configController.deleteRelay(0, "testRelay").getBody();
        Assert.assertTrue(str.contains("Unable"));
        Assert.assertEquals(relays.size(), 1);

        // Attempt to create a relay from a valid WSN server to itself
        str = configController.addRelay(0, "http://127.0.0.1:"+PORT1+"/", "testRelay").getBody();
        Assert.assertTrue(str.contains("Unable"));

        configController.deleteAllRelays();
    }
}
