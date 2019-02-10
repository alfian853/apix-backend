package com.future.apix.entity.apidetail;

import lombok.Data;

import java.util.HashMap;

@Data
public class SecurityDefinition {
    HashMap<String, Auth> auths = new HashMap<>();

    // masih belum yakin (tapi karena ada type, name, in)
    RequestBody api_key;
}
