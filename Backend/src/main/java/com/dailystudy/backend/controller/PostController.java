package com.dailystudy.backend.controller;

import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> postar(@RequestBody Post post) {

        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        Post novoPost = postService.criarPost(post, emailUsuarioLogado);

        return ResponseEntity.ok(novoPost);
    }
}
