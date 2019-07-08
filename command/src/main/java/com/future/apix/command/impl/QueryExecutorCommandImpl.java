package com.future.apix.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.QueryExecutorCommand;
import com.future.apix.command.model.QueryExecutorRequest;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.ConflictException;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.ProjectUpdateResponse;
import com.future.apix.util.jsonquery.JsonQueryExecutor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class QueryExecutorCommandImpl implements QueryExecutorCommand {

    @Data
    private class SignaturePointer {
        HashMap<String, Object> targetField;
        HashMap<String, Object> queryField;
    }

    private JsonQueryExecutor queryExecutor = new JsonQueryExecutor();

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    private ObjectMapper mapper;


    /**traverse ke field "section" or "path" or "link" or "method"
     * jika menemukan "_signature" maka return pointer dari apiData dan query
    **/
    private SignaturePointer getSignaturePointer(HashMap<String,Object> target, HashMap<String,Object> query){


        for (Object o : query.entrySet()) {
            Map.Entry<String, Object> pair = (Map.Entry) o;

            // jika child adalah http method, return child itu
            if (pair.getValue() instanceof HashMap) {
                SignaturePointer res = getSignaturePointer(
                        (HashMap<String, Object>) target.get(pair.getKey()),
                        (HashMap<String, Object>) pair.getValue());
                if(res != null)return res;
            }
            else if(pair.getKey().equals("_signature")){
                SignaturePointer pointer = new SignaturePointer();
                pointer.setTargetField(target);
                pointer.setQueryField(query);
                return pointer;
            }
        }
        return null;

    }
    @Override
    public ProjectUpdateResponse execute(QueryExecutorRequest request) {

        ApiProject project = apiRepository.findById(request.getId()).orElseThrow(DataNotFoundException::new);

        HashMap<String, Object> target = mapper.convertValue(project, HashMap.class);

        //validasi signature
        SignaturePointer pointer = getSignaturePointer(target, request.getQuery());
        if(pointer == null){
            throw new InvalidRequestException("signature not found!");
        }
        if(pointer.getTargetField() == null || pointer.getQueryField() == null){
            throw new InvalidRequestException("Invalid Edition Path!");
        }
        if(!pointer.getQueryField().get("_signature").equals(
                pointer.getTargetField().get("_signature"))){
            throw new ConflictException("Edition Conflict!");
        }

        ProjectUpdateResponse response = new ProjectUpdateResponse();
        response.setStatusToSuccess();

        if( queryExecutor.executeQuery(pointer.getTargetField(), pointer.getQueryField()) ){
            //generate signature baru setelah kontennya berhasil diupdate
            String newSignature = UUID.randomUUID().toString();
            response.setNewSignature(newSignature);
            pointer.getTargetField().put("_signature", newSignature);
        }
        else{//tidak ada update
            response.setNewSignature((String) pointer.getQueryField().get("_signature"));
        }

        project = mapper.convertValue(target, ApiProject.class);

        apiRepository.save(project);

        return response;
        /** karna pointer.getTargetField() me-return object yang didalam @target(bukan hasil clone),
         * maka tidak perlu di menge-set hasil query kedalam @target
         **/
    }
}
