package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExportResponse extends RequestResponse {

    @JsonProperty("file_url")
    String fileUrl;

    public static ExportResponse builder(){
        return new ExportResponse();
    }

    public ExportResponse fileUrl(String fileUrl){
        this.fileUrl = fileUrl;
        return this;
    }
}
