package com.dailystudy.backend.service;

import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post criarPost(Post post, String emailAutor) {
        post.setAutor(emailAutor);
        post.setDataCriacao(LocalDateTime.now());
        return postRepository.save(post);
    }

    public List<Post> listarFeed() {
        return postRepository.findAllByOrderByDataCriacaoDesc();
    }
}
