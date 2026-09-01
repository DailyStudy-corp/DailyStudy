package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.service.CurtidaService;
import com.dailystudy.backend.service.PostService;
import com.dailystudy.backend.util.MediaValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CurtidaService curtidaService;

    @PostMapping
    public ResponseEntity<Post> postar(@Valid @RequestBody PostCreateDTO dto, Authentication authentication) {

        if (!MediaValidator.isSafeImage(dto.mediaUrl())){
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        Post novoPost = postService.criarPost(dto, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(novoPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> editarPost(@PathVariable String id, @RequestBody EditarPostDTO dto, Authentication authentication) {

        if (!MediaValidator.isSafeImage(dto.mediaUrl())){
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        Post post = postService.editarPost(id, dto, username);

        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPost(@PathVariable String id, Authentication authentication) {

        String username = authentication.getName();
        postService.deletarPost(id, username);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PostFeedDTO>> listarPosts(Authentication authentication) {
        // ALT 4 - Passar username do usuário logado para calcular curtido em cada post
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(postService.listarFeed(username));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PostFeedDTO>> listarPostsMeus(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(postService.listarFeedAutor(username, username));
    }

      @PostMapping("/{id}/curtida")
     public ResponseEntity<CurtidaDTO> curtir (@PathVariable String id, Authentication authentication){
    String username = authentication.getName();
    return ResponseEntity.ok(curtidaService.toggleCurtida(id, username));
}

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Post> comentar(@PathVariable String id, @RequestBody ComentarioDTO dto, Authentication authentication){
        String username = authentication.getName();
        Post comentario = postService.criarComentario(id, dto, username);

        return ResponseEntity.ok(comentario);
    }

      @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<PostFeedDTO>> listarComentarios(@PathVariable String id, Authentication authentication) {
        // ALT 5  - Passar username do usuário logado para calcular curtido nos comentários
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(postService.listarComentarios(id, username));
      }

      @GetMapping("/{id}/detalhes")
    public ResponseEntity<PostDetalhesDTO> buscarDetalhes(@PathVariable String id, Authentication authentication) {
        // ALT 6  - Passar username do usuário logado para calcular curtido no post e comentários
        String username = authentication != null ? authentication.getName() : null;
        PostDetalhesDTO detalhes = postService.buscarDetalhes(id, username);
        return ResponseEntity.ok(detalhes);
    }

 }