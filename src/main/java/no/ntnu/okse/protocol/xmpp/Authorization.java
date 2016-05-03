package no.ntnu.okse.protocol.xmpp;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.authorization.UserAuthorization;

/**
 * This clas is a security clas for XMPP.
 * Sequrity is not wanted at the moment so the methods returns true.
 */

public class Authorization implements UserAuthorization {
    @Override
    public boolean verifyCredentials(Entity jid, String passwordCleartext, Object credentials) {
        return true;
    }

    @Override
    public boolean verifyCredentials(String username, String passwordCleartext, Object credentials) {
        return true;
    }
}
