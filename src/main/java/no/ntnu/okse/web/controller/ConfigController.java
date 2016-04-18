package no.ntnu.okse.web.controller;

import com.sun.org.apache.bcel.internal.generic.GETFIELD;
import no.ntnu.okse.core.CoreService;
import no.ntnu.okse.core.topic.TopicService;
import no.ntnu.okse.protocol.wsn.WSNotificationServer;
import org.apache.log4j.Logger;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    // URL routes
    private static final String GET_ALL_INFO = "/get/all";
    private static final String GET_ALL_MAPPINGS = "/mapping/get/all";
    private static final String ADD_MAPPING = "/mapping/add";
    private static final String DELETE_MAPPING = "/mapping/delete/single";
    private static final String DELETE_ALL_MAPPINGS = "/mapping/delete/all";
    private static final String CHANGE_AMQP = "/mapping/queue/change";
    private static final String ADD_RELAY = "/relay/add";
    private static final String DELETE_ALL_RELAYS = "/relay/delete/all";
    private static final String DELETE_RELAY = "/relay/delete/single";
    private static final String GET_WSN_SERVERS = "/get_wsn";
    private static final String GET_WSN_RELAYS = "/relay/get";

    // LOG4J logger
    private static Logger log = Logger.getLogger(ConfigController.class.getName());

    // Relay fields
    private List<WSNotificationServer> wsnServers;

    /**
     * Constructor for ConfigController. Initiates the localRelays
     */
    public ConfigController() {
    }

    /**
     * Returns all running WSN servers
     *
     * @return A List containing all running WSN servers
     */
    @RequestMapping(method = RequestMethod.GET, value = GET_WSN_SERVERS)
    public
    @ResponseBody
    List<Map<String,Object>> getWsnServers() {
        wsnServers = CoreService.getInstance().getAllProtocolServers().stream()
                .filter(server -> server.getProtocolServerType().equals("WSNotification"))
                .map(s -> (WSNotificationServer)s)
                .collect(Collectors.toList());

            return wsnServers.stream()
                .map(server -> new HashMap<String, Object>() {{
                    put("host", server.getHost());
                    put("port", server.getPort());
                }})
                .collect(Collectors.toList());
    }

    /**
     * Returns all configured relays for a given WSN server
     *
     * @param id Integer  ID of WSN server to fetch relays from
     * @return A list
     */
    @RequestMapping(method = RequestMethod.GET, value = GET_WSN_RELAYS)
    public
    @ResponseBody
    Set<String> getWsnRelays(@RequestParam(value = "serverid") Integer id) {
        return wsnServers.get(id).getRelays();
    }

    /**
     * This method returns all mappings and relays registered in OKSE
     *
     * @return A HashMap containing all mappings and relays
     */
    @RequestMapping(method = RequestMethod.GET, value = GET_ALL_INFO)
    public
    @ResponseBody
    HashMap<String, Object> getAllInfo() {
        TopicService ts = TopicService.getInstance();
        HashMap<String, HashSet<String>> allMappings = ts.getAllMappings();

        HashMap<String, Object> result = new HashMap<String, Object>() {{
            put("mappings", allMappings);
        }};

        return result;
    }

    /**
     * This method returns all mappings that exists in the TopicService
     *
     * @return A JSON serialized response body of the HashMap containing all mappings
     */
    @RequestMapping(method = RequestMethod.GET, value = GET_ALL_MAPPINGS)
    public
    @ResponseBody
    HashMap<String, HashSet<String>> getAllMappings() {
        TopicService ts = TopicService.getInstance();
        return ts.getAllMappings();
    }

    /**
     * This method adds a mapping between two topics in the TopicService
     *
     * @param topic    The topic to map from
     * @param newTopic The topic to map to
     * @return A JSON serialized response body
     */
    @RequestMapping(method = RequestMethod.POST, value = ADD_MAPPING)
    public
    @ResponseBody
    ResponseEntity<String> addMapping(@RequestParam(value = "fromTopic") String topic, @RequestParam(value = "toTopic") String newTopic) {
        log.debug("Adding a mapping between Topic{" + topic + "} and Topic{" + newTopic + "}");
        TopicService ts = TopicService.getInstance();
        ts.addMappingBetweenTopics(topic, newTopic);
        // TODO: We probably need to add some check somewhere, that checks if the input string is correct.

        return new ResponseEntity<String>("{ \"message\" :\"Added mapping from Topic{" + topic + "} to Topic{ " + newTopic + " }\" }", HttpStatus.OK);
    }

    /**
     * This method deletes all mappings for a given topic in the TopicService
     *
     * @param topicToRemove The topic to remove
     * @return A JSON serialized response body
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_MAPPING)
    public
    @ResponseBody
    ResponseEntity<String> deleteMapping(@RequestParam(value = "topic") String topicToRemove) {
        log.debug("Trying to remove the mapping for Topic{" + topicToRemove + "}");
        TopicService ts = TopicService.getInstance();
        ts.deleteMapping(topicToRemove);

        return new ResponseEntity<String>("{ \"message\" :\"Deleted mapping for Topic{" + topicToRemove + "}\" }", HttpStatus.OK);
    }

    /**
     * This method deletes all mappings in the TopicService
     *
     * @return A JSON serialized response body
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_ALL_MAPPINGS)
    public
    @ResponseBody
    ResponseEntity<String> deleteAllMapping() {
        log.debug("Trying to delete all mappings");
        TopicService ts = TopicService.getInstance();
        ts.getAllMappings().forEach((k, v) -> {
            ts.deleteMapping(k);
        });
        return new ResponseEntity<String>("{ \"message\" :\"Deleted all mappings\" }", HttpStatus.OK);
    }

    /**
     * This method changes the boolean value of the useQueue field in AMQPProtocolServer
     *
     * @return A message stating the new value of the useQueue variable
     */
    /*
    //TODO: Do this per instance
    @RequestMapping(method = RequestMethod.POST, value = CHANGE_AMQP)
    public
    @ResponseBody
    ResponseEntity<String> changeAMQPqueue() {
        AMQProtocolServer.getInstance().useQueue = (AMQProtocolServer.getInstance().useQueue ? false : true);
        log.debug("Value of AMQP queue is now " + AMQProtocolServer.getInstance().useQueue);
        return new ResponseEntity<String>("{ \"value\": " + AMQProtocolServer.getInstance().useQueue + ", " +
                "\"message\" :\"Successfully changed the useQueue variable to " + AMQProtocolServer.getInstance().useQueue + "\"}", HttpStatus.OK);
    }
    */

    /**
     * This method takes in a relay and a topic (not required) and sets up a relay
     *
     * @param relay String with host/port to relay from
     * @param topic String with topic to relay (not required)
     * @return A message telling the outcome of the subscription request.
     */
    //TODO: Do this per instance
    @RequestMapping(method = RequestMethod.POST, value = ADD_RELAY)
    public
    @ResponseBody
    ResponseEntity<String> addRelay(
            @RequestParam(value = "serverID") Integer id,
            @RequestParam(value = "from") String relay,
            @RequestParam(value = "topic", required = false) String topic) {
        log.debug("Trying to add relay from: " + relay + " with topic:" + topic);

        String regex = "(?:http.*://)?(?<host>[^:/ ]+).?(?<port>[0-9]*).*";
        Matcher m = Pattern.compile(regex).matcher(relay);
        String host = null;
        Integer port = null;

        if (m.matches()) {
            host = m.group("host");
            port = Integer.valueOf(m.group("port"));
        }

        if (host == null || port == null) {
            log.debug("Host or port not provided, not able to add relay");
            return new ResponseEntity<String>("{ \"message\" :\"Host or port not provided, not able to add relay\" }", HttpStatus.BAD_REQUEST);
        }

        if(wsnServers.get(id).addRelay(relay, host, port, topic)) {
            return new ResponseEntity<String>("{ \"message\" :\"Successfully added relay\" }", HttpStatus.OK);
        } else {

            return new ResponseEntity<String>("{ \"message\" :\"Unable to add relay\" }", HttpStatus.OK);
        }
    }

    /**
     * This method deletes a relay if it exists
     *
     * @param relay The relay to delete
     * @return A message telling if the removal were successful.
     */
    //TODO: Do this per instance
    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_RELAY)
    public
    @ResponseBody
    ResponseEntity<String> deleteRelay(@RequestParam(value = "serverID") Integer id, @RequestParam(value = "relayID") String relay) {
        log.debug("Trying to remove a relay: " + relay);
        final WSNotificationServer server = wsnServers.get(id);

        if(server.deleteRelay(relay))
            return new ResponseEntity<String>("{ \"message\" :\"Successfully removed the relay\" }", HttpStatus.OK);
        else
            return new ResponseEntity<String>("{ \"message\" :\"Unable to remove the relay, can't find it.\" }", HttpStatus.OK);
    }

    /**
     * This method deletes all relays registered.
     *
     * @return A response message
     */
    //TODO: Do this per instance
    @RequestMapping(method = RequestMethod.DELETE, value = DELETE_ALL_RELAYS)
    public
    @ResponseBody
    ResponseEntity<String> deleteAllRelays() {
        wsnServers.forEach(s -> s.deleteAllRelays());
        return new ResponseEntity<String>("{ \"message\" :\"Deleted all relays\" }", HttpStatus.OK);
    }
}
