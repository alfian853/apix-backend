package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DownloadResponse extends RequestResponse {

    @JsonProperty("file_url")
    String fileUrl;

    public static DownloadResponse builder(){
        return new DownloadResponse();
    }

    public DownloadResponse fileUrl(String fileUrl){
        this.fileUrl = fileUrl;
        return this;
    }
}
