package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PostRepositoryCustom {
    Map<String, Long> countGroupedByComentPostId(List<String> postIds);

    List<Post> ordenarFeedCursor(LocalDateTime cursorData, String cursorId, int limit);
}
