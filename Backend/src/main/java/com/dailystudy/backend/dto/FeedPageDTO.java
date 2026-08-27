package com.dailystudy.backend.dto;

import java.util.List;

public record FeedPageDTO(List<PostFeedDTO> posts, String nextCursor, boolean hasMore) {
}
