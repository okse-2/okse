/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.ntnu.okse.clients.wsn;

import org.apache.cxf.wsn.client.NotificationBroker;
import org.apache.cxf.wsn.client.Referencable;
import org.apache.cxf.wsn.client.Subscription;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.xml.bind.JAXBElement;
import static org.apache.cxf.wsn.jms.JmsTopicExpressionConverter.SIMPLE_DIALECT;

public class TestNotificationBroker extends NotificationBroker {
    public TestNotificationBroker(String address, Class<?>... cls) {
        super(address, cls);
    }

    public Subscription subscribe(Referencable consumer, String topic,
                                  String xpath, boolean raw, String initialTerminationTime)
            throws TopicNotSupportedFault, InvalidFilterFault, TopicExpressionDialectUnknownFault,
            UnacceptableInitialTerminationTimeFault, SubscribeCreationFailedFault,
                InvalidMessageContentExpressionFault, InvalidTopicExpressionFault, UnrecognizedPolicyRequestFault,
                UnsupportedPolicyRequestFault, ResourceUnknownFault, NotifyMessageNotSupportedFault,
            InvalidProducerPropertiesExpressionFault {
            Subscribe subscribeRequest = new Subscribe();
            if (initialTerminationTime != null) {
                subscribeRequest.setInitialTerminationTime(
                        new JAXBElement<>(QNAME_INITIAL_TERMINATION_TIME,
                                String.class, initialTerminationTime));
            }
            subscribeRequest.setConsumerReference(consumer.getEpr());
            subscribeRequest.setFilter(new FilterType());
            if (topic != null) {
                TopicExpressionType topicExp = new TopicExpressionType();
                // Modified: Added dialect
                topicExp.setDialect(SIMPLE_DIALECT);
                topicExp.getContent().add(topic);
                subscribeRequest.getFilter().getAny().add(
                        new JAXBElement<>(QNAME_TOPIC_EXPRESSION,
                                TopicExpressionType.class, topicExp));
            }
            if (xpath != null) {
                QueryExpressionType xpathExp = new QueryExpressionType();
                xpathExp.setDialect(XPATH1_URI);
                xpathExp.getContent().add(xpath);
                subscribeRequest.getFilter().getAny().add(
                        new JAXBElement<>(QNAME_MESSAGE_CONTENT,
                                QueryExpressionType.class, xpathExp));
            }
            if (raw) {
                subscribeRequest.setSubscriptionPolicy(new Subscribe.SubscriptionPolicy());
                subscribeRequest.getSubscriptionPolicy().getAny().add(new UseRaw());
            }
            SubscribeResponse response = getBroker().subscribe(subscribeRequest);
            return new Subscription(response.getSubscriptionReference());
    }

    public void notify(String topic, Object msg) {
        notify(null, topic, msg);
    }

    public void notify(Referencable publisher, String topic, Object msg) {
        getBroker();

        Notify notify = new Notify();
        NotificationMessageHolderType holder = new NotificationMessageHolderType();
        if (publisher != null) {
            holder.setProducerReference(publisher.getEpr());
        }
        if (topic != null) {
            TopicExpressionType topicExp = new TopicExpressionType();
            // Modified: Added dialect
            topicExp.setDialect(SIMPLE_DIALECT);
            topicExp.getContent().add(topic);
            holder.setTopic(topicExp);
        }
        holder.setMessage(new NotificationMessageHolderType.Message());
        holder.getMessage().setAny(msg);
        notify.getNotificationMessage().add(holder);
        getBroker().notify(notify);
    }
}
