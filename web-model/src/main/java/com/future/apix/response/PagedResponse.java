package com.future.apix.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedResponse<CONTENT> {

    Integer totalPages, pageSize,pageNumber, numberOfElements;
    Long totalElements, offset;
    Boolean first,last;
    List<CONTENT> contents;
}
