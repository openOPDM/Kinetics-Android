package org.kineticsfoundation.net;

/**
 * Common values for network
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:30 PM
 */
public interface NetworkConstants {

    Integer NETWORK_ERROR = 1;

    interface Arguments {
        String SESSION_TOKEN = "sessionToken";
        String ID = "id";
        String IDS = "ids";
    }

    interface ErrorCodes {
        int SESSION_TOKEN_INVALID = 716;
        int SESSION_TOKEN_IS_EXPIRED = 717;
        int USER_NOT_EXIST = 801;
        int USER_NOT_ACTIVATED = 804;
        int TEST_NOT_FOUND = 807;
        int USER_IS_DISABLED = 808;
        int CUSTOMER_NOT_EXIST = 810;
    }


}
