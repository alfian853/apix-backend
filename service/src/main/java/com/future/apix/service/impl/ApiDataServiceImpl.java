package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.apidetail.Github;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.request.AdvancedQuery;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectDto;
import com.future.apix.response.PagedResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiTeamService;
import com.future.apix.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    private ProjectRepository apiRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ApiTeamService apiTeamService;

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
    }

    @Override
    public RequestResponse deleteById(String id){
        ApiProject project = apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
        if (apiTeamService.checkProjectOwner(id)) {
            apiRepository.deleteById(id);
            return RequestResponse.success("Project has been deleted!");
        }
        else throw new InvalidRequestException("You are not authorized to delete this project!");
    }

    @Override
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        ApiProject project = new ApiProject();
        project.setBasePath(request.getBasePath());
        project.setHost(request.getHost());
        project.setInfo(oMapper.convertValue(request.getInfo(), ProjectInfo.class));

        Team team;

        if(request.getIsNewTeam()){
            TeamCreateRequest teamCreateRequest = new TeamCreateRequest();
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            teamCreateRequest.setCreator(user.getUsername());
            teamCreateRequest.setTeamName(request.getTeam());
            teamCreateRequest.setAccess(TeamAccess.PUBLIC);
            team = teamService.createTeam(teamCreateRequest);
        }
        else{
            team = teamService.getTeamByName(request.getTeam());
        }
        project.setProjectOwner(team);
        project.getTeams().add(team.getName());

        project.setGithubProject(new Github());
        project.getInfo().setSignature(UUID.randomUUID().toString());
        project.getGithubProject().setSignature(UUID.randomUUID().toString());
        project.setSignature(UUID.randomUUID().toString());
        apiRepository.save(project);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess();
        response.setMessage("Project has been created!");
        response.setProjectId(project.getId());
        return response;
    }

    private PagedResponse<ProjectDto> mapToPagedResponse(Page<ApiProject> page){
        return PagedResponse.<ProjectDto>builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .offset(page.getPageable().getOffset())
                .pageNumber(page.getPageable().getPageNumber())
                .pageSize(page.getPageable().getPageSize())
                .numberOfElements(page.getNumberOfElements())
                .contents(
                        page.get()
                                .map(apiProject -> ProjectDto.builder()
                                        .id(apiProject.getId())
                                        .githubUsername(apiProject.getGithubProject().getOwner())
                                        .host(apiProject.getHost())
                                        .title(apiProject.getInfo().getTitle())
                                        .repository(apiProject.getGithubProject().getRepo())
                                        .owner(apiProject.getProjectOwner().getCreator())
                                        .updatedAt(apiProject.getUpdatedAt())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public PagedResponse<ProjectDto> getByQuery(AdvancedQuery query) {
        Page<ApiProject> page = apiRepository.findByQuery(query);
        return mapToPagedResponse(page);

    }

    @Override
    public PagedResponse<ProjectDto> getByTeamAndQuery(AdvancedQuery query, String team) {
        Criteria teamCriteria = Criteria.where("teams").is(team);
        Page<ApiProject> page = apiRepository.findByQuery(query, teamCriteria);
        return mapToPagedResponse(page);

    }
}
