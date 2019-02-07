package com.future.apix.controller;

import com.future.apix.entity.ApiProject;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project")
@CrossOrigin
public class ApiController {

    @Autowired
    ApiDataService apiDataService;

    @PostMapping
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

}
