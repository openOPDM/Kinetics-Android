package org.kineticsfoundation.net.server;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.restclient.RestProcessor;
import com.lohika.sync.account.AccountManager;
import org.codehaus.jackson.type.TypeReference;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.net.api.TestSessionManager;

import java.util.Collection;
import java.util.List;

import static com.lohika.protocol.core.processor.ResponseDataParser.extractData;
import static org.kineticsfoundation.net.NetworkConstants.Arguments.ID;
import static org.kineticsfoundation.net.NetworkConstants.Arguments.IDS;

/**
 * API implementation
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 9:38 AM
 */
public class TestSessionManagerImpl extends AbsManager implements TestSessionManager {

    private static final String TEST_SESSION = "testSession";
    private static final String MANAGER = "TestSessionManager";
    private static final TypeReference<List<TestSession>> dataType = new TypeReference<List<TestSession>>() {
    };

    public TestSessionManagerImpl(RestProcessor restProcessor, AccountManager accountManager) {
        super(restProcessor, MANAGER, accountManager);
    }

    public int add(String sessionToken, TestSession testSession) {
        ResponseContainer responseContainer = executeRequest(newTokenBuilder("add",
                sessionToken).addArg(TEST_SESSION, testSession));

        return extractData(responseContainer, "id", Integer.class);
    }

    public List<TestSession> getAll(String sessionToken) {
        RequestBuilder requestBuilder = newTokenBuilder("getAll", sessionToken);

        return extractData(executeRequest(requestBuilder), "testSession", dataType);
    }

    public TestSession getDetails(String sessionToken, Integer id) {
        RequestBuilder requestBuilder = newTokenBuilder("getDetails", sessionToken).addArg(ID, id);

        return extractData(executeRequest(requestBuilder), TEST_SESSION, TestSession.class);
    }

    public void delete(String sessionToken, Collection<Integer> ids) {
        executeRequest(newTokenBuilder("delete", sessionToken).addArg(IDS, ids));
    }

    public void modifyStatus(String sessionToken, List<Integer> ids, boolean isValid) {
        executeRequest(newTokenBuilder("modifyStatus", sessionToken).addArg(IDS, ids).addArg("valid", isValid));
    }
}
