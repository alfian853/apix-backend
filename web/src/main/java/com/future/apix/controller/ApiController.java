package com.future.apix.controller;

import com.future.apix.entity.ApiProject;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiDataUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/project")
@CrossOrigin
public class ApiController {

    @Autowired
    ApiDataService apiDataService;

    @Autowired
    ApiDataUpdateService updateService;

    @PostMapping("/import")
    public RequestResponse importFromFile(@RequestParam("file")MultipartFile file, @RequestParam("type") String type){
        if(type.equals("oas-swagger2")){
            return apiDataService.importFromFile(file);
        }
        else{
            throw new InvalidRequestException("oas format is not supported");
        }
    }

    @GetMapping("/{id}")
    public ApiProject getById(@PathVariable("id") String id){
        return apiDataService.findById(id);
    }

    @PutMapping
    public RequestResponse doApiDataQuery(@RequestBody HashMap<String,Object> query){
        return updateService.doQuery(query);
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
}
