package com.future.apix.controller;

import com.future.apix.command.QueryExecutorCommand;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.command.model.QueryExecutorRequest;
import com.future.apix.command.model.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.response.DownloadResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiTeamService;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.command.Swagger2CodegenCommand;
import com.future.apix.command.Swagger2ExportCommand;
import com.future.apix.command.Swagger2ImportCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ApiController {

    @Autowired
    private ApiDataService apiDataService;

    @Autowired
    private ApiTeamService apiTeamService;

    @Autowired
    CommandExecutorService commandExecutor;

    @PostMapping("/import")
    public RequestResponse importFromFile(@RequestParam("file")MultipartFile file,
                                          @RequestParam("type") String type,
                                          @RequestParam("team") String team,
                                          @RequestParam("isNewTeam") Boolean isNewTeam) {
        if(type.equals("oas-swagger2")){
            ProjectImportRequest request = new ProjectImportRequest();
            request.setFile(file);
            request.setIsNewTeam(isNewTeam);
            request.setTeam(team);
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

    // digunakan untuk mendapatkan apiProject (filter by host, basePath, dan info)
    @GetMapping("/all/info")
    public List<ApiProject> findAllProjects() {return apiDataService.findAllProjects(); }

    @GetMapping
    public Page<ApiProject> findAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    @RequestParam(value = "direction", required = false) String direction) {
        if (!direction.isEmpty() && direction.equals("asc")){
            return apiDataService.findAll(PageRequest.of(page, size, Sort.Direction.ASC, sort));
        }
        else if (!direction.isEmpty() && direction.equals("desc")) {
            return apiDataService.findAll(PageRequest.of(page, size, Sort.Direction.DESC, sort));
        }
        else throw new DataNotFoundException("Projects is not available at the moment!");
    }

    @GetMapping("/search")
    public Page<ApiProject> findSearch(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       @RequestParam(value = "search") String search) {
        return apiDataService.findSearch(search, PageRequest.of(page, size));
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
