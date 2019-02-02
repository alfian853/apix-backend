package com.future.apix.entity.apidetail;

import java.io.Serializable;
import java.util.List;

public class BodyData implements Serializable {

    String type;

    //examples value
    List<Object> values;

    boolean required;

    String description;

}
