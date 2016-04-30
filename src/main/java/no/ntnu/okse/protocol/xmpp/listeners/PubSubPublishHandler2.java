/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package no.ntnu.okse.protocol.xmpp.listeners;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.protocol.xmpp.XMPPServer;
import org.apache.qpid.proton.codec.messaging.SourceType;
import org.apache.vysper.compliance.SpecCompliance;
import org.apache.vysper.compliance.SpecCompliant;
import org.apache.vysper.xml.fragment.*;
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

import static no.ntnu.okse.core.Utilities.log;

/**
 * This class handles the "publish" use cases for the "pubsub" namespace.
 *
 * @author The Apache MINA Project (http://mina.apache.org)
 */
@SpecCompliant(spec = "xep-0060", section = "7.1", status = SpecCompliant.ComplianceStatus.IN_PROGRESS, coverage = SpecCompliant.ComplianceCoverage.PARTIAL)
public class PubSubPublishHandler2 extends AbstractPubSubGeneralHandler {

    XMPPServer server;
    /**
     * Creates a new handler for publish requests.
     *
     * @param serviceConfiguration
     */
    public PubSubPublishHandler2(PubSubServiceConfiguration serviceConfiguration) {
        super(serviceConfiguration);
    }
    public void setXMPPServer(XMPPServer server){
        this.server = server;
    }

    /**
     * @return "publish" as worker element.
     */
    @Override
    protected String getWorkerElement() {
        return "publish";
    }

    /**
     * This method takes care of handling the "publish" use-case including all (relevant) error conditions.
     *
     * @return the appropriate response stanza (either success or some error condition).
     */
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
        System.out.println("Message received");


        Entity serverJID = serviceConfiguration.getDomainJID();
        CollectionNode root = serviceConfiguration.getRootNode();

        Entity sender = extractSenderJID(stanza, sessionContext);

        StanzaBuilder sb = StanzaBuilder.createDirectReply(stanza, false, IQStanzaType.RESULT);
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

        //Andreas ... can edit this one if we want all users to be able to publish on all topics
        if (!node.isAuthorized(sender, PubSubPrivilege.PUBLISH)) {
            // not enough privileges to publish - error condition 1 (7.1.3)
            log.info("THE XMPP publisher for current msg has no publisher rigths, the message should be refused if the standard was followed. error condition 1 (7.1.3). (overwritten in OKSE)");
            //return errorStanzaGenerator.generateInsufficientPrivilegesErrorStanza(sender, serverJID, stanza);
        }
        //Following printlines should be deleted, for testing only
        System.out.println("NODENAME " + nodeName);
        System.out.println("payload " + item.getInnerText());
        System.out.println("ID " + strID);
        System.out.println("STANZA GET TO " + stanza.getTo());

        handlePublishInOkse(nodeName, item.getInnerText(), strID);

        StanzaRelay relay = serverRuntimeContext.getStanzaRelay();

        XMLElementBuilder eventItemBuilder = new XMLElementBuilder("item", NamespaceURIs.XEP0060_PUBSUB_EVENT);
        if (strID == null) {
            strID = idGenerator.create();
        }
        eventItemBuilder.addAttribute("id", strID);

        for (XMLFragment fragment : item.getInnerFragments()) {
            System.out.println("frament :" + fragment.toString());
            if (fragment instanceof XMLElement) {
                eventItemBuilder.addPreparedElement((XMLElement) fragment);
            } else {
                // XMLText
                eventItemBuilder.addText(((XMLText) fragment).getText());
            }
        }

        //The 2 lines below should be deleted eventualy, and are just used for imidiate testing. They originaly were here in the default handler class
         //node.publish(sender, relay, strID, eventItemBuilder.build());
        //node.publish(null, relay, "demoID1461861614060", eventItemBuilder.build());


        buildSuccessStanza(sb, nodeName, strID);

        sb.endInnerElement(); // pubsub
        return new IQStanza(sb.build());
    }

    /**
     * This method adds the default "success" elements to the given StanzaBuilder.
     *
     * @param sb the StanzaBuilder to add the success elements.
     * @param node the node to which the message was published.
     * @param id the id of the published message.
     */
    private void buildSuccessStanza(StanzaBuilder sb, String node, String id) {
        sb.startInnerElement("publish", NamespaceURIs.XEP0060_PUBSUB);
        sb.addAttribute("node", node);

        sb.startInnerElement("item", NamespaceURIs.XEP0060_PUBSUB);
        sb.addAttribute("id", id);
        sb.endInnerElement();

        sb.endInnerElement();
    }



    void handlePublishInOkse(String topic, XMLText payload, String id){
        server.sendMessageToOKSE(new Message( payload.toString(), topic, null, "XMPP"));
    }

    public void publishXMPPmessage(Message message, ServerRuntimeContext serverRuntimeContext){
        CollectionNode root = serviceConfiguration.getRootNode();
        LeafNode node = root.find(message.getTopic());
        if(node == null){
            //no xmpp clients are subscribed on the topic.
            return;
        }
        XMLElementBuilder eventItemBuilder = new XMLElementBuilder("item", NamespaceURIs.XEP0060_PUBSUB_EVENT);
        String strID = idGenerator.create();
        eventItemBuilder.addAttribute("id", strID);
        eventItemBuilder.addText(message.getMessage());
        StanzaRelay relay = serverRuntimeContext.getStanzaRelay();
        System.out.println("IN publishXMPPmessage function");
        System.out.println("topic " + message.getTopic());
        System.out.println("payload " + message.getMessage());
        node.publish(null, relay, strID, eventItemBuilder.build());
    }
}
