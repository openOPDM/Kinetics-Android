package org.kineticsfoundation.account;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import com.google.android.testing.mocking.UsesMocks;
import com.lohika.protocol.core.response.error.ServerError;
import com.lohika.sync.account.AccountManager;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.net.api.HttpRequestException;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.kineticsfoundation.net.server.RemoteApi;
import org.springframework.http.HttpStatus;

import static com.google.android.testing.mocking.AndroidMock.*;
import static org.kineticsfoundation.dao.DaoUtils.makeUniqueId;
import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes.USER_NOT_EXIST;

/**
 * Test for our {@link Authenticator} implementation
 * Created by akaverin on 5/31/13.
 */
public class AuthenticatorTest extends AndroidTestCase {

    private Account fakeAccount;
    private AccountManager mockAccountManager;
    private org.kineticsfoundation.net.api.AccountManager mockManagerApi;

    public void setUp() throws Exception {
        super.setUp();
        setContext(new AuthMockContext());

        mockManagerApi = createMock(org.kineticsfoundation.net.api.AccountManager.class);
        mockAccountManager = createMock(AccountManager.class, getContext(), Authenticator.ACCOUNT_TYPE);

        fakeAccount = new Account(makeUniqueId(), Authenticator.ACCOUNT_TYPE);
    }

    @UsesMocks(AccountManager.class)
    public void testGetAuthToken() throws Exception {
        //setup mocks
        final String pass = makeUniqueId();
        final Integer customerId = 1;
        final String token = makeUniqueId();

        expect(mockAccountManager.getPassword(fakeAccount)).andReturn(pass);
        expect(mockAccountManager.getData(fakeAccount, Authenticator.KEY_PROJECT_ID)).andReturn(customerId
                .toString());
        expect(mockManagerApi.login(fakeAccount.name, pass, customerId)).andReturn(token);

        replay(mockAccountManager, mockManagerApi);

        //positive case
        Authenticator authenticator = new Authenticator(getContext());

        Bundle result = authenticator.getAuthToken(null, fakeAccount, "", null);

        assertEquals(token, result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN));

        verify(mockAccountManager, mockManagerApi);
    }

    @UsesMocks(AccountManager.class)
    public void testGetAuthTokenServerErrorNoUser() throws Exception {
        String error = makeUniqueId();

        expect(mockAccountManager.getPassword(fakeAccount)).andReturn(makeUniqueId());
        expect(mockAccountManager.getData(fakeAccount, Authenticator.KEY_PROJECT_ID)).andReturn("0");
        expect(mockManagerApi.login((String) anyObject(), (String) anyObject(), (Integer) anyObject())).andThrow(new
                ProtocolRequestException(new ServerError(USER_NOT_EXIST, error)));
        mockAccountManager.removeAccounts();

        replay(mockAccountManager, mockManagerApi);

        Bundle result = new Authenticator(getContext()).getAuthToken(null, fakeAccount, "", null);
        assertEquals(Integer.toString(USER_NOT_EXIST), result.getString(android.accounts.AccountManager
                .KEY_ERROR_CODE));
        assertEquals(error, result.getString(android.accounts.AccountManager.KEY_ERROR_MESSAGE));

        verify(mockManagerApi, mockAccountManager);
    }

    @UsesMocks(AccountManager.class)
    public void testGetAuthTokenServerError() throws Exception {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;

        expect(mockAccountManager.getPassword(fakeAccount)).andReturn(makeUniqueId());
        expect(mockAccountManager.getData(fakeAccount, Authenticator.KEY_PROJECT_ID)).andReturn("0");
        expect(mockManagerApi.login((String) anyObject(), (String) anyObject(), (Integer) anyObject())).andThrow(new
                HttpRequestException(httpStatus));

        replay(mockAccountManager, mockManagerApi);

        Bundle result = new Authenticator(getContext()).getAuthToken(null, fakeAccount, "", null);
        assertEquals(Integer.toString(httpStatus.value()), result.getString(android.accounts.AccountManager
                .KEY_ERROR_CODE));
        assertEquals(httpStatus.getReasonPhrase(), result.getString(android.accounts.AccountManager.KEY_ERROR_MESSAGE));

        verify(mockManagerApi, mockAccountManager);
    }

    @UsesMocks(AccountManager.class)
    public void testGetAuthTokenNoPassword() throws Exception {

        expect(mockAccountManager.getPassword(fakeAccount)).andReturn(null);
        replay(mockAccountManager);

        Bundle result = new Authenticator(getContext()).getAuthToken(null, fakeAccount, "", null);
        assertTrue(result.containsKey(android.accounts.AccountManager.KEY_INTENT));

        verify(mockAccountManager);
    }

    private final class AuthMockContext extends MockContext {

        @Override
        public Context getApplicationContext() {
            return new KineticsApplication() {
                @Override
                public com.lohika.sync.account.AccountManager getAccountManager() {
                    return mockAccountManager;
                }

                @Override
                public RemoteApi getRemoteApi() {
                    return new RemoteApi(null) {
                        @Override
                        public org.kineticsfoundation.net.api.AccountManager getAccountManager() {
                            return mockManagerApi;
                        }
                    };
                }
            };
        }

        @Override
        public Object getSystemService(String name) {
            return null;
        }

        @Override
        public String getPackageName() {
            return getClass().getSimpleName();
        }
    }

}
