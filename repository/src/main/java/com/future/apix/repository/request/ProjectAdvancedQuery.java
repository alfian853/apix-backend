package com.future.apix.repository.request;

import com.future.apix.repository.enums.ProjectField;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class ProjectAdvancedQuery {
    int page;
    int size;
    ProjectField sortBy;
    Sort.Direction direction;
    String search;
}
