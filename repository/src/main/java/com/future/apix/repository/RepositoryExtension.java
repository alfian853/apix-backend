package com.future.apix.repository;

import com.future.apix.repository.enums.MongoEntityField;
import com.future.apix.repository.request.ProjectAdvancedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

public interface RepositoryExtension<ENTITY> {

    MongoTemplate getMongoTemplate();
    Class<ENTITY> getEntityClass();
    List<? extends MongoEntityField> getFieldList();

    default Page<ENTITY> findByQuery(ProjectAdvancedQuery requestQuery, Criteria...additionalCriteria) {
        Pageable pageable = PageRequest.of(requestQuery.getPage(), requestQuery.getSize());
        Query query = new Query();

        List<Criteria> andCriteria = new ArrayList<>();

        getFieldList().forEach(field -> {
            andCriteria.add(Criteria.where(field.getMongoFieldValue())
                    .regex(requestQuery.getSearch(),"i"));
        });

        Criteria orCriteria = new Criteria().orOperator(andCriteria.toArray(new Criteria[0]));

        if(additionalCriteria.length > 0){
            int len = additionalCriteria.length;
            for(int i=0; i < len ; ++i){
                orCriteria.andOperator(additionalCriteria[i]);
            }
        }

        query.addCriteria(orCriteria);

        Sort sort = new Sort(requestQuery.getDirection(), requestQuery.getSortBy().getMongoFieldValue());

        query.with(pageable);
        query.with(sort);

        List<ENTITY> list = this.getMongoTemplate().find(query, this.getEntityClass());
        System.out.println(list);
        return PageableExecutionUtils.getPage(list, pageable,
            ()-> getMongoTemplate().count(query, this.getEntityClass()));
    }
}
