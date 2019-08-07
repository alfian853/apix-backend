package com.future.apix.repository;

import com.future.apix.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);

    List<User> findByTeams(String teamName);

    @Query(value = "{}", fields = "{password: 0, authorities: 0}")
    List<User> findAll();
}
