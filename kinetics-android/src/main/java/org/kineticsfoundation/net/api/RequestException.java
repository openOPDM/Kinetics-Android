package org.kineticsfoundation.net.api;

/**
 * Network layer exceptions
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 4/24/13
 * Time: 2:08 PM
 */
public class RequestException extends RuntimeException {

    RequestException() {
    }

    public RequestException(Throwable throwable) {
        super(throwable);
    }

}
