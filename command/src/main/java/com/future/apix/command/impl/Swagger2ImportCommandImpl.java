package com.future.apix.command.impl;

import com.future.apix.command.Swagger2ImportCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.apidetail.*;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class Swagger2ImportCommandImpl implements Swagger2ImportCommand {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ObjectMapper oMapper;


    private HashMap<String,String> refDefinitions = new HashMap<>();

    public void setMapper(ObjectMapper objectMapper){
        this.oMapper = objectMapper;
    }

    private void replaceRefWithId(HashMap<String, Object> data){
        for(Object obj : data.entrySet()){
            Map.Entry<String, Object> pair = (Map.Entry<String, Object>) obj;
            if(pair.getKey().equals("$ref") || pair.getKey().equals("ref")){
                String ref = (String) pair.getValue();
                ref = ref.split("/",3)[2];
                pair.setValue(this.refDefinitions.get(ref));
            }
            else if(pair.getValue() instanceof HashMap){
                replaceRefWithId(this.toStrObjMap(pair.getValue()));
            }
        }
    }

    private void setApiPathVariable(ApiProject project, String section, String path, Map.Entry<String,Object> data) {
        List<HashMap<String, Object>> pathVariables = (List<HashMap<String, Object>>) data.getValue();
        for (HashMap<String, Object> variable : pathVariables) {
            this.replaceRefWithId(variable);
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
                this.replaceRefWithId(parameter);
                String input = (String) parameter.get("in");
                if(input.equals("query")){
                    Schema query = oMapper.convertValue(parameter, Schema.class);
                    query.setIn("query");
                    query.setDescription((String) parameter.get("description"));
                    methodData.getRequest().getQueryParamsLazily().put(
                            (String) parameter.get("name"), query
                    );
                }
                else if(input.equals("header")){
                    Schema header = oMapper.convertValue(parameter, Schema.class);
                    header.setIn("header");
                    header.setDescription((String) parameter.get("description"));
                    methodData.getRequest().getHeadersLazily().put(
                            (String) parameter.get("name"), header
                    );
                }
                else if(input.equals("body")){
                    OperationDetail body = methodData.getRequest();
                    body.setIn("body");
                    body.setName("body");
                    body.setDescription((String) parameter.get("description"));
                    body.setSchema(oMapper.convertValue(parameter.get("schema"), Schema.class));
                }
                else if(input.equals("formData")){
                    OperationDetail body = methodData.getRequest();
                    body.setIn("formData");
                    body.setName("formData");
                    body.getSchemaLazily().setType("object");
                    if(parameter.get("type").equals("object")){
                        HashMap<String, Object> schema = toStrObjMap(parameter.get("schema"));
                        HashMap<String, Object> properties = toStrObjMap(schema.get("properties"));
                        Schema newSchema = new Schema();
                        newSchema.setName(parameter.get("name").toString());
                        newSchema.setType("object");
                        newSchema.setProperties(oMapper.convertValue(properties, HashMap.class));
                        body.getSchemaLazily().getPropertiesLazily().put(parameter.get("name").toString(),newSchema);
                    }
                    else{
                        Schema newSchema = new Schema();
                        String newName = parameter.get("name").toString();
                        newSchema.setName(newName);
                        newSchema.setType(parameter.get("type").toString());
                        newSchema.setDescription(parameter.get("description").toString());
                        body.getSchemaLazily().getPropertiesLazily().put(newName, newSchema);
                    }


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

        this.replaceRefWithId(toStrObjMap(dataMap.get("responses")));
        HashMap<String, OperationDetail> responses = oMapper.convertValue(
                dataMap.get("responses"),
                TypeFactory.defaultInstance().constructMapType(HashMap.class,String.class, OperationDetail.class)
        );
        methodData.setResponses(responses);

        HttpMethod method = HttpMethod.valueOf(data.getKey().toUpperCase());
        if(
                SchemaValidator.isValid(methodData.getRequest().getHeadersLazily()) &&
                        SchemaValidator.isValid(methodData.getRequest().getQueryParamsLazily()) &&
                        ((method == HttpMethod.GET) || (methodData.getRequest() == null ||
                                BodyValidator.isValid(methodData.getRequest())))

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
     * return : data dari link(semua http method dari link tersebut).
     * contoh :
     * "/api" :{
     *     "post" : {object},
     *     "get" : {object}
     * }
     * **/
    //                                                    Path,DataPath
    private void setLinkData(ApiProject project, Map.Entry<String,Object> pathsData){

        Iterator iterator = toStrObjMap(pathsData.getValue()).entrySet().iterator();
        String section=null;

        while(iterator.hasNext()) {
            //httpMethod,operationData
            Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
            if(section == null && !pair.getKey().equals("parameters")){
                section = this.getSection(toStrObjMap(pair.getValue()));
            }
            switch (pair.getKey()){
                case "parameters":
                    this.setApiPathVariable(project,section, pathsData.getKey(), pair);
                    break;
                default:
                    this.setApiMethodData(project, section, pathsData.getKey(), pair);
            }
        }

    }

    private HashMap<String,Object> toStrObjMap(Object object){
        return (HashMap<String,Object>) object;
    }


    @Override
    public ApiProject execute(ProjectImportRequest request) {

        HashMap<String,Object> json = null;
        MultipartFile file = request.getFile();
        Team team = Optional.ofNullable(teamRepository.findByName(request.getTeam()))
                .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
        if (Optional.of(file).isPresent()) System.out.println("File is exists!");

        try {
            json = oMapper.readValue(file.getInputStream(), HashMap.class);
            ApiProject project = new ApiProject();
            project.setBasePath((String) json.get("basePath"));
            project.setInfo(oMapper.convertValue(json.get("info"), ProjectInfo.class));
            project.setHost((String) json.get("host"));
            project.setSchema((List<String>) json.get("schema"));
            project.setExternalDocs(oMapper.convertValue(json.get("externalDocs"), Contact.class));
            project.getTeams().add(request.getTeam()); // add team
            project.setGithubProject(new Github());

            /* Copy Definitions Operation*/
            HashMap<String,Object> definitionsJson = toStrObjMap(json.get("definitions"));
            HashMap<String,Object> newDefinitionsJson = new HashMap<>();

            this.refDefinitions = new HashMap<>();
            Iterator iterator = definitionsJson.entrySet().iterator();

            while (iterator.hasNext()){
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                HashMap<String,Object> isinya = toStrObjMap(pair.getValue());
                isinya.put("name",pair.getKey());
                String key = UUID.randomUUID().toString();
                newDefinitionsJson.put(key, isinya);
                this.refDefinitions.put(pair.getKey(), "#/definitions/"+key);
            }

            this.replaceRefWithId(newDefinitionsJson);

            iterator = newDefinitionsJson.entrySet().iterator();
            HashMap<String, Definition> definitions = project.getDefinitions();
            while(iterator.hasNext()){
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
//                Definition definition = oMapper.convertValue(pair.getValue(),Definition.class);
                Definition definition = new Definition();
                definition.setSchema(oMapper.convertValue(pair.getValue(), Schema.class));
                definition.setName(definition.getSchema().getName());
                definition.getSchema().setName(null);
                // validate content
                if(SchemaValidator.isValid(definition.getSchema().getPropertiesLazily())){
                    definitions.put(pair.getKey(), definition);
                }
                else{
                    throw new InvalidRequestException("Json OAS is not valid!");
                }
            }

            /* Copy Paths Operation */
            //     link, listOfMethod
            HashMap<String,Object> paths = toStrObjMap(json.get("paths"));
            iterator = paths.entrySet().iterator();

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
                    ApiSection section = project.getSections().get(tag.get("name"));
                    section.setInfo(oMapper.convertValue(tag, Tag.class));
                    section.getInfo().setSignature(UUID.randomUUID().toString());
                }
            }
            /* End of Append Tags */


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
            return project;

//            return RequestResponse.success("Data Imported");

        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidRequestException("Failed to import data : "+e.getMessage());
        }

    }
}
