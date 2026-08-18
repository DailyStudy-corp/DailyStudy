package com.dailystudy.backend.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CurtidaRepositoryImpl implements CurtidaRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public Map<String, Long> countGroupedByPostId(List<String> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Aggregation aggregacao = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("postId").in(postIds)),
                Aggregation.group("postId").count().as("total")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregacao, "curtida", Document.class);

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        document -> document.getString("_id"),
                        document -> ((Number) document.get("total")).longValue()
                ));
    }

}
