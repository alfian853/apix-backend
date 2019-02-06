package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonQueryExecutor {

    private ObjectMapper mapper = new ObjectMapper();
    private static ActionExecutor actionExecutor = ActionExecutor.getInstance();

    private boolean update(HashMap<String,Object> target,HashMap<String,Object> data){

        boolean hasUpdate = false;

        if(data.containsKey("_hasActions")){
            List<HashMap<String,Object> > actions = (List<HashMap<String, Object>>) data.get("_actions");

            for (HashMap<String,Object> action : actions) {
                actionExecutor.execute(target, action);
            }
            hasUpdate = true;
        }
        for (Object o : data.entrySet()) {
            Map.Entry<String, Object> pair = (Map.Entry) o;

            if(pair.getKey().equals("_hasActions") ||
               pair.getKey().equals("_actions")){
                continue;
            }

            if (pair.getValue() instanceof HashMap) {

                boolean hasCreatingNew = false;

                if (!target.containsKey(pair.getKey())) {
                    target.put(pair.getKey(), new HashMap<>());
                    hasCreatingNew = true;
                }

                boolean hasUpdateChild = update(
                        (HashMap<String, Object>) target.get(pair.getKey()),
                        (HashMap<String, Object>) pair.getValue());

                //if no operation in new child, then remove the child
                if (!hasUpdateChild && hasCreatingNew) {
                    target.remove(pair.getKey());
                }
                hasUpdate |= hasUpdateChild;
            }
        }

        return hasUpdate;
    }

    public <T> T  executeQuery(T target, HashMap<String, Object> query){
        HashMap<String,Object> json = mapper.convertValue(target, HashMap.class);
        update(json, query);
        return (T) mapper.convertValue(json,target.getClass());
    }

}
