package com.future.apix.service.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.service.command.Swagger2ImportCommand;
import com.future.apix.entity.ApiMethodData;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import com.future.apix.entity.apidetail.*;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class Swagger2ImportCommandImpl implements Swagger2ImportCommand {

    @Autowired
    ApiRepository apiRepository;


    private ObjectMapper oMapper = new ObjectMapper();


    private void setApiLinkPathVariable(ApiProject project, String section, String path, Map.Entry<String,Object> data) {
        List<HashMap<String, Object>> pathVariables = (List<HashMap<String, Object>>) data.getValue();
        for (HashMap<String, Object> variable : pathVariables) {
            Schema pathVariable = oMapper.convertValue(variable, Schema.class);
            project.getSections().get(section).getPaths().computeIfAbsent(path, k -> new Path())
                    .getPathVariables()
                    .put(pathVariable.getName(), pathVariable);
        }
    }

    private String getSection(HashMap<String,Object> methodData){
        return ((List<String>)methodData.get("tags")).get(0);
    }

    /**
     * return : data dari method api{post,get,put,delete}.
     * contoh :
     * post : {
     *     "operatioId" : "createItemPOST",
     *     "description" : "",
     *     ............
     * }
     * **/                                                      //path             method,methodData
    private void setApiMethodData(ApiProject project,String section, String path, Map.Entry<String,Object> data){

        ApiMethodData methodData = new ApiMethodData();
        HashMap<String,Object> dataMap = toStrObjMap(data.getValue());
        methodData.setOperationId((String) dataMap.get("operationId"));
        methodData.setSummary((String) dataMap.get("summary"));
        methodData.setDescription((String) dataMap.get("description"));
        methodData.setConsumes((List<String>) dataMap.get("consumes"));
        methodData.setProduces((List<String>) dataMap.get("produces"));
        methodData.setDeprecated((Boolean) dataMap.get("deprecated"));
        Path pathOfMethod = project.getSections()
                .computeIfAbsent(section, v -> new ApiSection()).getPaths()
                .computeIfAbsent(path, v -> new Path());

        if(dataMap.get("parameters") != null){

            List<Object> parameters = (List<Object>) dataMap.get("parameters");

            for (Object paramObj : parameters) {
                HashMap<String, Object> parameter = toStrObjMap(paramObj);
                String input = (String) parameter.get("in");
                if(input.equals("query")){
                    methodData.getRequestBody().getQueryParams().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Schema.class)
                    );
                }
                else if(input.equals("header")){
                    methodData.getRequestBody().getHeaders().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Schema.class)
                    );
                }
                else if(input.equals("body")){
                    RequestBody body = methodData.getRequestBody();
                    body.setIn("body");
                    body.setName("body");
                    body.setSchema(oMapper.convertValue(parameter.get("schema"), Schema.class));
                }
                else if(input.equals("formData")){
                    RequestBody body = methodData.getRequestBody();
                    body.setIn("formData");
                    body.setName("formData");
                    body.getSchema().setType("object");
                    body.getSchema().getProperties().put(
                            parameter.get("name").toString(),
                            oMapper.convertValue(parameter, Schema.class)
                    );

                }
                else if(input.equals("path")){
                    if(pathOfMethod.getPathVariables().containsKey(parameter.get("name"))){
                        continue;
                    }
                    Schema pathVariable = oMapper.convertValue(parameter, Schema.class);
                    pathOfMethod.getPathVariables().put(pathVariable.getName(),pathVariable);
                }
                else{
                    throw new InvalidRequestException("can't process parameter[in] = "+parameter.get("in"));
                }
            }

        }

//#debug        System.out.println("Tes : "+path+" "+data.getKey());

        HashMap<String, RequestBody> responses = (HashMap<String, RequestBody>) dataMap.get("responses");
        methodData.setResponses(responses);

        HttpMethod method = HttpMethod.valueOf(data.getKey().toUpperCase());
        if(
                SchemaValidator.isValid(methodData.getRequestBody().getHeaders()) &&
                        SchemaValidator.isValid(methodData.getRequestBody().getQueryParams()) &&
                        ((method == HttpMethod.GET) || (methodData.getRequestBody() == null ||
                                BodyValidator.isValid(methodData.getRequestBody())))

        ){
        }
        else{
            throw new InvalidRequestException("Json OAS is not valid!");
        }
        project.getSections().get(section).getPaths()
                .computeIfAbsent(path,v -> new Path())
                .getMethods().put(data.getKey(),methodData);

    }

    /**
     * return : data dari link(sudah include semua http method dari link tersebut).
     * contoh :
     * "/api" :{
     *     "post" : {object},
     *     "get" : {object}
     * }
     * **/
    //                                                    Path,DataPath
    private void setLinkData(ApiProject project, Map.Entry<String,Object> pathsData){
        Iterator iterator = toStrObjMap(pathsData.getValue()).entrySet().iterator();
        //<http method, operation data>
        HashMap<String, ApiMethodData> result = new HashMap<>();
        String section=null;
        while(iterator.hasNext()) {
            //httpMethod,operationData
            Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
            if(section == null){
                section = this.getSection(toStrObjMap(pair.getValue()));
            }
            switch (pair.getKey()){
                case "parameters":
                    setApiLinkPathVariable(project,section, pathsData.getKey(), pair);
                    break;
                default:
                    setApiMethodData(project, section, pathsData.getKey(), pair);
            }
        }

    }

    private HashMap<String,Object> toStrObjMap(Object object){
        return (HashMap<String,Object>) object;
    }

    @Override
    public RequestResponse executeCommand(MultipartFile file) {

        HashMap<String,Object> json = null;
        try {
            json = oMapper.readValue(file.getInputStream(), HashMap.class);
            ApiProject project = new ApiProject();
            project.setBasePath((String) json.get("basePath"));
            project.setInfo(oMapper.convertValue(json.get("info"), ProjectInfo.class));
            project.setHost((String) json.get("host"));
            project.setSchemes((List<String>) json.get("schemes"));
            project.setExternalDocs(oMapper.convertValue(json.get("externalDocs"), Contact.class));

            /* Copy Paths Operation */
            //     link, listOfMethod
            HashMap<String,Object> paths = toStrObjMap(json.get("paths"));
            Iterator iterator = paths.entrySet().iterator();

            while(iterator.hasNext()){
                //       link,ListOfMethod
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();

                //           sectionName             link
                this.setLinkData(project, pair);

            }

            /* End of Copy Paths Operation**/

            /* Append Description of Sections from Tags */
            List<Object> tags = (List<Object>) json.get("tags");
            if(tags != null){
                for (Object tagObj : tags) {
                    HashMap<String, Object> tag = toStrObjMap(tagObj);
                    if(!tag.containsKey("externalDocs")){
                        tag.put("externalDocs",new Contact());
                    }
                    ApiSection section = project.getSections().get(tag.get("name"));
                    section.setInfo(oMapper.convertValue(tag, Tag.class));
                }
            }
            /* End of Append Tags */


            /* Copy Definitions Operation*/
            HashMap<String,Object> definitionsJson = toStrObjMap(json.get("definitions"));
            HashMap<String, Schema> definitions = project.getDefinitions();
            iterator = definitionsJson.entrySet().iterator();

            while(iterator.hasNext()){
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                Schema definition = oMapper.convertValue(pair.getValue(),Schema.class);

                // validate content
                if(SchemaValidator.isValid(definition.getProperties())){
                    definitions.put(pair.getKey(), definition);
                }
                else{
                    throw new InvalidRequestException("Json OAS is not valid!");
                }
            }

            /* Security Definitions Operation */
            HashMap<String, Object> securityDefinitionJson = (HashMap<String, Object>) json.get("securityDefinitions");
            if(securityDefinitionJson != null){
                HashMap<String, SecurityScheme> securityScheme = project.getSecurityDefinitions();
                iterator = securityDefinitionJson.entrySet().iterator();

                while(iterator.hasNext()) {
                    Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                    SecurityScheme scheme = oMapper.convertValue(pair.getValue(), SecurityScheme.class);
                    securityScheme.put(pair.getKey(), scheme);
                }
            }

            apiRepository.save(project);

            return RequestResponse.success("Data Imported");

        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidRequestException("Failed to import data : "+e.getMessage());
        }

    }
}
