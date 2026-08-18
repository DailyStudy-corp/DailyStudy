package com.dailystudy.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public Map<String, Long> countGroupedByComentPostId(List<String> postIds) {
        if (postIds.isEmpty()){
            return Map.of();
        }

        Aggregation aggregacao = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("comentPostId").in(postIds)),
                Aggregation.group("comentPostId").count().as("total")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregacao, "posts", Document.class);

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        document -> document.getString("_id"),
                        document -> ((Number) document.get("total")).longValue()
                ));
    }
}
