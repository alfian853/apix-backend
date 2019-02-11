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
import com.future.apix.util.jsonquery.JsonQueryExecutor;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.ParameterValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    ApiRepository apiRepository;


    private ObjectMapper oMapper = new ObjectMapper();
    private JsonQueryExecutor queryExecutor = new JsonQueryExecutor();

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
                HashMap<String, Object> parameter = (HashMap<String, Object>) paramObj;
                if(parameter.get("in").equals("query")){
                    methodData.getQueryParams().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Parameter.class)
                    );
                }
                else if(parameter.get("in").equals("header")){
                    methodData.getHeaders().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Parameter.class)
                    );
                }
                else if(parameter.get("in").equals("body")){
                    RequestBody body = oMapper.convertValue(parameter, RequestBody.class);
                    methodData.setBody(body);
                }
                else if(parameter.get("in").equals("path")){
                    methodData.getPathVariables().put(
                            (String) parameter.get("name"),oMapper.convertValue(parameter, Parameter.class)
                    );
                }
                else if(parameter.get("in").equals("formData")){
                    methodData.setBody(oMapper.convertValue(parameter, RequestBody.class));
                }
                else{
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
    private HashMap<String, ApiMethodData> getLinkData(HashMap<String, Object> data){
        Iterator iterator = data.entrySet().iterator();
        HashMap<String, ApiMethodData> result = new HashMap<>();
        while(iterator.hasNext()) {
            Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
            ApiMethodData methodData = getApiMethodData((HashMap<String, Object>) pair.getValue());

            HttpMethod method = HttpMethod.valueOf(pair.getKey().toUpperCase());
            // validating content
            if(
                    ParameterValidator.isValid(methodData.getHeaders()) &&
                            ParameterValidator.isValid(methodData.getPathVariables()) &&
                            ParameterValidator.isValid(methodData.getQueryParams()) &&
                            ((method == HttpMethod.GET) || (methodData.getBody() == null ||
                            BodyValidator.isValid(methodData.getBody())))

            ){
                result.put(pair.getKey().toLowerCase(),methodData);
            }
            else{
                throw new InvalidRequestException("Json OAS is not valid!");
            }
        }
        return result;
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
//            project.setSwagger((String) json.get("swagger"));
//            project.setOpenapi((String) json.get("openapi"));
            project.setSchemes((List<String>) json.get("schemes"));
            project.setExternalDocs(oMapper.convertValue(json.get("externalDocs"), Contact.class));

            /* Copy Paths Operation */
            //     link, listOfMethod
            HashMap<String,Object> paths = (HashMap<String, Object>) json.get("paths");
            Iterator iterator = paths.entrySet().iterator();
            //     section,<link, HashMapOfMethod>
            HashMap<String, ApiSection> sections = project.getSections();

            while(iterator.hasNext()){
                //        link,ListOfMethod
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                String section = pair.getKey().split("/",3)[1];
                if(!sections.containsKey(section)){
                    sections.put(section, new ApiSection());
                }
                //           sectionName             link
                sections.get(section).getPaths().put(pair.getKey(), getLinkData((HashMap<String, Object>) pair.getValue()));


            }
            /* End of Copy Paths Operation**/

            /* Append Description of Sections from Tags */
            List<Object> tags = (List<Object>) json.get("tags");
//            HashMap<String, String> tagName = tags.stream().collect(Collectors.toMap(tags.get("name")))
            HashMap<String, Tag> tagDescription = new HashMap<>();
            for (Object tagObj : tags) {
                HashMap<String, Object> tag = (HashMap<String, Object>) tagObj;
                ApiSection section = sections.get(tag.get("name"));
                section.setTag(oMapper.convertValue(tag, Tag.class));
            }
            /* End of Append Tags */


            /* Copy Definitions Operation*/
            HashMap<String,Object> definitionsJson = (HashMap<String, Object>) json.get("definitions");
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
            HashMap<String, SecurityScheme> securityScheme = project.getSecurityDefinitions();
            iterator = securityDefinitionJson.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                SecurityScheme scheme = oMapper.convertValue(pair.getValue(), SecurityScheme.class);
                securityScheme.put(pair.getKey(), scheme);
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
