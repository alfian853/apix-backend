package com.future.apix.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectImportRequest {
    String team;
    Boolean isNewTeam; // true if create new team
    MultipartFile file;
}
