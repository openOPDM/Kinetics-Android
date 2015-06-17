package org.kineticsfoundation.account;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.lohika.protocol.core.response.error.ServerError;
import de.akquinet.android.androlog.Log;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.R;
import org.kineticsfoundation.activity.LoginActivity;
import org.kineticsfoundation.net.api.HttpRequestException;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.springframework.http.HttpStatus;

import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes;

/**
 * Kinetics Account authenticator implementation
 * Created by akaverin on 5/29/13.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "org.kineticsfoundation.android.account";
    public static final String KEY_PROJECT_ID = "KEY_PROJECT_ID";
    public static final String KEY_AUTH_CHECK = "KEY_AUTH_CHECK";
    private final Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.i(this, "addAccount");

        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(KEY_AUTH_CHECK, true);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                     Bundle options) throws NetworkErrorException {
        Log.i(this, "confirmCredentials");

        // the password was missing or incorrect, return an Intent to an
        // Activity that will prompt the user for the password.
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle
            options) throws NetworkErrorException {
        Log.i(this, "getAuthToken");

        KineticsApplication application = (KineticsApplication) context.getApplicationContext();
        com.lohika.sync.account.AccountManager accountManager = application.getAccountManager();

        final String password = accountManager.getPassword(account);
        if (TextUtils.isEmpty(password)) {
            return confirmCredentials(response, account, options);
        }
        try {
            Integer customerId = Integer.parseInt(accountManager.getData(account, KEY_PROJECT_ID));

            String token = application.getRemoteApi().getAccountManager().login(account.name, password, customerId);

            final Bundle result = new Bundle();
            result.putSerializable(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            return result;

        } catch (ProtocolRequestException e) {
            ServerError error = e.getError();
            switch (error.getCode()) {
                case ErrorCodes.USER_IS_DISABLED:
                case ErrorCodes.USER_NOT_EXIST:
                case ErrorCodes.USER_NOT_ACTIVATED:
                case ErrorCodes.CUSTOMER_NOT_EXIST:
                    // UI will be notified by system about this change
                    accountManager.removeAccounts();
                    break;
            }
            return errorBundle(error.getCode().toString(), error.getDescription());

        } catch (HttpRequestException e) {
            HttpStatus httpStatus = e.getHttpStatus();
            return errorBundle(Integer.toString(httpStatus.value()), httpStatus.getReasonPhrase());
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return context.getString(R.string.authenticator_label);
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
                                    Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features) throws NetworkErrorException {
        return null;
    }

    private Bundle errorBundle(String code, String description) {
        final Bundle errorBundle = new Bundle();
        errorBundle.putString(AccountManager.KEY_ERROR_CODE, code);
        errorBundle.putString(AccountManager.KEY_ERROR_MESSAGE, description);
        return errorBundle;
    }
}
