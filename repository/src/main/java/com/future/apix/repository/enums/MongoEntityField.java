package com.future.apix.repository.enums;

import java.util.List;

public interface MongoEntityField {
    MongoEntityField getMongoField(String field);
    String getMongoFieldValue();
    List<MongoEntityField> getMongoFieldList();
}
