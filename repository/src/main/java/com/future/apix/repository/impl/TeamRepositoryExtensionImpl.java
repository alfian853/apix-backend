package com.future.apix.repository.impl;

import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.repository.TeamRepositoryExtension;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.UpdateRequest;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


public class TeamRepositoryExtensionImpl implements TeamRepositoryExtension {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UpdateResult removeMemberFromTeam(String teamName, String memberName) {
        Query query = new Query(Criteria.where("name").is(teamName));
        Update update = new Update();
        update.pull("members",new BasicDBObject().append("username",memberName));

        return mongoTemplate.updateFirst(query, update, Team.class);
    }

    @Override
    public UpdateResult inviteMemberToTeam(String teamName, String memberName, boolean isInvite) {
        Query query = new Query(Criteria.where("name").is(teamName).and("members.username").is(memberName));
        Update update = new Update();
        update.set("members.$.grant", isInvite);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Team.class);
        if(result.getMatchedCount() == 0) { // jika belum ada member
            Query query1 = new Query(Criteria.where("name").is(teamName));
            Update update1 = new Update();
            Member member = new Member(memberName, isInvite);
            update1.addToSet("members", member);
            return mongoTemplate.updateFirst(query1, update1, Team.class);
        }
        else {
            return result;
        }
    }

    @Override
    public UpdateResult removeTeamFromMember(String teamName, String memberName) {
        Query query = new Query(Criteria.where("username").is(memberName));
        Update update = new Update().pull("teams", teamName);
        return mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public UpdateResult removeTeamFromProject(String teamName, String projectId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(projectId)).and("projectOwner.name").ne(teamName));
        Update update = new Update().pull("teams", teamName);
        return mongoTemplate.updateFirst(query, update, ApiProject.class);
    }

}
