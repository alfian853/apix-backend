package com.future.apix.controller;

import com.future.apix.command.QueryExecutorCommand;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.command.model.QueryExecutorRequest;
import com.future.apix.command.model.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.enums.ProjectField;
import com.future.apix.repository.request.ProjectAdvancedQuery;
import com.future.apix.request.CreateTeamRequest;
import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.response.*;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiTeamService;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.command.Swagger2CodegenCommand;
import com.future.apix.command.Swagger2ExportCommand;
import com.future.apix.command.Swagger2ImportCommand;
import com.future.apix.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ApiDataService apiDataService;

    @Autowired
    private ApiTeamService apiTeamService;

    @Autowired
    private TeamService teamService;

    @Autowired
    CommandExecutorService commandExecutor;

    @PostMapping("/import")
    public RequestResponse importFromFile(@RequestParam("file")MultipartFile file,
                                          @RequestParam("type") String type,
                                          @RequestParam("team") String teamName,
                                          @RequestParam("isNewTeam") Boolean isNewTeam) {
        if(type.equals("oas-swagger2")){
            ProjectImportRequest request = new ProjectImportRequest();
            request.setFile(file);

            if(isNewTeam){
                Team team = new Team();
                team.setName(teamName);
                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                CreateTeamRequest createTeamRequest = new CreateTeamRequest();
                createTeamRequest.setTeamName(teamName);
                createTeamRequest.setCreator(user.getUsername());
                createTeamRequest.setAccess(TeamAccess.PUBLIC);
                createTeamRequest.setMembers(Collections.singletonList(user.getUsername()));
                request.setTeam(this.teamService.createTeam(createTeamRequest));
            }
            else{
                request.setTeam(
                    this.teamService.getTeamByName(teamName)
                );
            }

            return (commandExecutor.executeCommand(Swagger2ImportCommand.class, request) == null)?
                    RequestResponse.failed() : RequestResponse.success();
        }
        else{
            throw new InvalidRequestException("oas format is not supported");
        }
    }

    @GetMapping("/{id}/export")
    public RequestResponse exportToOas(@PathVariable("id")String id,
                                       @RequestParam("format") FileFormat format){
        ExportRequest request = new ExportRequest(id, format);
        return commandExecutor.executeCommand(Swagger2ExportCommand.class,request);
    }

    @GetMapping("/{id}")
    public ApiProject getById(@PathVariable("id") String id){
        ApiProject project = apiDataService.findById(id);
        return project;
    }

    @PutMapping("/{id}")
    public RequestResponse doApiDataQuery(@PathVariable("id")String id,@RequestBody HashMap<String,Object> query){
        return commandExecutor.executeCommand(QueryExecutorCommand.class,
                new QueryExecutorRequest(id, query));
    }

    @GetMapping
    public PagedResponse<ProjectDto> findByQuery(@RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                                 @RequestParam(value = "sort", required = false, defaultValue = "updated_at") String sort,
                                                 @RequestParam(value = "direction", required = false, defaultValue = "desc") String direction,
                                                 @RequestParam(value = "search", defaultValue = "") String search){
        ProjectAdvancedQuery query = ProjectAdvancedQuery.builder()
                .direction(Sort.Direction.fromString(direction))
                .page(page)
                .size(size)
                .sortBy(ProjectField.getProjectField(sort))
                .search(search)
                .build();
        return apiDataService.getByQuery(query);
    }

    @GetMapping("/team/{team}")
    public PagedResponse<ProjectDto> findByTeamAndQuery(@PathVariable(value = "team") String team,
                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                                        @RequestParam(value = "sort", required = false, defaultValue = "updated_at") String sort,
                                                        @RequestParam(value = "direction", required = false, defaultValue = "desc") String direction,
                                                        @RequestParam(value = "search", defaultValue = "") String search) {
        ProjectAdvancedQuery query = ProjectAdvancedQuery.builder()
            .direction(Sort.Direction.fromString(direction))
            .page(page)
            .size(size)
            .sortBy(ProjectField.getProjectField(sort))
            .search(search)
            .build();
        return apiDataService.getByTeamAndQuery(query, team);
    }

    // digunakan untuk delete by Id (sementara)
    @DeleteMapping("/{id}")
    public RequestResponse deleteById(@PathVariable("id") String id){
        return apiDataService.deleteById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectCreateResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return apiDataService.createProject(request);
    }

    @GetMapping("/{id}/codegen")
    public DownloadResponse getCodegen(@PathVariable("id") String id){
        return commandExecutor.executeCommand(Swagger2CodegenCommand.class, id);
    }

    @PostMapping("/{id}/assign")
    public RequestResponse assignTeamToProject(
            @PathVariable("id") String id,
            @RequestBody ProjectAssignTeamRequest request
    ){
        return apiTeamService.grantTeamAccess(id, request);
    }
}
