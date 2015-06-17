package org.kineticsfoundation.net.server;

import android.os.Bundle;
import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.error.ServerError;
import com.lohika.restclient.IntentValidationException;
import com.lohika.restclient.RestParams;
import com.lohika.restclient.RestProcessor;
import com.lohika.restclient.RestRequestBundleBuilder;
import com.lohika.sync.account.AccountManager;
import org.kineticsfoundation.net.NetworkConfig;
import org.kineticsfoundation.net.api.HttpRequestException;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.kineticsfoundation.net.api.RequestException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.kineticsfoundation.net.NetworkConstants.Arguments.SESSION_TOKEN;
import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes.SESSION_TOKEN_INVALID;
import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes.SESSION_TOKEN_IS_EXPIRED;
import static org.kineticsfoundation.util.NetworkUtils.getSessionToken;

/**
 * Common managers logic
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 4/24/13
 * Time: 2:27 PM
 */
abstract class AbsManager {

    private final String manager;
    private final RestProcessor restProcessor;
    private final AccountManager accountManager;
    private RestRequestBundleBuilder bundleBuilder;

    AbsManager(RestProcessor restProcessor, String manager, AccountManager accountManager) {
        this.manager = manager;
        this.restProcessor = restProcessor;
        this.accountManager = accountManager;
        try {
            this.bundleBuilder = new RestRequestBundleBuilder().putUrl(NetworkConfig.URL).putHttpMethod(HttpMethod
                    .POST)
                    .putReturnType(ResponseContainer.class);
        } catch (IntentValidationException e) {
            throw new RequestException(e);
        }
    }

    ResponseContainer executeRequest(RequestBuilder requestBuilder) {
        try {
            Bundle bundle = bundleBuilder.putRequestEntity(requestBuilder.build()).getBundle();
            Bundle responseBundle = restProcessor.processRequest(bundle);

            HttpStatus httpStatus = (HttpStatus) responseBundle.getSerializable(RestParams.EXTRA_HTTP_STATUS);
            if (!httpStatus.series().equals(HttpStatus.Series.SUCCESSFUL)) {
                throw new HttpRequestException(httpStatus);
            }
            ResponseContainer responseContainer = (ResponseContainer) responseBundle.getSerializable(RestParams
                    .EXTRA_RESPONSE_DATA);

            //looks we got some protocol error
            if (responseContainer.getResponse().getError() != null) {
                responseContainer = processServerError(responseContainer.getResponse().getError(), requestBuilder);
            }
            return responseContainer;

        } catch (IntentValidationException e) {
            throw new RequestException(e);
        }
    }

    private ResponseContainer processServerError(ServerError error, RequestBuilder requestBuilder) {
        switch (error.getCode()) {
            case SESSION_TOKEN_INVALID:
            case SESSION_TOKEN_IS_EXPIRED:
                accountManager.invalidateToken();
                requestBuilder.setArg(SESSION_TOKEN, getSessionToken(accountManager));

                //WARNING: recursive call, we should not get here again -> as we will get new valid token
                return executeRequest(requestBuilder);

            default:
                throw new ProtocolRequestException(error);
        }
    }

    RequestBuilder newBuilder(String method) {
        return new RequestBuilder(manager, method);
    }

    RequestBuilder newTokenBuilder(String method, String token) {
        return newBuilder(method).addArg(SESSION_TOKEN, token);
    }
}
