package com.dailystudy.backend.controller;

<<<<<<< HEAD
import com.dailystudy.backend.dto.ComentarioDTO;
import com.dailystudy.backend.dto.CurtidaDTO;
import com.dailystudy.backend.dto.EditarPostDTO;
import com.dailystudy.backend.dto.PostFeedDTO;
import com.dailystudy.backend.dto.PostDetalhesDTO;  // Gui
=======
import com.dailystudy.backend.dto.*;
>>>>>>> c904829c7f783df0831b03152ba2eaa88abbe6b4
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
    public ResponseEntity<List<PostFeedDTO>> listarPosts() {
        return ResponseEntity.ok(postService.listarFeed());
    }


    @GetMapping("/mine")
    public ResponseEntity<List<PostFeedDTO>> listarFeedAutor(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(postService.listarFeedAutor(username));
    }

    @PostMapping("/{id}/curtida")
    public ResponseEntity<CurtidaDTO> curtir (@PathVariable String id, Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(curtidaService.statusCurtida(id, username));
    }

     @PostMapping("/{id}/curtida")
    public ResponseEntity<CurtidaDTO> curtir (@PathVariable String id, Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(curtidaService.toggleCurtida(id, username));
    }

    @GetMapping("/{id}/curtida")
    public ResponseEntity<CurtidaDTO> statusCurtida(@PathVariable String id, Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(curtidaService.statusCurtida(id, username));
    }
     
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Post> comentar(@PathVariable String id, @RequestBody ComentarioDTO dto, Authentication authentication){
        String username = authentication.getName();
        Post comentario = postService.criarComentario(id, dto, username);

        return ResponseEntity.ok(comentario);
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<PostFeedDTO>> listarComentarios(@PathVariable String id){
        return ResponseEntity.ok(postService.listarComentarios(id));
    }
}