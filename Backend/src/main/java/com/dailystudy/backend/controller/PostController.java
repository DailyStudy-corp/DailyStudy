package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.EditarPostDTO;
import com.dailystudy.backend.dto.PostFeedDTO;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PutMapping("/{id}")
    public ResponseEntity<Post> editarPost(@PathVariable String id, @RequestBody EditarPostDTO dto, Authentication authentication){

        String username = authentication.getName();
        Post post = postService.editarPost(id, dto, username);

        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPost(@PathVariable String id, Authentication authentication){

        String username = authentication.getName();
        postService.deletarPost(id, username);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostFeedDTO>> listarPosts(){
        return ResponseEntity.ok(postService.listarFeed());
    }
}
