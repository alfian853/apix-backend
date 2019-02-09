package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.ConflictException;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataUpdateService;
import com.future.apix.util.jsonquery.JsonQueryExecutor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiDataUpdateImpl implements ApiDataUpdateService {

    @Data
    private class SignaturePointer {
        HashMap<String, Object> methodField;
        HashMap<String, Object> queryField;
    }

    private JsonQueryExecutor queryExecutor = new JsonQueryExecutor();

    @Autowired
    ApiRepository apiRepository;

    private ObjectMapper mapper = new ObjectMapper();


    /**traverse ke field "section" or "path" or "link" or "method"
     * jika menemukan "_signature" maka return pointer dari apiData dan query
    **/
    private SignaturePointer getSignaturePointer(HashMap<String,Object> target, HashMap<String,Object> query){

        for (Object o : query.entrySet()) {
            Map.Entry<String, Object> pair = (Map.Entry) o;

            // jika child adalah http method, return child itu
            if (pair.getValue() instanceof HashMap) {
                return getSignaturePointer(
                        (HashMap<String, Object>) target.get(pair.getKey()),
                        (HashMap<String, Object>) pair.getValue());
            }
            else if(pair.getKey().equals("_signature")){
                SignaturePointer pointer = new SignaturePointer();
                pointer.setMethodField(target);
                pointer.setQueryField(query);
                return pointer;
            }
        }
        throw new InvalidRequestException("signature not found!");

    }

    @Override
    public RequestResponse doQuery(HashMap<String, Object> query) {
        String id = (String) query.get("id");

        if(id == null)throw new InvalidRequestException("json doesn't contain 'id' field");
        System.out.println("Search id: "+id);
        ApiProject project = apiRepository.findById(id).orElseThrow(DataNotFoundException::new);

        HashMap<String, Object> target = mapper.convertValue(project, HashMap.class);

        SignaturePointer pointer = getSignaturePointer(target, query);
        if(pointer.getQueryField().get("_signature").equals(
                pointer.getMethodField().get("_signature"))){
            throw new ConflictException("Edition Conflict!, Please refresh the tab");
        }

        queryExecutor.executeQuery(pointer.getMethodField(), pointer.getQueryField());

        /** karna pointer.getMethodField() me-return object yang didalam @target(bukan hasil clone),
         * maka tidak perlu di set lagi hasil query kedalam @target
         **/
        System.out.println(project.getSections().get("lendment").getPaths().get("/lendment/create").get("post"));

        project = mapper.convertValue(target, ApiProject.class);

        System.out.println(project.getSections().get("lendment").getPaths().get("/lendment/create").get("post"));
        apiRepository.save(project);
        return RequestResponse.success();
    }
}
