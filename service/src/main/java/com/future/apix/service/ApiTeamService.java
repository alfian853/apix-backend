package com.future.apix.service;

import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.response.RequestResponse;

public interface ApiTeamService {

    // Project can invite team to collab
    RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request);

    // check if authorized user is projectOwner
    boolean checkProjectOwner(String id);
}
