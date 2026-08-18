package com.dailystudy.backend.repository;

import java.util.List;
import java.util.Map;

public interface PostRepositoryCustom {
    Map<String, Long> countGroupedByComentPostId(List<String> postIds);
}
