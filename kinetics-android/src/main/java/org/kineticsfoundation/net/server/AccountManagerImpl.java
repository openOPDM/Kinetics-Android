package org.kineticsfoundation.net.server;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.restclient.RestProcessor;
import org.codehaus.jackson.type.TypeReference;
import org.kineticsfoundation.dao.model.Project;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.net.NetworkConstants;
import org.kineticsfoundation.net.api.AccountManager;

import java.util.List;

import static com.lohika.protocol.core.processor.ResponseDataParser.extractData;

/**
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:28 PM
 */
public class AccountManagerImpl extends AbsManager implements AccountManager {

    private static final TypeReference<List<Project>> dataType = new TypeReference<List<Project>>() {
    };
    private static final String MANAGER = "AccountManager";

    public AccountManagerImpl(RestProcessor restProcessor, com.lohika.sync.account.AccountManager accountManager) {
        super(restProcessor, MANAGER, accountManager);
    }

    @Override
    public List<Project> authenticate(String email, String pass) {
        RequestBuilder builder = newBuilder("authenticate").addArg(Arguments.EMAIL,
                email).addArg(Arguments.PASS_HASH, pass);

        return extractData(executeRequest(builder), Arguments.PROJECT, dataType);
    }

    public String login(String email, String pass, Integer customer) {
        RequestBuilder requestBuilder = newBuilder("login").addArg(
                Arguments.EMAIL, email).addArg(Arguments.PASS_HASH, pass).addArg(Arguments.PROJECT, customer);

        ResponseContainer responseContainer = executeRequest(requestBuilder);
        return extractData(responseContainer, NetworkConstants.Arguments.SESSION_TOKEN,
                String.class);
    }

    public void logout(String sessionToken) {
        executeRequest(newTokenBuilder("logout", sessionToken));
    }

    public void createUser(User user, String pass, Integer... projects) {
        RequestBuilder builder = newBuilder("createUser").addArg(Arguments.EMAIL, user.getEmail())
                .addArg(Arguments.FIRST_NAME, user.getFirstName()).addArg(Arguments.SECOND_NAME, user.getSecondName())
                .addArg(Arguments.PASS_HASH, pass)
                .addArg(Arguments.PROJECT, projects);

        if (user.getBirthday() != null) {
            builder.addArg(Arguments.BIRTHDAY,
                    user.getBirthday().getTime());
        }
        if (user.getGender() != null) {
            builder.addArg(Arguments.GENDER, user.getGender().name());
        }
        executeRequest(builder);
    }

    public void confirmCreate(String email, String confirmationCode) {
        RequestBuilder requestBuilder = newBuilder("confirmCreate").addArg("confirmationCode",
                confirmationCode).addArg(Arguments.EMAIL, email);

        executeRequest(requestBuilder);
    }

    private static final class Arguments {

        public static final String PROJECT = "project";
        public static final String EMAIL = "email";
        public static final String PASS_HASH = "passHash";
        public static final String FIRST_NAME = "firstName";
        public static final String SECOND_NAME = "secondName";
        public static final String GENDER = "gender";
        public static final String BIRTHDAY = "birthday";
    }

}
