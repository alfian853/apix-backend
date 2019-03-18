package com.future.apix.util.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.apidetail.OperationDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ApiProjectConverter {

    private static ObjectMapper mapper = new ObjectMapper();

    private static ApiProjectConverter converter;

    public synchronized static ApiProjectConverter getInstance(){
        if(converter == null){
            converter = new ApiProjectConverter();
        }
        return converter;
    }

    public LinkedHashMap<String,Object> convertToOasSwagger2(ApiProject project){
        LinkedHashMap<String, Object> swaggerOas2 = new LinkedHashMap<>();

        swaggerOas2.put("swagger","2.0");
        HashMap<String,Object> info = mapper.convertValue(project.getInfo(),HashMap.class);
        info.remove("_signature");
        swaggerOas2.put("info",info);
        swaggerOas2.put("host",project.getHost());
        swaggerOas2.put("basePath",project.getBasePath());
        swaggerOas2.put("schema",project.getSchemes());
        LinkedHashMap<String,Object> paths = new LinkedHashMap<>();
        swaggerOas2.put("paths",paths);
        swaggerOas2.put("securityDefinitions",project.getSecurityDefinitions());
        swaggerOas2.put("definitions",project.getDefinitions());

        project.getSections().forEach((sectionName,apiSection) -> {

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
                    methodDataMap.put("operationId",methodData.getOperationId());
                    methodDataMap.put("deprecated",methodData.getDeprecated());
                    methodDataMap.put("consumes",methodData.getConsumes());
                    methodDataMap.put("produces",methodData.getProduces());
                    methodDataMap.put("tags", Collections.singletonList(sectionName));
                    LinkedList<Object> parameters = new LinkedList<>();
                    int paramId = 0;
                    methodDataMap.put("parameters",parameters);

                    OperationDetail body = methodData.getRequest();

                    //push body to parameters
                    if(!body.getIn().equals("")){
                        LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                        param.put("in",body.getIn());
                        param.put("name",body.getName());
                        param.put("required",body.isRequired());
                        param.put("schema",body.getSchemaLazily());
                        param.put("type",body.getType());
                        param.put("description",body.getDescription());
                        parameters.add(param);
                    }

                    //push queryParams to parameters
                    body.getQueryParamsLazily().forEach((queryName, query) -> {
                        LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                        param.put("name",queryName);
                        param.put("in","query");
                        LinkedHashMap<String, Object> queryMap = mapper.convertValue(query,LinkedHashMap.class);
                        queryMap.forEach(param::put);
                        parameters.add(param);
                    });

                    //push header to parameters
                    body.getHeadersLazily().forEach((headerName, header) -> {
                        LinkedHashMap<String,Object> param = new LinkedHashMap<>();
                        param.put("name",headerName);
                        param.put("in","header");
                        LinkedHashMap<String, Object> headerMap = mapper.convertValue(header,LinkedHashMap.class);
                        headerMap.forEach(param::put);
                        parameters.add(param);
                    });


                    LinkedHashMap<String,Object> responses = new LinkedHashMap<>();
                    methodDataMap.put("responses",responses);

                    //push responses
                    methodData.getResponses().forEach((httpCode,response)->{
                        LinkedHashMap<String,Object> responseMap = mapper.convertValue(response,LinkedHashMap.class);
                        responses.put(httpCode,response);
                    });

                });//close method

                pathDataMap.put("parameters",variables);

                paths.put(pathName,pathDataMap);

            });//close path
        });//close section

        return swaggerOas2;
    }
}
