package com.future.apix.util;

import java.util.HashMap;

public class ActionExecutor {

    private static ActionExecutor factory;

    public synchronized static ActionExecutor getInstance(){
        if(factory == null){
            factory = new ActionExecutor();
        }
        return factory;
    }

    public void execute(HashMap<String,Object> target, HashMap<String,Object> query) {

        String actionString = (String) query.get("action");

        switch (actionString){
            case "put":
                doInsert(target, query);
                break;
            case "rename":
                doRename(target, query);
                break;
            case "delete":
                doDelete(target, query);
                break;
        }

    }

    private void doRename(HashMap<String, Object> target, HashMap<String, Object> query) {
        String key = (String) query.get("key");
        Object temp = target.get(key);
        target.remove(key);
        target.put((String) query.get("newKey"), temp);
        System.out.println("rename success : "+query.toString());
    }

    private void doInsert(HashMap<String, Object> target, HashMap<String, Object> query) {
        target.put((String) query.get("key"), query.get("value"));
        System.out.println("insert success : "+query.toString());
    }

    private void doDelete(HashMap<String, Object> target, HashMap<String, Object> query) {
        target.remove(query.get("key"));
        System.out.println("delete success : "+query.toString());
    }


}
