package com.future.apix.repository.enums;

import java.util.List;

public interface MongoEntityField {
    String getMongoFieldValue();
    List<MongoEntityField> getMongoFieldList();
}
