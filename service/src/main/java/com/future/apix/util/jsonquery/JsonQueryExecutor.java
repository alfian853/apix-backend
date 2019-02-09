package com.future.apix.util.jsonquery;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonQueryExecutor {

    private ObjectMapper mapper = new ObjectMapper();
    private static ActionExecutor actionExecutor = ActionExecutor.getInstance();

    private boolean checkAndExecuteActions(HashMap<String,Object> target,HashMap<String,Object> data){
        if(data.containsKey("_hasActions")){
            List<HashMap<String,Object> > actions = (List<HashMap<String, Object>>) data.get("_actions");

            for (HashMap<String,Object> action : actions) {
                actionExecutor.execute(target, action);
            }
            return true;
        }
        return false;
    }

    private boolean traverseChild(HashMap<String,Object> target,HashMap<String,Object> data){
        boolean hasUpdate = false;
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

                boolean hasUpdateChild = this.update(
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

    protected boolean update(HashMap<String,Object> target,HashMap<String,Object> data){

        boolean hasUpdate = this.checkAndExecuteActions(target, data);
        hasUpdate |= this.traverseChild(target, data);

        return hasUpdate;
    }

    public <T> T  executeQuery(T target, HashMap<String, Object> query){
        HashMap<String,Object> json = mapper.convertValue(target, HashMap.class);
        update(json, query);
        return (T) mapper.convertValue(json,target.getClass());
    }

    public void executeQuery(HashMap<String,Object> target, HashMap<String, Object> query){
        update(target, query);
    }

}
