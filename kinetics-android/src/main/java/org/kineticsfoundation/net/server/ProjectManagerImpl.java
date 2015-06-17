package org.kineticsfoundation.net.server;

import com.lohika.protocol.core.processor.ResponseDataParser;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.restclient.RestProcessor;
import com.lohika.sync.account.AccountManager;
import org.codehaus.jackson.type.TypeReference;
import org.kineticsfoundation.dao.model.Project;
import org.kineticsfoundation.net.api.ProjectManager;

import java.util.List;

/**
 * Project API implementation
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 9:19 AM
 */
public class ProjectManagerImpl extends AbsManager implements ProjectManager {

    private static final String MANAGER = "ProjectManager";
    private static final TypeReference<List<Project>> dataType = new TypeReference<List<Project>>() {
    };

    public ProjectManagerImpl(RestProcessor restProcessor, AccountManager accountManager) {
        super(restProcessor, MANAGER, accountManager);
    }

    public List<Project> getProjectInfoList() {
        ResponseContainer responseContainer = executeRequest(newBuilder("getProjectInfoList"));

        return ResponseDataParser.extractData(responseContainer, "project", dataType);
    }
}
