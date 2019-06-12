package com.future.apix.entity.apidetail;

import com.future.apix.entity.Mappable;
import lombok.Data;

@Data
public class Github implements Mappable {
    String owner, repo, branch;
    String path;
}
