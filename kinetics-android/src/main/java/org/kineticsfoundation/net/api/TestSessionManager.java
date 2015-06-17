package org.kineticsfoundation.net.api;

import org.kineticsfoundation.dao.model.TestSession;

import java.util.Collection;
import java.util.List;

/**
 * Test Session Manager API abstraction
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:21 PM
 */
public interface TestSessionManager {

    int add(String sessionToken, TestSession testSession);

    List<TestSession> getAll(String sessionToken);

    TestSession getDetails(String sessionToken, Integer id);

    void delete(String sessionToken, Collection<Integer> ids);

    void modifyStatus(String sessionToken, List<Integer> ids, boolean isValid);

}
