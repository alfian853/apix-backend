package com.future.apix.repository.request;

import com.future.apix.repository.enums.MongoEntityField;
import com.future.apix.repository.enums.ProjectField;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class AdvancedQuery {
    int page;
    int size;
    MongoEntityField sortBy;
    Sort.Direction direction = Sort.Direction.ASC;
    String search = "";
}
