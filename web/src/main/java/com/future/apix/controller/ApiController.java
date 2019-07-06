package com.future.apix.controller;

import com.future.apix.entity.ApiProject;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.response.DownloadResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiDataUpdateService;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.service.command.Swagger2CodegenCommand;
import com.future.apix.service.command.Swagger2ExportCommand;
import com.future.apix.service.command.Swagger2ImportCommand;
import org.springframework.beans.factory.annotation.Autowired;
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
    ApiDataService apiDataService;

    @Autowired
    CommandExecutorService commandExecutor;

    @Autowired
    ApiDataUpdateService updateService;

    @PostMapping("/import")
    public RequestResponse importFromFile(@RequestParam("file")MultipartFile file,
                                          @RequestParam("type") String type,
                                          @RequestParam("team") String team){
        if(type.equals("oas-swagger2")){
            ProjectImportRequest request = new ProjectImportRequest();
            request.setFile(file);
            request.setTeam(team);
            return (commandExecutor.execute(Swagger2ImportCommand.class, request) == null)?
                    RequestResponse.failed() : RequestResponse.success();
        }
        else{
            throw new InvalidRequestException("oas format is not supported");
        }
    }

    @PostMapping("/{id}/export")
    public RequestResponse exportToOas(@PathVariable("id")String id, @RequestParam("type") String type){
        if(type.equals("oas-swagger2")){
            return commandExecutor.execute(Swagger2ExportCommand.class,id);
        }
        else{
            throw new InvalidRequestException("oas format is not supported");
        }
    }

    @GetMapping("/{id}")
    public ApiProject getById(@PathVariable("id") String id){
        ApiProject project = apiDataService.findById(id);
        return project;
    }

    @PutMapping("/{id}")
    public RequestResponse doApiDataQuery(@PathVariable("id")String id,@RequestBody HashMap<String,Object> query){
        return updateService.doQuery(id, query);
    }

    // digunakan untuk mendapatkan apiProject (filter by host, basePath, dan info)
    @GetMapping("/all/info")
    public List<ApiProject> findAllProjects() {return apiDataService.findAllProjects(); }

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
        return commandExecutor.execute(Swagger2CodegenCommand.class, id);
    }

    @PostMapping("/{id}/assign")
    public RequestResponse assignTeamToProject(
            @PathVariable("id") String id,
            @RequestParam("teamName") String teamName){
        return apiDataService.grantTeamAccess(id, teamName);
    }
}
