package com.future.apix.repository;

import com.future.apix.repository.enums.MongoEntityField;
import com.future.apix.repository.request.AdvancedQuery;
import lombok.Builder;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryExtensionTest {

    private List<Sample> sampleList = new LinkedList<>();

    @Data
    @Builder
    public static class Sample {
        private String attr1;
        private String attr2;
    }

    public static enum SampleEnum implements MongoEntityField{
        ATTR1("field.attr1"),
        ATTR2("field.attr2");

        String field;
        SampleEnum(String field) {
            this.field = field;
        }

        @Override
        public String getMongoFieldValue() {
            return field;
        }

        @Override
        public List<MongoEntityField> getMongoFieldList() {
            return Arrays.asList(SampleEnum.values());
        }
    }

    @Mock
    MongoTemplate mongoTemplate;

    RepositoryExtension<Sample> repositoryExtension = new RepositoryExtension<Sample>() {
        @Override
        public MongoTemplate getMongoTemplate() {
            return mongoTemplate;
        }

        @Override
        public Class<Sample> getEntityClass() {
            return Sample.class;
        }

        @Override
        public List<? extends MongoEntityField> getFieldList() {
            return SampleEnum.ATTR1.getMongoFieldList();
        }
    };

    @Before
    public void before(){
        for(int i = 0; i < 20; i++){
            sampleList.add(
                    Sample.builder().attr1("sample "+i+"-attr1").attr2("sample "+i+"-attr2").build()
            );
        }
        when(mongoTemplate.find(any(Query.class),eq(Sample.class))).thenReturn(sampleList);
    }

    @Test
    public void findByQueryTest(){
        Page<Sample> samplePage = repositoryExtension.findByQuery(
                AdvancedQuery.builder()
                        .page(0)
                        .size(5)
                        .sortBy(SampleEnum.ATTR1)
                        .search("sample search")
                        .build(),
                Criteria.where("attr1").is("value")
        );
        Assert.assertEquals(samplePage.getContent(), sampleList);
    }


}
