package com.future.apix.util.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.apidetail.*;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.*;

public class SwaggerToApixOasConverter {

    @Autowired
    ObjectMapper oMapper;

    private static SwaggerToApixOasConverter converter;

    public synchronized static SwaggerToApixOasConverter getInstance(){
        if(converter == null){
            converter = new SwaggerToApixOasConverter();
        }
        return converter;
    }

    public void setMapper(ObjectMapper objectMapper){
        this.oMapper = objectMapper;
    }

    private void replaceRefWithId(Map<String, Object> data, Map<String, String> refToIdMap){
        for(Object obj : data.entrySet()){
            Map.Entry<String, Object> pair = (Map.Entry<String, Object>) obj;
            if((pair.getKey().equals("$ref") || pair.getKey().equals("ref"))
                    && pair.getValue() instanceof String){

                String ref = (String) pair.getValue();
                ref = ref.split("/",3)[2];
                pair.setValue(refToIdMap.get(ref));
            }
            else if(pair.getValue() instanceof HashMap){
                replaceRefWithId(this.toStrObjMap(pair.getValue()), refToIdMap);
            }
        }
    }

    private void setPathVariable(ApiProject project, String section, String path,
        Map.Entry<String,Object> statusAndOperationEntry, Map<String, String> refToIdMap) {

        List<Map<String, Object>> pathVariables = (List<Map<String, Object>>) statusAndOperationEntry.getValue();
        for (Map<String, Object> variable : pathVariables) {
            this.replaceRefWithId(variable, refToIdMap);
            Schema pathVariable = oMapper.convertValue(variable, Schema.class);
            project.getSections().get(section).getPaths().computeIfAbsent(path, k -> new Path())
                    .getPathVariables()
                    .put(pathVariable.getName(), pathVariable);
        }
    }

    private String getSection(Map<String,Object> methodData){
        return ((List<String>)methodData.get("tags")).get(0);
    }

    /**
     * melakukan mutasi pada object dengan hasil.
     * post : {
     *     "operatioId" : "createItemPOST",
     *     "description" : "",
     *     ............
     * }
     * **/
    private void setOperationData(ApiProject project,String section, String path, Map.Entry<String,Object> data, Map<String, String> refToIdMap){

        ApiMethodData methodData = new ApiMethodData();
        Map<String,Object> dataMap = toStrObjMap(data.getValue());
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
                Map<String, Object> parameter = toStrObjMap(paramObj);
                this.replaceRefWithId(parameter, refToIdMap);
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
                        Map<String, Object> schema = toStrObjMap(parameter.get("schema"));
                        Map<String, Object> properties = toStrObjMap(schema.get("properties"));
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
                    throw new RuntimeException("can't process parameter[in] = "+parameter.get("in"));
                }
            }

        }

        this.replaceRefWithId(toStrObjMap(dataMap.get("responses")), refToIdMap);
        Map<String, OperationDetail> responses = oMapper.convertValue(
            dataMap.get("responses"),
            TypeFactory.defaultInstance().constructMapType(HashMap.class,String.class, OperationDetail.class)
        );
        methodData.setResponses(responses);

        HttpMethod method = HttpMethod.valueOf(data.getKey().toUpperCase());
        if(
            SchemaValidator.isValid(methodData.getRequest().getHeadersLazily()) &&
            SchemaValidator.isValid(methodData.getRequest().getQueryParamsLazily()) &&
            (
                (method == HttpMethod.GET) || (methodData.getRequest() == null ||
                BodyValidator.isValid(methodData.getRequest()))
            )
        ){
        }
        else{
            throw new RuntimeException("Json OAS is not valid!");
        }
        project.getSections().get(section).getPaths()
                .computeIfAbsent(path,v -> new Path())
                .getMethods().put(data.getKey(),methodData);

    }

    /**
     * melakukan mutasi pada object @ApiProject dengan hasil:
     * "/api" :{
     *     "post" : {object},
     *     "get" : {object}
     * }
     * **/
    private void setPathData(ApiProject project, Map.Entry<String,Object> pathData, Map<String, String> refToIdMap){

        Iterator iterator = toStrObjMap(pathData.getValue()).entrySet().iterator();
        String section=null;

        while(iterator.hasNext()) {
            Map.Entry<String,Object> statusAndOperationEntry = (Map.Entry) iterator.next();
            if(section == null && !statusAndOperationEntry.getKey().equals("parameters")){
                section = this.getSection(toStrObjMap(statusAndOperationEntry.getValue()));
            }
            switch (statusAndOperationEntry.getKey()){
                case "parameters":
                    this.setPathVariable(project,section, pathData.getKey(), statusAndOperationEntry, refToIdMap);
                    break;
                default:
                    this.setOperationData(project, section, pathData.getKey(), statusAndOperationEntry, refToIdMap);
            }
        }

    }

    private Map<String,Object> toStrObjMap(Object object){
        return (Map<String,Object>) object;
    }


    public ApiProject convert(Map<String, Object> json){
        try {
            ApiProject project = new ApiProject();
            project.setBasePath((String) json.get("basePath"));
            project.setInfo(oMapper.convertValue(json.get("info"), ProjectInfo.class));
            project.setHost((String) json.get("host"));
            project.setSchema((List<String>) json.get("schema"));
            project.setExternalDocs(oMapper.convertValue(json.get("externalDocs"), Contact.class));
            project.setGithubProject(new Github());

            /* Copy Definitions Operation*/
            Map<String,Object> definitionsJson = toStrObjMap(json.get("definitions"));
            Map<String,Object> newDefinitionsJson = new HashMap<>();

            Map<String,String> refToIdMap = new HashMap<>();
            Iterator iterator = definitionsJson.entrySet().iterator();

            //create new definitions with uuid as key also keep mapping data from definition name to unique uuid
            while (iterator.hasNext()){
                Map.Entry<String,Object> nameAndDefinitionEntry = (Map.Entry) iterator.next();
                Map<String,Object> definitionData = toStrObjMap(nameAndDefinitionEntry.getValue());
                definitionData.put("name",nameAndDefinitionEntry.getKey());
                String key = UUID.randomUUID().toString();
                newDefinitionsJson.put(key, definitionData);
                refToIdMap.put(nameAndDefinitionEntry.getKey(), "#/definitions/"+key);
            }

            this.replaceRefWithId(newDefinitionsJson, refToIdMap);

            iterator = newDefinitionsJson.entrySet().iterator();
            Map<String, Definition> definitions = project.getDefinitions();
            while(iterator.hasNext()){
                Map.Entry<String,Object> nameAndDefinitionEntry = (Map.Entry) iterator.next();
                Definition definition = new Definition();
                definition.setSchema(oMapper.convertValue(nameAndDefinitionEntry.getValue(), Schema.class));
                definition.setName(definition.getSchema().getName());
                definition.getSchema().setName(null);

                if(SchemaValidator.isValid(definition.getSchema().getPropertiesLazily())){
                    definitions.put(nameAndDefinitionEntry.getKey(), definition);
                }
                else{
                    throw new RuntimeException("Json OAS is not valid!");
                }
            }

            /* Copy Paths Operation */
            Map<String,Object> paths = toStrObjMap(json.get("paths"));
            iterator = paths.entrySet().iterator();

            while(iterator.hasNext()){
                Map.Entry<String,Object> pathAndOperationsPair = (Map.Entry) iterator.next();
                this.setPathData(project, pathAndOperationsPair, refToIdMap);

            }
            /* End of Copy Paths Operation**/

            /* Append description and name of sections from tags */
            List<Object> tags = (List<Object>) json.get("tags");
            if(tags != null){
                for (Object tagObj : tags) {
                    Map<String, Object> tag = toStrObjMap(tagObj);
                    ApiSection section = project.getSections().get(tag.get("name"));
                    section.setInfo(oMapper.convertValue(tag, Tag.class));
                    section.getInfo().setSignature(UUID.randomUUID().toString());
                }
            }
            else {
                Map<String, ApiSection> sections = project.getSections();
                for (Map.Entry<String, ApiSection> sectionEntry : sections.entrySet() ) {
                    ApiSection section = sectionEntry.getValue();
                    section.getInfo().setName(sectionEntry.getKey());
                    section.getInfo().setSignature(UUID.randomUUID().toString());
                }
            }
            /* End of Append Tags */


            /* Security Definitions Operation */
            Map<String, Object> securityDefinitionJson = (Map<String, Object>) json.get("securityDefinitions");
            if(securityDefinitionJson != null){
                Map<String, SecurityScheme> securityScheme = project.getSecurityDefinitions();
                iterator = securityDefinitionJson.entrySet().iterator();

                while(iterator.hasNext()) {
                    Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                    SecurityScheme scheme = oMapper.convertValue(pair.getValue(), SecurityScheme.class);
                    securityScheme.put(pair.getKey(), scheme);
                }
            }
            return project;


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to import data : "+e.getMessage());
        }

    }

}
