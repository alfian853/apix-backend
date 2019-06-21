package com.future.apix.controller;

import com.future.apix.entity.ApiProject;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.request.ProjectCreateRequest;
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
    public RequestResponse importFromFile(@RequestParam("file")MultipartFile file, @RequestParam("type") String type){
        if(type.equals("oas-swagger2")){
            return (commandExecutor.execute(Swagger2ImportCommand.class, file) == null)?
                    RequestResponse.failed() : RequestResponse.success();
        }
        else{
            throw new InvalidRequestException("oas format is not supported");
        }
    }

    @GetMapping("/{id}/export")
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

    // digunakan untuk mendapatkan semua apiProject (sementara)
    @GetMapping("/all")
    public List<ApiProject> findAll(){
        return apiDataService.findAll();
    }

    // digunakan untuk mendapatkan apiProject (filter by host, basePath, dan info)
    @GetMapping("/all/info")
    public List<ApiProject> findAllProjects() {return apiDataService.findAllProjects(); }

    // digunakan untuk delete by Id (sementara)
    @DeleteMapping("/{id}")
    public RequestResponse deleteById(@PathVariable("id") String id){
        return apiDataService.deleteById(id);
    }

    /*
    @GetMapping(value = "/bebek",
                params = {"name"}
    )
    public List<ApiProject> findByUser(@RequestParam("name") String username) {return apiDataService.findByUser(username); }
    */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectCreateResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return apiDataService.createProject(request);
    }

    @GetMapping("/{id}/codegen")
    public Object getCodegen(@PathVariable("id") String id){
        return commandExecutor.execute(Swagger2CodegenCommand.class, id);
    }
}
