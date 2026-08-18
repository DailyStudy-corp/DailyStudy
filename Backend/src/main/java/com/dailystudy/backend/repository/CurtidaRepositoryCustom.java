package com.dailystudy.backend.repository;

import java.util.List;
import java.util.Map;

public interface CurtidaRepositoryCustom {
    Map<String, Long> countGroupedByPostId(List<String> postsIds);
}
