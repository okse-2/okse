/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Norwegian Defence Research Establishment / NTNU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package no.ntnu.okse.core.messaging;

import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.topic.Topic;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Created by Aleksander Skraastad (myth) on 4/17/15.
 * <p>
 * okse is licenced under the MIT licence.
 */
public class Message {

    // Immutable fields
    private final Publisher publisher;
    private final LocalDateTime created;
    private final Topic topic;
    private final String message;
    private final String messageID;
    private static Logger log;

    // Mutable fields
    private LocalDateTime processed;
    private boolean systemMessage;

    public Message(String message, Topic topic, Publisher publisher) {
        log = Logger.getLogger(Message.class.getName());
        this.publisher = publisher;
        this.topic = topic;
        this.created = LocalDateTime.now();
        this.processed = null;
        this.message = message;
        this.systemMessage = false;
        this.messageID = generateMessageID();
    }

    /**
     * Private method that generates an MD5 message ID
     *
     * @return A string containing the generated messageID
     */
    private String generateMessageID() {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(Long.toString(System.nanoTime()).getBytes());
            byte[] hash = m.digest();
            String messageID = new String(Hex.encode(hash));

            return messageID;
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not generate a message ID (MD5 algorithm not found)");
        }

        return null;
    }

    /**
     * Fetches the Message ID of this Message object.
     * @return A string containing the MessageID of this object.
     */
    public String getMessageID() {
        return this.messageID;
    }

    /**
     * Retrieves the raw message content of this message
     * @return A string containing the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Retrieves the Topic object this message is destined for
     * @return A topic object this message is to be broadcasted to
     */
    public Topic getTopic() {
        return this.topic;
    }

    /**
     * Retrieves the Publisher object this message originated from, null if non-registered or system-message
     * @return The publisher object this message came from, null otherwise.
     */
    public Publisher getPublisher() {
        return this.publisher;
    }

    /**
     * Checks to see if this message was sent from a registered publisher
     * @return
     */
    public boolean messageSentFromRegisteredPublisher() {
        if (this.publisher == null) return false;
        return true;
    }

    /**
     * Returns a LocalDateTime object of when this message object was initialized
     * @return A LocalDateTime object representing the creation time of this object.
     */
    public LocalDateTime getCreationTime() {
        return this.created;
    }

    /**
     * Checks to see if this message has been processed yet.
     * @return True if the message has been processed, false otherwise.
     */
    public boolean isProcessed() {
        if (this.processed == null) return false;
        return true;
    }

    /**
     * Flags this message as processed. This is a one-time operation and cannot be updated further.
     * @return The LocalDateTime object representing the time at which this command was first run.
     */
    public LocalDateTime setProcessed() {
        if (!isProcessed()) this.processed = LocalDateTime.now();
        return this.processed;
    }

    /**
     * Retrieves the completion time of this message (when it was processed).
     * @return A LocalDateTime object representing the time of completion/processing for this message. Returns
     *         null if it has not yet been processed.
     */
    public LocalDateTime getComplectionTime() {
        return this.processed;
    }

    /**
     * Sets the systemMessage status flag of this message. Used to check if this message was broadcasted from the brokering
     * system itself, and not from an external publisher.
     * @param flag True to set this message as a system message, false otherwise.
     */
    public void setSystemMessage(boolean flag) {
        this.systemMessage = flag;
    }

    /**
     * Checks to see if this message originated from the brokering system itself, and not an external publisher.
     * @return True if it was a system generated message, false otherwise.
     */
    public boolean isSystemMessage() {
        return this.systemMessage;
    }

    @Override
    public String toString() {
        return "Message (" + messageID + ") [systemMessage: " + systemMessage + ", created: " + created + ", " +
               "topic: " + topic.getFullTopicStringIgnoreCase() + "]";
    }
}