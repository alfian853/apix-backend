package com.future.apix.service;

import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.response.RequestResponse;

public interface ApiTeamService {
    RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request);
}
