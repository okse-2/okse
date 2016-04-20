package no.ntnu.okse.protocol.xmpp;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.authorization.UserAuthorization;

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
