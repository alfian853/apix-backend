package com.future.apix.repository.impl;

import com.future.apix.entity.Team;
import com.future.apix.repository.TeamRepositoryExtension;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public class TeamRepositoryExtensionImpl implements TeamRepositoryExtension {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Team removeMemberFromTeam(String teamName, String member) {
        Query query = new Query(Criteria.where("name").is(teamName));
        Update update = new Update();
        update.pull("members",new BasicDBObject().append("username",member));

        return mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), Team.class);
//        return true;
    }
}
