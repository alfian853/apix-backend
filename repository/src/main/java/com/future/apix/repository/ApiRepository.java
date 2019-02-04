package com.future.apix.repository;

import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ApiRepository extends MongoRepository<ApiProject, String>{


}
