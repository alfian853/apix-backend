package com.future.apix.response;

import lombok.Data;

@Data
public class RequestResponse {
    Boolean success = false;
    String message="";

    public void setStatusToFailed(){
        success = false;
    }
    public void setStatusToSuccess(){
        success = true;
    }
    public boolean isSuccess(){
        return this.success;
    }

    public static RequestResponse success(){
        RequestResponse response = new RequestResponse();
        response.setStatusToSuccess();
        return response;
    }

    public static RequestResponse success(String message){
        RequestResponse response = success();
        response.setMessage(message);
        return response;
    }

    public static RequestResponse failed(){
        RequestResponse response = new RequestResponse();
        response.setStatusToFailed();
        return response;
    }

    public static RequestResponse failed(String message){
        RequestResponse response = failed();
        response.setMessage(message);
        return response;
    }

}
