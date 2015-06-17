package org.kineticsfoundation.net.api;

import com.lohika.protocol.core.response.error.ServerError;

/**
 * Exceptions specific to our application protocol
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 4/25/13
 * Time: 12:54 PM
 */
public class ProtocolRequestException extends RequestException {

    private final ServerError error;

    public ProtocolRequestException(ServerError error) {
        this.error = error;
    }

    public ServerError getError() {
        return error;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + error;
    }
}
