package no.ntnu.okse.protocol.xmpp.commons;

//import org.apache.vysper.compliance.SpecCompliant;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubPublishHandler2;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubSubscribeHandler2;
import no.ntnu.okse.protocol.xmpp.listeners.PubSubTestHandler;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityUtils;
import org.apache.vysper.xmpp.modules.core.base.handler.MessageHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.NodeDiscoItemsVisitor;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PubSubServiceConfiguration;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PublishSubscribeModule;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.ServiceDiscoItemsVisitor;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubCreateNodeHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubPublishHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubRetrieveAffiliationsHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubRetrieveSubscriptionsHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubSubscribeHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.PubSubUnsubscribeHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.owner.PubSubOwnerConfigureNodeHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.owner.PubSubOwnerDeleteNodeHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.model.CollectionNode;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.model.LeafNode;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.storageprovider.CollectionNodeStorageProvider;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.storageprovider.LeafNodeStorageProvider;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ComponentInfoRequestListener;
import org.apache.vysper.xmpp.modules.servicediscovery.management.Feature;
import org.apache.vysper.xmpp.modules.servicediscovery.management.Identity;
import org.apache.vysper.xmpp.modules.servicediscovery.management.InfoElement;
import org.apache.vysper.xmpp.modules.servicediscovery.management.InfoRequest;
import org.apache.vysper.xmpp.modules.servicediscovery.management.Item;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ItemRequestListener;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ServerInfoRequestListener;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ServiceDiscoveryRequestException;
import org.apache.vysper.xmpp.protocol.NamespaceHandlerDictionary;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.protocol.StanzaHandler;
import org.apache.vysper.xmpp.protocol.StanzaProcessor;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.server.components.ComponentStanzaProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the XEP0060 module. This class is also responsible for disco requests at the service level.
 *
 * @author The Apache MINA Project (http://mina.apache.org)
 */
//@SpecCompliant(spec = "xep-0060", comment = "spec. version: 1.13rc", status = SpecCompliant.ComplianceStatus.IN_PROGRESS, coverage = SpecCompliant.ComplianceCoverage.PARTIAL)
public class PubSub extends PublishSubscribeModule {

    // The configuration of the service
    private PubSubServiceConfiguration serviceConfiguration = null;

    // for debugging
    private final Logger logger = LoggerFactory.getLogger(PublishSubscribeModule.class);

    private ComponentStanzaProcessor stanzaProcessor;

    private ServerRuntimeContext serverRuntimeContext;

    /**
     * the subdomain this module becomes know under.
     */
    protected String subdomain = "pubsub";

    /**
     * the domain derived from the subdomain and the server domain
     */
    protected Entity fullDomain;

    /**
     * Create a new PublishSubscribeModule together with a new root-collection node.
     */
    public PubSub(String subdomain) {
        this();
        this.subdomain = subdomain;
    }

    /**
     * Create a new PublishSubscribeModule together with a new root-collection node.
     */
    public PubSub() {
        this(new PubSubServiceConfiguration(new CollectionNode()));
    }

    /**
     * Create a new PublishSubscribeModule together with a supplied root-collection node.
     */
    public PubSub(PubSubServiceConfiguration serviceConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
    }

    /**
     * Initializes the pubsub module, configuring the storage providers.
     */
    @Override
    public void initialize(ServerRuntimeContext serverRuntimeContext) {
        super.initialize(serverRuntimeContext);

        this.serverRuntimeContext = serverRuntimeContext;

        fullDomain = EntityUtils.createComponentDomain(subdomain, serverRuntimeContext);

        CollectionNodeStorageProvider collectionNodeStorageProvider = (CollectionNodeStorageProvider) serverRuntimeContext
                .getStorageProvider(CollectionNodeStorageProvider.class);
        LeafNodeStorageProvider leafNodeStorageProvider = (LeafNodeStorageProvider) serverRuntimeContext
                .getStorageProvider(LeafNodeStorageProvider.class);

        if (collectionNodeStorageProvider == null) {
            logger.warn("No collection node storage provider found, using the default (in memory)");
        } else {
            serviceConfiguration.setCollectionNodeStorageProvider(collectionNodeStorageProvider);
        }

        if (leafNodeStorageProvider == null) {
            logger.warn("No leaf node storage provider found, using the default (in memory)");
        } else {
            serviceConfiguration.setLeafNodeStorageProvider(leafNodeStorageProvider);
        }

        ComponentStanzaProcessor processor = new ComponentStanzaProcessor(serverRuntimeContext);
        addPubsubHandlers(processor);
        addPubsubOwnerHandlers(processor);
        processor
                .addDictionary(new NamespaceHandlerDictionary(NamespaceURIs.XEP0060_PUBSUB_EVENT, new MessageHandler()));
        stanzaProcessor = processor;

        this.serviceConfiguration.setDomainJID(fullDomain);
        this.serviceConfiguration.initialize();
    }

    /**
     * Returns the service name
     */
    @Override
    public String getName() {
        return "XEP-0060 Publish-Subscribe";
    }

    /**
     * Returns the implemented spec. version.
     */
    @Override
    public String getVersion() {
        return "1.13rc3";
    }

    /**
     * Implements the getServerInfosFor method from the {@link ServerInfoRequestListener} interface.
     * Makes this modules available via disco#info as "pubsub service" in the pubsub namespace.
     *
     * @see ComponentInfoRequestListener#getComponentInfosFor(org.apache.vysper.xmpp.modules.servicediscovery.management.InfoRequest)
     */
    public List<InfoElement> getComponentInfosFor(InfoRequest request) throws ServiceDiscoveryRequestException {
        if (!fullDomain.getDomain().equals(request.getTo().getDomain()))
            return null;

        CollectionNode root = serviceConfiguration.getRootNode();
        List<InfoElement> infoElements = new ArrayList<InfoElement>();
        if (request.getNode() == null || request.getNode().length() == 0) {
            infoElements.add(new Identity("pubsub", "service", "Publish-Subscribe"));
            infoElements.add(new Feature(NamespaceURIs.XEP0060_PUBSUB));
        } else {
            LeafNode node = root.find(request.getNode());
            infoElements.addAll(node.getNodeInfosFor(request));
        }
        return infoElements;
    }

    @Override
    protected void addComponentInfoRequestListeners(List<ComponentInfoRequestListener> componentInfoRequestListeners) {
        componentInfoRequestListeners.add(this);
    }

    /**
     * Make this object available for disco#items requests.
     */
    @Override
    protected void addItemRequestListeners(List<ItemRequestListener> itemRequestListeners) {
        itemRequestListeners.add(this);
    }

    /**
     * Implements the getItemsFor method from the {@link ItemRequestListener} interface.
     * Makes this modules available via disco#items and returns the associated nodes.
     *
     * @see ItemRequestListener#getItemsFor(InfoRequest)
     */
    public List<Item> getItemsFor(InfoRequest request) throws ServiceDiscoveryRequestException {
        CollectionNode root = serviceConfiguration.getRootNode();
        List<Item> items = null;

        if (request.getNode() == null || request.getNode().length() == 0) {
            if (serverRuntimeContext.getServerEnitity().equals(request.getTo())) {
                // top level item request. for example return entry for "pubsub.vysper.org" on request for "vysper.org"
                List<Item> componentItem = new ArrayList<Item>();
                componentItem.add(new Item(fullDomain));
                return componentItem;
            } else if (!fullDomain.equals(request.getTo())) {
                return null; // not in component's domain
            }
            ServiceDiscoItemsVisitor nv = new ServiceDiscoItemsVisitor(serviceConfiguration);
            root.acceptNodes(nv);
            items = nv.getNodeItemList();
        } else {
            LeafNode node = root.find(request.getNode());
            NodeDiscoItemsVisitor iv = new NodeDiscoItemsVisitor(request.getTo());
            node.acceptItems(iv);
            items = iv.getItemList();
        }

        return items;
    }

    /**
     * Inserts the handlers for the pubsub#owner namespace into the HandlerDictionary.
     * @param dictionary the list to which the handlers should be appended.
     */
    private void addPubsubOwnerHandlers(ComponentStanzaProcessor dictionary) {
        ArrayList<StanzaHandler> pubsubOwnerHandlers = new ArrayList<StanzaHandler>();
        pubsubOwnerHandlers.add(new PubSubOwnerConfigureNodeHandler(serviceConfiguration));
        pubsubOwnerHandlers.add(new PubSubOwnerDeleteNodeHandler(serviceConfiguration));
        dictionary
                .addDictionary(new NamespaceHandlerDictionary(NamespaceURIs.XEP0060_PUBSUB_OWNER, pubsubOwnerHandlers));
    }

    /**
     * Inserts the handlers for the pubsub namespace into the HandlerDictionary.
     * @param dictionary the list to which the handlers should be appended.
     */
    private void addPubsubHandlers(ComponentStanzaProcessor dictionary) {
        ArrayList<StanzaHandler> pubsubHandlers = new ArrayList<StanzaHandler>();
        //pubsubHandlers.add(new PubSubTestHandler(serviceConfiguration));
        pubsubHandlers.add(new PubSubSubscribeHandler2(serviceConfiguration));
        pubsubHandlers.add(new PubSubUnsubscribeHandler(serviceConfiguration));
        pubsubHandlers.add(new PubSubPublishHandler2(serviceConfiguration));
        pubsubHandlers.add(new PubSubCreateNodeHandler(serviceConfiguration));
        pubsubHandlers.add(new PubSubRetrieveSubscriptionsHandler(serviceConfiguration));
        pubsubHandlers.add(new PubSubRetrieveAffiliationsHandler(serviceConfiguration));
        dictionary.addDictionary(new NamespaceHandlerDictionary(NamespaceURIs.XEP0060_PUBSUB, pubsubHandlers));
    }

    public PubSubServiceConfiguration getServiceConfiguration(){
        return serviceConfiguration;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public StanzaProcessor getStanzaProcessor() {
        return stanzaProcessor;
    }
}
