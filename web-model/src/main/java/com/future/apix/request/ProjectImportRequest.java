package com.future.apix.request;

import com.future.apix.entity.Team;
import com.future.apix.enumerate.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectImportRequest {
    Team team;
    MultipartFile file;
    FileFormat format;
}
