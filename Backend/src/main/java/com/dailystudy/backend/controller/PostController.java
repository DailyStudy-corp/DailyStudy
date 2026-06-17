package com.dailystudy.backend.controller;

import com.dailystudy.backend.model.Atividade;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> postar(@RequestBody Post post, Authentication authentication) {

        String emailUsuarioLogado = authentication.getName();

        String nomePost = emailUsuarioLogado.split("@")[0];

        Post novoPost = postService.criarPost(post, emailUsuarioLogado, nomePost);

        return ResponseEntity.ok(novoPost);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Atividade>> obterFeed(){
        List<Atividade> feed = postService.listarFeed();
        return ResponseEntity.ok(feed);
    }
}
