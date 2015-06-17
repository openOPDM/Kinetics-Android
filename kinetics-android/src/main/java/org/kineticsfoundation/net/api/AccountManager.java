package org.kineticsfoundation.net.api;

import org.kineticsfoundation.dao.model.Project;
import org.kineticsfoundation.dao.model.User;

import java.util.List;

/**
 * Account Manager API abstraction
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:12 PM
 */
public interface AccountManager {

    /**
     * Perform authentication
     *
     * @param email client email
     * @param pass  client password
     * @return list of {@link org.kineticsfoundation.dao.model.Project} available to login
     */
    List<Project> authenticate(String email, String pass);

    /**
     * Performs login operation
     *
     * @param email      client email
     * @param pass       client password
     * @param customerId client's customer
     * @return sessionToken provided by server
     */
    String login(String email, String pass, Integer customerId);

    void logout(String sessionToken);

    void createUser(User user, String pass, Integer... projects);

    void confirmCreate(String email, String confirmationCode);

}
