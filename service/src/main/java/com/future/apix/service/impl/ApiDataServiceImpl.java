package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiMethodData;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import com.future.apix.entity.apidetail.Definition;
import com.future.apix.entity.apidetail.Parameter;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.entity.apidetail.RequestBody;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
                    methodData.setBody(oMapper.convertValue(parameter, RequestBody.class));
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

        HashMap<HttpStatus, RequestBody> responses = (HashMap<HttpStatus, RequestBody>) data.get("responses");
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
    private HashMap<HttpMethod, ApiMethodData> getLinkData(HashMap<String, Object> data){
        Iterator iterator = data.entrySet().iterator();
        HashMap<HttpMethod, ApiMethodData> result = new HashMap<>();
        while(iterator.hasNext()) {
            Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
            result.put(
                HttpMethod.valueOf(pair.getKey().toUpperCase()),
                getApiMethodData((HashMap<String, Object>) pair.getValue())
            );
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

            /* Copy to Paths Operation */
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
            /* End of Copy to Paths Operation**/

            HashMap<String,Object> definitionsJson = (HashMap<String, Object>) json.get("definitions");
            HashMap<String, Definition> definitions = project.getDefinitions();
            iterator = definitionsJson.entrySet().iterator();

            while(iterator.hasNext()){
                Map.Entry<String,Object> pair = (Map.Entry) iterator.next();
                definitions.put(pair.getKey(), oMapper.convertValue(pair.getValue(),Definition.class));
            }

            apiRepository.save(project);

            return RequestResponse.success("Data Imported");

        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidRequestException("Failed to import data : "+e.getMessage());
        }

    }


}