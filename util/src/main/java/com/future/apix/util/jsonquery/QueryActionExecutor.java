package com.future.apix.util.jsonquery;

import java.util.Map;

public class QueryActionExecutor {

    private static QueryActionExecutor executor;

    public synchronized static QueryActionExecutor getInstance(){
        if(executor == null){
            executor = new QueryActionExecutor();
        }
        return executor;
    }

    public void execute(Map<String,Object> target, Map<String,Object> query) {

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

    protected void doRename(Map<String, Object> target, Map<String, Object> query) {
        String key = (String) query.get("key");
        Object temp = target.get(key);
        target.remove(key);
        target.put((String) query.get("newKey"), temp);
        System.out.println("rename success : "+query.toString());
    }

    protected void doInsert(Map<String, Object> target, Map<String, Object> query) {
        target.put((String) query.get("key"), query.get("value"));
        System.out.println("insert success : "+query.toString());
    }

    protected void doDelete(Map<String, Object> target, Map<String, Object> query) {
        target.remove(query.get("key"));
        System.out.println("delete success : "+query.toString());
    }


}
