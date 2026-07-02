package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.EditarPostDTO;
import com.dailystudy.backend.dto.PostFeedDTO;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final CurtidaRepository curtidaRepository;

    public Post criarPost(Post post, String username) {
        post.setAutorId(username);
        post.setDataCriacao(LocalDateTime.now());

        return postRepository.save(post);
    }

    public Post editarPost(String id, EditarPostDTO dto, String username){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if(!post.getAutorId().equals(username)){
            throw new RuntimeException("Não pode editar esse post");
        }

        post.setContent(dto.content());
        post.setMediaUrl(dto.mediaUrl());
        post.setDataEdicao(LocalDateTime.now());

        return postRepository.save(post);
    }

    public void deletarPost(String id, String username){
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (!post.getAutorId().equals(username)){
           throw new RuntimeException("Não pode deletar esse post");
        }

        postRepository.deleteById(id);
    }

    public List<PostFeedDTO> listarFeed() {
        List<Post> posts = postRepository.findAllByOrderByDataCriacaoDesc();
        return posts.stream().map(post -> {
            Usuario autor = usuarioRepository.findByUsername(post.getAutorId())
                    .orElse(null);
            String autorNome = autor != null ? autor.getUsername() : "Usuario removido";
            String autorFoto = autor != null ? autor.getImg_perfil() : null;

            long totalCurtidas = curtidaRepository.countByPostId(post.getId());
            long totalComentarios = comentarioRepository.countByPostId(post.getId());

            return new PostFeedDTO(post, autorNome, autorFoto, totalCurtidas, totalComentarios);
        }).toList();
    }
}
