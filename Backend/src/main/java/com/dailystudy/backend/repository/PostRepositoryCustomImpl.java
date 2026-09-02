package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
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

    public List<Post> ordenarFeedCursor(LocalDateTime cursorData, String cursorId, int limit){
        Criteria filtro = Criteria.where("comentPostId").is(null);

        if (cursorData != null && cursorId != null) {
            Criteria paginacao = new Criteria().orOperator(
                    Criteria.where("dataCriacao").lt(cursorData),
                    new Criteria().andOperator(
                            Criteria.where("dataCriacao").is(cursorData),
                            Criteria.where("_id").lt(cursorId)
                    )
            );
            filtro = new Criteria().andOperator(filtro, paginacao);
        }

        Query query = new Query(filtro)
                .with(Sort.by(Sort.Order.desc("dataCriacao"), Sort.Order.desc("_id")))
                .limit(limit + 1);

        return mongoTemplate.find(query, Post.class);
    }
}
