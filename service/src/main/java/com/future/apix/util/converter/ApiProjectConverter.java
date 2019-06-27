package com.future.apix.util.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Mappable;
import com.future.apix.entity.apidetail.OperationDetail;
import com.future.apix.entity.apidetail.Tag;
import org.springframework.util.StringUtils;

import java.util.*;

public class ApiProjectConverter {

    private static ObjectMapper mapper = new ObjectMapper();

    private static ApiProjectConverter converter;

    public synchronized static ApiProjectConverter getInstance(){
        if(converter == null){
            converter = new ApiProjectConverter();
        }
        return converter;
    }


    private void replaceRefWithId(HashMap<String, Object> data,HashMap<String, String> idToName){
        for(Object obj : data.entrySet()){
            Map.Entry<String, Object> pair = (Map.Entry<String, Object>) obj;
            if(pair.getKey() == null)continue;
            if(pair.getKey().equals("$ref") || pair.getKey().equals("ref")){
                String ref = (String) pair.getValue();
                ref = ref.split("/",3)[2];
                pair.setValue("#/definitions/"+idToName.get(ref));
            }
            else if(pair.getValue() instanceof HashMap){
                replaceRefWithId((HashMap<String, Object>) pair.getValue(), idToName);
            }
            else if(pair.getValue() instanceof Mappable){
                HashMap<String, Object> tmp = mapper.convertValue(pair.getValue(), HashMap.class);
                this.replaceRefWithId(tmp, idToName);
                pair.setValue(
                        mapper.convertValue(tmp, pair.getValue().getClass())
                );
            }
        }
    }


    public LinkedHashMap<String,Object> convertToOasSwagger2(ApiProject project){
        LinkedHashMap<String, Object> swaggerOas2 = new LinkedHashMap<>();

        swaggerOas2.put("swagger","2.0");
        HashMap<String,Object> info = mapper.convertValue(project.getInfo(),HashMap.class);
        info.remove("_signature");
        swaggerOas2.put("info",info);
        swaggerOas2.put("host",project.getHost());
        swaggerOas2.put("basePath",project.getBasePath());
        swaggerOas2.put("externalDocs", project.getExternalDocs());
        swaggerOas2.put("schema",project.getSchema());
        List<Tag> tags = new LinkedList<>();
        swaggerOas2.put("tags", tags);
        LinkedHashMap<String,Object> paths = new LinkedHashMap<>();
        swaggerOas2.put("paths",paths);

        HashMap<String, Object> definitions = new HashMap<>();
        HashMap<String, String> definitionIdToName = new HashMap<>();
        project.getDefinitions().forEach((key, value) -> {
            value.setSignature(null);
            definitions.put(value.getName(), value);
            definitionIdToName.put(key, value.getName());
            value.setName(null);
        });
        this.replaceRefWithId(definitions, definitionIdToName);

        swaggerOas2.put("definitions", definitions);
        swaggerOas2.put("securityDefinitions",project.getSecurityDefinitions());


        project.getSections().forEach((sectionName,apiSection) -> {
            apiSection.getInfo().setSignature(null);
            tags.add(apiSection.getInfo());
            apiSection.getPaths().forEach((pathName, pathData) -> {

                LinkedHashMap<String, Object> pathDataMap = new LinkedHashMap<>();

                LinkedList<Object> variables = new LinkedList<>();

                pathData.getPathVariables().forEach((varName,varData) -> {
                    variables.push(varData);
                });

                pathData.getMethods().forEach((httpMethod,methodData) -> {

                    LinkedHashMap<String, Object> methodDataMap = new LinkedHashMap<>();

                    pathDataMap.put(httpMethod, methodDataMap);

                    methodDataMap.put("summary",methodData.getSummary());
                    methodDataMap.put("description",methodData.getDescription());
                    if(methodData.getOperationId() != null){
                        methodDataMap.put("operationId",methodData.getOperationId());
                    }
                    if(methodData.getDeprecated() != null){
                        methodDataMap.put("deprecated",methodData.getDeprecated());
                    }
                    if(methodData.getConsumes() != null){
                        methodDataMap.put("consumes",methodData.getConsumes());
                    }
                    methodDataMap.put("produces",methodData.getProduces());
                    methodDataMap.put("tags", Collections.singletonList(sectionName));
                    LinkedList<Object> parameters = new LinkedList<>();

                    OperationDetail body = methodData.getRequest();

                    //push body to parameters
                    if(!StringUtils.isEmpty(body.getIn())){
                        if(body.getIn().equals("formData")){
                            body.getSchema().getProperties().values().forEach(schema -> {
                                schema.setIn("formData");
                                parameters.add(schema);
                            });
                        }
                        else{
                            LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                            param.put("in",body.getIn());
                            param.put("name",body.getName());
                            param.put("required",body.isRequired());
                            param.put("schema",body.getSchema());
                            param.put("description",body.getDescription());
                            this.replaceRefWithId(param, definitionIdToName);
                            parameters.add(param);
                        }
                    }

                    //push queryParams to parameters
                    body.getQueryParamsLazily().forEach((queryName, query) -> {
                        LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                        param.put("name",queryName);
                        param.put("in","query");
                        LinkedHashMap<String, Object> queryMap = mapper.convertValue(query,LinkedHashMap.class);
                        queryMap.forEach(param::put);
                        this.replaceRefWithId(param, definitionIdToName);
                        parameters.add(param);
                    });

                    //push header to parameters
                    body.getHeadersLazily().forEach((headerName, header) -> {
                        LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                        param.put("name",headerName);
                        param.put("in","header");
                        LinkedHashMap<String, Object> headerMap = mapper.convertValue(header,LinkedHashMap.class);
                        headerMap.forEach(param::put);
                        this.replaceRefWithId(param, definitionIdToName);
                        parameters.add(param);
                    });

                    if(parameters.size() > 0){
                        methodDataMap.put("parameters",parameters);
                    }
                    LinkedHashMap<String,Object> responses = new LinkedHashMap<>();
                    methodDataMap.put("responses",responses);

                    //push responses
                    methodData.getResponses().forEach((httpCode,response)->{
                        LinkedHashMap<String,Object> responseMap = mapper.convertValue(response,LinkedHashMap.class);
//                        this.replaceRefWithId(responseMap, definitionIdToName);
                        responses.put(
                                httpCode,
                                mapper.convertValue(responseMap, OperationDetail.class)
                        );
                    });

                });//close method

                if(variables.size() > 0){
                    pathDataMap.put("parameters",variables);
                }
                this.replaceRefWithId(pathDataMap, definitionIdToName);
                paths.put(pathName,pathDataMap);

            });//close path
        });//close section

        return swaggerOas2;
    }
}
