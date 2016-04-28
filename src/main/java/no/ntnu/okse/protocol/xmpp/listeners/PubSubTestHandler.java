package no.ntnu.okse.protocol.xmpp.listeners;

import org.apache.vysper.compliance.SpecCompliance;
import org.apache.vysper.compliance.SpecCompliant;
import org.apache.vysper.xml.fragment.XMLElement;
import org.apache.vysper.xml.fragment.XMLElementBuilder;
import org.apache.vysper.xml.fragment.XMLFragment;
import org.apache.vysper.xml.fragment.XMLText;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.delivery.StanzaRelay;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PubSubPrivilege;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.PubSubServiceConfiguration;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.handler.AbstractPubSubGeneralHandler;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.model.CollectionNode;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.model.LeafNode;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.stanza.IQStanza;
import org.apache.vysper.xmpp.stanza.IQStanzaType;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;

/**
 * Created by Ogdans3 on 28.04.2016.
 */
@SpecCompliant(spec = "xep-0060", section = "7.1", status = SpecCompliant.ComplianceStatus.IN_PROGRESS, coverage = SpecCompliant.ComplianceCoverage.PARTIAL)
public class PubSubTestHandler extends AbstractPubSubGeneralHandler {
    public PubSubTestHandler(PubSubServiceConfiguration serviceConfiguration) {
        super(serviceConfiguration);
    }

    @Override
    protected String getWorkerElement() {
        return "test";
    }

    @Override
    @SpecCompliance(compliant = {
            @SpecCompliant(spec = "xep-0060", section = "7.1.2", status = SpecCompliant.ComplianceStatus.FINISHED, coverage = SpecCompliant.ComplianceCoverage.COMPLETE),
            @SpecCompliant(spec = "xep-0060", section = "7.1.2.1", status = SpecCompliant.ComplianceStatus.FINISHED, coverage = SpecCompliant.ComplianceCoverage.COMPLETE),
            @SpecCompliant(spec = "xep-0060", section = "7.1.2.2", status = SpecCompliant.ComplianceStatus.NOT_STARTED, coverage = SpecCompliant.ComplianceCoverage.UNSUPPORTED),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.1", status = SpecCompliant.ComplianceStatus.FINISHED, coverage = SpecCompliant.ComplianceCoverage.COMPLETE),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.2", status = SpecCompliant.ComplianceStatus.NOT_STARTED, coverage = SpecCompliant.ComplianceCoverage.UNSUPPORTED),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.3", status = SpecCompliant.ComplianceStatus.FINISHED, coverage = SpecCompliant.ComplianceCoverage.COMPLETE),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.4", status = SpecCompliant.ComplianceStatus.NOT_STARTED, coverage = SpecCompliant.ComplianceCoverage.UNSUPPORTED),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.5", status = SpecCompliant.ComplianceStatus.NOT_STARTED, coverage = SpecCompliant.ComplianceCoverage.UNSUPPORTED),
            @SpecCompliant(spec = "xep-0060", section = "7.1.3.6", status = SpecCompliant.ComplianceStatus.NOT_STARTED, coverage = SpecCompliant.ComplianceCoverage.UNSUPPORTED) })
    protected Stanza handleSet(IQStanza stanza, ServerRuntimeContext serverRuntimeContext, SessionContext sessionContext) {
        Entity serverJID = serviceConfiguration.getDomainJID();
        CollectionNode root = serviceConfiguration.getRootNode();

        System.out.println("Something happened");
        System.out.println("Something happened");
        System.out.println("Something happened");
        System.out.println("Something happened");
        System.out.println("Something happened");

        StanzaBuilder sb = StanzaBuilder.createDirectReply(stanza, false, IQStanzaType.RESULT);
        return new IQStanza(sb.build());

/*        Entity sender = extractSenderJID(stanza, sessionContext);

//        StanzaBuilder sb = StanzaBuilder.createDirectReply(stanza, false, IQStanzaType.RESULT);
        sb.startInnerElement("pubsub", NamespaceURIs.XEP0060_PUBSUB);

        XMLElement publish = stanza.getFirstInnerElement().getFirstInnerElement(); // pubsub/publish
        String nodeName = publish.getAttributeValue("node"); // MUST

        XMLElement item = publish.getFirstInnerElement();
        String strID = item.getAttributeValue("id"); // MAY

        LeafNode node = root.find(nodeName);

        if (node == null) {
            // node does not exist - error condition 3 (7.1.3)
            return errorStanzaGenerator.generateNoNodeErrorStanza(sender, serverJID, stanza);
        }

        if (!node.isAuthorized(sender, PubSubPrivilege.PUBLISH)) {
            // not enough privileges to publish - error condition 1 (7.1.3)
            return errorStanzaGenerator.generateInsufficientPrivilegesErrorStanza(sender, serverJID, stanza);
        }

        StanzaRelay relay = serverRuntimeContext.getStanzaRelay();

        XMLElementBuilder eventItemBuilder = new XMLElementBuilder("item", NamespaceURIs.XEP0060_PUBSUB_EVENT);
        if (strID == null) {
            strID = idGenerator.create();
        }
        eventItemBuilder.addAttribute("id", strID);

        for (XMLFragment fragment : item.getInnerFragments()) {
            if (fragment instanceof XMLElement) {
                eventItemBuilder.addPreparedElement((XMLElement) fragment);
            } else {
                // XMLText
                eventItemBuilder.addText(((XMLText) fragment).getText());
            }
        }

        node.publish(sender, relay, strID, eventItemBuilder.build());

//        buildSuccessStanza(sb, nodeName, strID);

        sb.endInnerElement(); // pubsub
        return new IQStanza(sb.build());*/
    }
}
