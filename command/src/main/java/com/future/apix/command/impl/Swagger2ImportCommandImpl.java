package com.future.apix.command.impl;

import com.future.apix.command.Swagger2ImportCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.apidetail.*;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class Swagger2ImportCommandImpl implements Swagger2ImportCommand {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private SwaggerToApixOasConverter converter;

    public void setMapper(ObjectMapper objectMapper){
        this.oMapper = objectMapper;
    }

    @Override
    public ApiProject execute(ProjectImportRequest request) {

        HashMap<String,Object> json = null;
        MultipartFile file = request.getFile();
        Team team = setTeam(request.getIsNewTeam(), request.getTeam());
        if (Optional.of(file).isPresent()) System.out.println("File is exists!");

        try {
            json = oMapper.readValue(file.getInputStream(), HashMap.class);
            ApiProject project = converter.convert(json);
            project.setProjectOwner(team);
            project.getTeams().add(team.getName());
            apiRepository.save(project);
            return project;

        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidRequestException("Failed to import data : "+e.getMessage());
        }

    }

    private Team setTeam(Boolean isNewTeam, String teamName){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isNewTeam) { // if team is exists
            Team team = Optional.ofNullable(teamRepository.findByName(teamName))
                .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
            return team;
        }
        else { // if team does not exists, and create new team with authenticated user as the owner
            Team newTeam = new Team();
            UserProfileResponse
                convertUser = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
            User user = Optional.ofNullable(userRepository.findByUsername(convertUser.getUsername()))
                .orElseThrow(() -> new DataNotFoundException("User is not exists!"));
            newTeam.setName(teamName);
            newTeam.setCreator(user.getUsername());
            newTeam.getMembers().add(new Member(user.getUsername(), true));
            newTeam.setAccess("private");
            newTeam = teamRepository.save(newTeam);

            user.getTeams().add(newTeam.getName());
            userRepository.save(user);

            return newTeam;
        }
    }
}
