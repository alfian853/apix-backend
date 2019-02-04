package com.future.apix.controller;

import com.future.apix.exception.InvalidRequestException;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
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

}
