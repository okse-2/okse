package no.ntnu.okse.protocol.stomp;

import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class SystemAcknowledgementHandler implements StampyAcknowledgementHandler {
    @Override
    public void ackReceived(String s, String s1, String s2) throws Exception {

    }

    @Override
    public void nackReceived(String s, String s1, String s2) throws Exception {

    }

    @Override
    public void noAcknowledgementReceived(String s) {

    }
}
