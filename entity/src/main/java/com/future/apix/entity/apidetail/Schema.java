package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema implements Mappable {

    String type;

    String description;

    String name,in;

    String collectionFormat;

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

    @JsonIgnore
    public HashMap<String,Schema> getPropertiesLazily(){
        return (properties == null)?properties = new HashMap<>() : properties;
    }


//    Object additionalProperties;

    /** if datatype = Array
     *      array of string = {type:String,pattern:"[A-Z]+"}
     *      array of int = {type:Integer,format : Int32}
     *
     *      if items = {} still Valid
     * **/
    Schema items;

    public Schema getItems() {
        if(this.items != null){
            if(this.items.getType() != null || this.items.ref != null) return this.items;
            else{
                items.setType("object");
                return items;
            }
        }
        return null;
    }

    /** if datatype = {String,Integer,Number,...}
     * we can define the example
     * */
    String example;

    /**if datatype = String**/
    String pattern;

    /**if datatype = {Integer,Number}**/
    String format;

    /**if datatype = {File}**/
    String extension;

    /** if enum in List **/
    @Field("enum")
    @JsonProperty("enum")
    List<String> enums;

    Xml xml;

    @Field("default")
    @JsonProperty("default")
    Object defaults;

    Integer maximum, minimum, maxLength, minLength, maxItems, minItems, multipleOf;
    Boolean exclusiveMaximum, exclusiveMinimum, uniqueItems;
}
