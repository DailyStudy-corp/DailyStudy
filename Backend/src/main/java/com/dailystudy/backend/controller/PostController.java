package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.service.CurtidaService;
import com.dailystudy.backend.service.PostService;
import com.dailystudy.backend.util.MediaValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Post> postar(@Valid @RequestBody PostCreateDTO dto, @AuthenticationPrincipal Usuario usuarioLogado) {

        if (!MediaValidator.isSafeImage(dto.mediaUrl())){
            return ResponseEntity.badRequest().build();
        }

        Post novoPost = postService.criarPost(dto, usuarioLogado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(novoPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> editarPost(@Valid @PathVariable String id, @RequestBody EditarPostDTO dto, @AuthenticationPrincipal Usuario usuarioLogado) {

        if (!MediaValidator.isSafeImage(dto.mediaUrl())){
            return ResponseEntity.badRequest().build();
        }

        Post post = postService.editarPost(id, dto, usuarioLogado.getId());

        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPost(@PathVariable String id, @AuthenticationPrincipal Usuario usuarioLogado) {

        postService.deletarPost(id, usuarioLogado.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PostFeedDTO>> listarPosts() {
        return ResponseEntity.ok(postService.listarFeed());
    }


    @GetMapping("/mine")
    public ResponseEntity<List<PostFeedDTO>> listarFeedAutor(@AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(postService.listarFeedAutor(usuarioLogado.getId()));
    }

    @PostMapping("/{id}/curtida")
    public ResponseEntity<CurtidaDTO> curtir (@PathVariable String id, @AuthenticationPrincipal Usuario usuarioLogado){
         return ResponseEntity.ok(curtidaService.toggleCurtida(id, usuarioLogado.getId()));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Post> comentar(@Valid @PathVariable String id, @RequestBody ComentarioDTO dto, @AuthenticationPrincipal Usuario usuarioLogado){
        Post comentario = postService.criarComentario(id, dto, usuarioLogado.getId());

        return ResponseEntity.ok(comentario);
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<PostFeedDTO>> listarComentarios(@PathVariable String id){
        return ResponseEntity.ok(postService.listarComentarios(id));
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<PostDetalhesDTO> buscarDetalhes(@PathVariable String id) {
        PostDetalhesDTO detalhes = postService.buscarDetalhes(id);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping
    public ResponseEntity<FeedPageDTO> listarPosts(@RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") @Max(50) int limit) {
        return ResponseEntity.ok(postService.listarFeedCursor(cursor, limit));
    }
}