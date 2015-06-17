package org.kineticsfoundation.net.api;

import org.kineticsfoundation.dao.model.Project;

import java.util.List;

/**
 * Project Manager API abstraction
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:20 PM
 */
public interface ProjectManager {

    List<Project> getProjectInfoList();

}
