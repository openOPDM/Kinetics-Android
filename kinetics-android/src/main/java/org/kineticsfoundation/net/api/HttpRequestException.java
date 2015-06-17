package org.kineticsfoundation.net.api;

import org.springframework.http.HttpStatus;

/**
 * Requests specific to HTTP errors
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 4/24/13
 * Time: 2:11 PM
 */
public class HttpRequestException extends RequestException {

    private final HttpStatus httpStatus;

    public HttpRequestException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return super.toString() + ", HTTP error: " + httpStatus.name();
    }
}
