package org.kineticsfoundation.account;

import android.accounts.AbstractAccountAuthenticator;
import com.lohika.sync.account.AbsAuthenticationService;

/**
 * Our Authentication provider
 * Created by akaverin on 5/29/13.
 */
public class AuthenticationService extends AbsAuthenticationService {
    @Override
    protected AbstractAccountAuthenticator setupAuthenticator() {
        return new Authenticator(getApplicationContext());
    }
}
