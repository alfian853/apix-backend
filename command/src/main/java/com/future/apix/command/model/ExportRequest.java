package com.future.apix.command.model;

import com.future.apix.enumerate.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExportRequest {
    private String projectId;
    private FileFormat format;

}
