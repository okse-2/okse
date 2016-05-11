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
package no.ntnu.okse.examples.wsn;

import no.ntnu.okse.examples.TestClient;
import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3c.dom.Element;
import org.apache.cxf.wsn.client.Consumer;
import org.apache.cxf.wsn.client.Subscription;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public final class WSNClient implements TestClient {
    private static Logger log = Logger.getLogger(WSNClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 61000;
    private TestNotificationBroker notificationBroker;
    private Map<String, Subscription> subscriptions;
    private Map<String, Consumer> consumers;
    private Consumer.Callback callback;


    public WSNClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public WSNClient(String host, int port) {
        subscriptions = new HashMap<>();
        consumers = new HashMap<>();
        notificationBroker = new TestNotificationBroker(String.format("http://%s:%d", host, port));
        callback = new ExampleConsumer();
    }

    public static void main(String[] args) throws Exception {
        WSNClient client = new WSNClient();
        client.subscribe("example");
        client.publish("example", "Hello World!");
        Thread.sleep(5000);
        client.unsubscribe("example");
        System.exit(0);
    }

    @Override
    public void connect() {
        // Do nothing
    }

    @Override
    public void disconnect() {
        // Do nothing
    }

    @Override
    public void subscribe(String topic) {
        subscribe(topic, "localhost", 9001);
    }

    public void subscribe(String topic, String host, int port) {
        log.debug("Subscribing to topic: " + topic);
        Consumer consumer = new Consumer(callback,
                String.format("http://%s:%d/MyConsumer", host, port));
        consumers.put(topic, consumer);
        try {
            subscriptions.put(topic, notificationBroker.subscribe(consumer, topic));
            log.debug("Subscribed to topic: " + topic);
        }
        catch (Exception e) {
            log.error("Failed to subscribe", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        log.debug("Unsubscribing from topic: " + topic);
        if(subscriptions.containsKey(topic)) {
            try {
                Subscription subscription = subscriptions.get(topic);
                subscription.unsubscribe();
                log.debug("Unsubscribed from topic: " + topic);
            } catch (UnableToDestroySubscriptionFault | ResourceUnknownFault e) {
                log.error("Failed to unsubscribe", e);
            }
        }
        else {
            log.debug("Topic not found");
        }
        if(consumers.containsKey(topic)) {
            log.debug("Stopping consumer for topic: " + topic);
            Consumer consumer = consumers.get(topic);
            consumer.stop();
            log.debug("Consumer stopped");
        }
    }

    @Override
    public void publish(String topic, String content) {
        log.debug(String.format("Publishing to topic %s with content %s", topic, content));
        // TODO: Try to get rid of JAXB element wrapping
        notificationBroker.notify(topic, new JAXBElement<>(
                new QName("string"), String.class, content));
        log.debug("Published message successfully");
    }

    public void setCallback(Consumer.Callback callback) {
        this.callback = callback;
    }

    private class ExampleConsumer implements Consumer.Callback {
        public void notify(NotificationMessageHolderType message) {
            Object o = message.getMessage().getAny();
            System.out.println(message.getMessage().getAny());
            if (o instanceof Element) {
                System.out.println(((Element)o).getTextContent());
            }
        }
    }
}
