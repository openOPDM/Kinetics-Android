package org.kineticsfoundation.net.server;

import android.os.Bundle;
import android.test.AndroidTestCase;
import com.google.android.testing.mocking.UsesMocks;
import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.error.ServerError;
import com.lohika.restclient.RestParams;
import com.lohika.restclient.RestProcessor;
import com.lohika.sync.account.AccountManager;
import com.lohika.sync.core.exception.SynAuthErrorException;
import org.kineticsfoundation.net.api.HttpRequestException;
import org.springframework.http.HttpStatus;

import static com.google.android.testing.mocking.AndroidMock.*;
import static com.lohika.protocol.core.processor.ResponseFactory.makeErrorResponse;
import static com.lohika.protocol.core.processor.ResponseFactory.makeSuccessResponse;
import static org.easymock.EasyMock.isA;
import static org.kineticsfoundation.dao.DaoUtils.makeUniqueId;
import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes.SESSION_TOKEN_IS_EXPIRED;

/**
 * Test of token invalidation
 * Created by akaverin on 6/4/13.
 */
public class AbsManagerTest extends AndroidTestCase {

    private RestProcessor mockRestProcessor;
    private AccountManager mockAccountManager;
    private DummyManager manager;

    public void setUp() throws Exception {
        super.setUp();
        mockRestProcessor = createMock(RestProcessor.class);
        mockAccountManager = createMock(AccountManager.class, getContext(), "");
        manager = new DummyManager(mockRestProcessor, null, mockAccountManager);
    }

    @UsesMocks({RestProcessor.class, AccountManager.class})
    public void testExecuteRequestHttpError() throws Exception {
        Bundle responseBundle = new Bundle();
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        responseBundle.putSerializable(RestParams.EXTRA_HTTP_STATUS, httpStatus);

        expect(mockRestProcessor.processRequest(isA(Bundle.class))).andReturn(responseBundle);

        replay(mockAccountManager, mockRestProcessor);

        try {
            manager.executeRequest(new RequestBuilder("", ""));
            fail("Should throw exception!");
        } catch (HttpRequestException e) {
            assertEquals(httpStatus, e.getHttpStatus());
        }
        verify(mockAccountManager, mockRestProcessor);
    }

    @UsesMocks({RestProcessor.class, AccountManager.class})
    public void testExecuteRequestTokenExpired() throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder(makeUniqueId(), makeUniqueId());

        Bundle errorBundle = getHttpOkBundle();
        errorBundle.putSerializable(RestParams.EXTRA_RESPONSE_DATA, makeErrorResponse(new ServerError
                (SESSION_TOKEN_IS_EXPIRED, "")));

        expect(mockRestProcessor.processRequest(isA(Bundle.class))).andReturn(errorBundle);
        mockAccountManager.invalidateToken();
        expect(mockAccountManager.getBlockingAuthToken(anyBoolean())).andReturn("sometoken");

        Bundle successBundle = getHttpOkBundle();
        successBundle.putSerializable(RestParams.EXTRA_RESPONSE_DATA, makeSuccessResponse(requestBuilder
                .buildFunction()));
        expect(mockRestProcessor.processRequest(isA(Bundle.class))).andReturn(successBundle);

        replay(mockAccountManager, mockRestProcessor);

        ResponseContainer responseContainer = manager.executeRequest(requestBuilder);
        assertNotNull(responseContainer.getResponse().getFunction().getData());
        assertNull(responseContainer.getResponse().getError());

        verify(mockAccountManager, mockRestProcessor);
    }

    @UsesMocks({RestProcessor.class, AccountManager.class})
    public void testExecuteRequestTokenExpiredAndFail() throws Exception {
        RequestBuilder requestBuilder = new RequestBuilder(makeUniqueId(), makeUniqueId());

        Bundle errorBundle = getHttpOkBundle();
        errorBundle.putSerializable(RestParams.EXTRA_RESPONSE_DATA, makeErrorResponse(new ServerError
                (SESSION_TOKEN_IS_EXPIRED, "")));

        expect(mockRestProcessor.processRequest(isA(Bundle.class))).andReturn(errorBundle);
        mockAccountManager.invalidateToken();
        expect(mockAccountManager.getBlockingAuthToken(anyBoolean())).andReturn(null);

        replay(mockAccountManager, mockRestProcessor);

        try {
            manager.executeRequest(requestBuilder);
            fail("Should throw exception!!!");
        } catch (SynAuthErrorException e) {

        }
        verify(mockAccountManager, mockRestProcessor);
    }

    private Bundle getHttpOkBundle() {
        Bundle responseBundle = new Bundle();
        responseBundle.putSerializable(RestParams.EXTRA_HTTP_STATUS, HttpStatus.OK);
        return responseBundle;
    }

    private static final class DummyManager extends AbsManager {

        DummyManager(RestProcessor restProcessor, String manager, AccountManager accountManager) {
            super(restProcessor, manager, accountManager);
        }
    }
}
