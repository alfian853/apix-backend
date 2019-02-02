package com.future.apix.entity.apidetail;

import java.io.Serializable;

public class BodyData implements Serializable {

    String name;

    DataType type;

    //examples value
    /*isi object value [
    *   primitive data type : {int,string,date} ||
    *   object : {BodyData.class,CustomDataType}
    * ]
    *
    * support body untuk : [
    *   application/json,
    *   multipart/form-data,
    *   application/x-www-form-urlencoded
    *   ]
    * */
    Object value;

    boolean required;

    String description;

}
