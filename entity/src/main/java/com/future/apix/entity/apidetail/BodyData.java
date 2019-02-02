package com.future.apix.entity.apidetail;

import java.io.Serializable;
import java.util.HashMap;

public class BodyData implements Serializable {

    String name;

    String type;

    //examples value
    /*isi object values [
    *   primitive data type : {int,string,date} ||
    *   object : {BodyData.class}
    * ]
    * */
    HashMap<String, Object> values;

    boolean required;

    String description;

}
