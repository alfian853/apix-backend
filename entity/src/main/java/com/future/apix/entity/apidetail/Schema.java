package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schema {

    String type;

    boolean required;

    String description;

    //untuk custom dataType, isi schema = isi custom dataType
    @JsonProperty("$ref")
    String ref;

    /** if datatype = Object
    *isi object value [
    *   primitive data type : {int,string,date} ||
    *   object : {Schema.class,CustomDataType}
    * ]
    * */
    HashMap<String,Schema> properties;


    /** if datatype = Array
     *      array of string = {type:String,pattern:"[A-Z]+"}
     *      array of int = {type:Integer,format : Int32}
     * **/
    Schema items;

    /** if datatype = {String,Integer,Number,...}
     * we can define the example
     * */
    String example;

    /**if datatype = String**/
    String pattern;

    /**if datatype = {Integer,Number}**/
    String format;

}
