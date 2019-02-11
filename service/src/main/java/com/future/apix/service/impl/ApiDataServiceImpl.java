package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiMethodData;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import com.future.apix.entity.apidetail.*;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    ApiRepository apiRepository;


    private ObjectMapper oMapper = new ObjectMapper();

    /**
     * return : data dari method api{post,get,put,delete}.
     * contoh :
     * post : {
     *     "operatioId" : "createItemPOST",
     *     "description" : "",
     *     ............
     * }
     * **/
    private ApiMethodData getApiMethodData(HashMap<String,Object> data){

        ApiMethodData methodData = new ApiMethodData();
        methodData.setOperationId((String) data.get("operationId"));
        methodData.setSummary((String) data.get("summary"));
        methodData.setDescription((String) data.get("description"));
        methodData.setConsumes((List<String>) data.get("consumes"));
        methodData.setProduces((List<String>) data.get("produces"));
        methodData.setDeprecated((Boolean) data.get("deprecated"));
        if(data.get("parameters") != null){

            List<Object> parameters = (List<Object>) data.get("parameters");

            for (Object paramObj : parameters) {
                HashMap<String, Object> parameter = toStrObjMap(paramObj);
                String input = (String) parameter.get("in");
                if(input.equals("query")){
                    methodData.getQueryParams().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Schema.class)
                    );
                }
                else if(input.equals("header")){
                    methodData.getHeaders().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Schema.class)
                    );
                }
                else if(input.equals("body")){
                    RequestBody body = oMapper.convertValue(parameter, RequestBody.class);
                    methodData.setBody(body);
                }
                else if(input.equals("formData")){
                    methodData.setBody(oMapper.convertValue(parameter, RequestBody.class));
                }
                else if(!input.equals("path")){
                    throw new InvalidRequestException("can't process parameter[in] = "+parameter.get("in"));
                }
            }

        }

        HashMap<String, RequestBody> responses = (HashMap<String, RequestBody>) data.get("responses");
        methodData.setResponses(responses);

        return methodData;
    }

    /**
     * return : data dari link(sudah include semua http method dari link tersebut).
     * contoh :
     * "/api" :{
     *     "post" : {object},
     *     "get" : {object}
     * }
     * **/
    private Path getLinkData(HashMap<String, Object> data){
        Iterator iterator = data.entrySet().iterator();
        HashMap<String, ApiMethodData> result = new HashMap<>();
        while(iterator.hasNext()) {
            Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
            ApiMethodData methodData = getApiMethodData( toStrObjMap(pair.getValue()) );

            HttpMethod method = HttpMethod.valueOf(pair.getKey().toUpperCase());
            // validating content
            if(
                    SchemaValidator.isValid(methodData.getHeaders()) &&
                            SchemaValidator.isValid(methodData.getQueryParams()) &&
                            ((method == HttpMethod.GET) || (methodData.getBody() == null ||
                            BodyValidator.isValid(methodData.getBody())))

            ){
                result.put(pair.getKey().toLowerCase(),methodData);
            }
            else{
                throw new InvalidRequestException("Json OAS is not valid!");
            }
        }
        Path path = new Path();
        path.setMethods(result);
        path.setPathVariables(this.getPathVariables(data));
        return path;
    }

    private HashMap<String, ApiSection> getSections(List<HashMap<String,String>> tags){

        HashMap<String, ApiSection> sections = new HashMap<>();

        for(HashMap<String,String> tag : tags){
            String tagName = tag.get("name");
            if(!sections.containsKey(tagName)){
                sections.put(tagName,new ApiSection());
            }
        }

        return sections;
    }

    //input : <Http Method,Method Data> path
    //output : pathVariables
    HashMap<String, Schema> getPathVariables(HashMap<String,Object> path){
        String firstKey = path.keySet().iterator().next();
        List<HashMap<String,String>> parameters
                = (List<HashMap<String,String>>) toStrObjMap(path.get(firstKey)).get("parameters");

        if(parameters == null){
            return null;
        }
        HashMap<String, Schema> res = new HashMap<>();
        for(HashMap<String,String> param : parameters){
            if(param.get("in").equals("path")){

                Schema pathValue = oMapper.convertValue(param,Schema.class);

                if(!SchemaValidator.isValid(pathValue)){
                    throw new InvalidRequestException("Json OAS is not valid!");
                }

                res.put(pathValue.getName(), pathValue);
            }
        }

        return res;

    }


    //input : object list of method = [@get,@post,@put....]
    //output : section name
    private String getSectionName(Object methodsObj){
        HashMap<String,Object> methods = toStrObjMap(methodsObj);
        String httpMethod = methods.keySet().iterator().next();
        HashMap<String, Object> methodObj = toStrObjMap(methods.get(httpMethod));
        return ((List<String>)methodObj.get("tags")).get(0);
    }

    private HashMap<String,Object> toStrObjMap(Object object){
        return (HashMap<String,Object>) object;
    }

    @Override
    public RequestResponse importFromFile(MultipartFile file) {

        HashMap<String,Object> json = null;
        try {
            json = oMapper.readValue(file.getInputStream(), HashMap.class);
            ApiProject project = new ApiProject();
            project.setBasePath((String) json.get("basePath"));
            project.setInfo(oMapper.convertValue(json.get("info"),ProjectInfo.class));
            project.setHost((String) json.get("host"));
            project.setSchemes((List<String>) json.get("schemes"));
            project.setExternalDocs(oMapper.convertValue(json.get("externalDocs"), Contact.class));

            /* Copy Paths Operation */
            //     link, listOfMethod
            HashMap<String,Object> paths = toStrObjMap(json.get("paths"));
            Iterator iterator = paths.entrySet().iterator();

            //     section,<link, HashMapOfMethod>
            HashMap<String, ApiSection> sections = this.getSections((List<HashMap<String, String>>) json.get("tags"));

            while(iterator.hasNext()){
                //        link,ListOfMethod
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();

                String sectionName = this.getSectionName(pair.getValue());

                //           sectionName             link
                sections.get(sectionName).getPaths().put(pair.getKey(), getLinkData(toStrObjMap(pair.getValue())));


            }
            project.setSections(sections);

            /* End of Copy Paths Operation**/

            /* Append Description of Sections from Tags */
            List<Object> tags = (List<Object>) json.get("tags");
//            HashMap<String, String> tagName = tags.stream().collect(Collectors.toMap(tags.get("name")))
            HashMap<String, Tag> tagDescription = new HashMap<>();
            for (Object tagObj : tags) {
                HashMap<String, Object> tag = toStrObjMap(tagObj);
                if(!tag.containsKey("externalDocs")){
                    tag.put("externalDocs",new Contact());
                }
                ApiSection section = sections.get(tag.get("name"));
                section.setInfo(oMapper.convertValue(tag, Tag.class));
            }
            /* End of Append Tags */


            /* Copy Definitions Operation*/
            HashMap<String,Object> definitionsJson = toStrObjMap(json.get("definitions"));
            HashMap<String, Definition> definitions = project.getDefinitions();
            iterator = definitionsJson.entrySet().iterator();

            while(iterator.hasNext()){
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                Definition definition = oMapper.convertValue(pair.getValue(),Definition.class);

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

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public List<ApiProject> findAll() {
        return apiRepository.findAll();
    }

    @Override
    public RequestResponse deleteById(String id){
        ApiProject project = apiRepository.findById(id).orElseThrow(DataNotFoundException::new);
        apiRepository.deleteById(id);
        return RequestResponse.success("Project has been deleted!");
    }


}
