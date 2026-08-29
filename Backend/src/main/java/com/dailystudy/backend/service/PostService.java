package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.PostDetalhesDTO;
import com.dailystudy.backend.dto.ComentarioDTO;
import com.dailystudy.backend.dto.EditarPostDTO;
import com.dailystudy.backend.dto.PostCreateDTO;
import com.dailystudy.backend.dto.PostFeedDTO;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.*;
import jakarta.validation.Valid;
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
    private final CurtidaRepository curtidaRepository;

    public Post criarPost(PostCreateDTO dto, String username) {
        Post post = new Post();
        post.setId(null);
        post.setContent(dto.content());
        post.setMediaUrl(dto.mediaUrl());
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

    // ALT 1  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public List<PostFeedDTO> listarFeed(String usuarioLogadoUsername) {
        List<Post> posts = postRepository.findByComentPostIdIsNullOrderByDataCriacaoDesc();

        return mapearFeedDTO(posts, usuarioLogadoUsername);
    }

    // ALT 2  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public List<PostFeedDTO> listarFeedAutor(String username, String usuarioLogadoUsername){
    List<Post> posts = postRepository.findByAutorIdOrderByDataCriacaoDesc(username);

    return mapearFeedDTO(posts, usuarioLogadoUsername);
    }

    public Post criarComentario(String comentPostId, ComentarioDTO dto, String username){
    postRepository.findById(comentPostId)
            .orElseThrow(() -> new RuntimeException("Post não encontrado"));

    Post comentario = new Post();
    comentario.setContent(dto.content());
    comentario.setMediaUrl(dto.mediaUrl());
    comentario.setDataCriacao(LocalDateTime.now());
    comentario.setComentPostId(comentPostId);

    // ALTERAÇÃO 2 -  Adicionado setAutorId — estava faltando,
    // causando autor nulo nos comentários e exibindo "Usuario removido" na tela.
    comentario.setAutorId(username);

    return postRepository.save(comentario);
    }

    // ALT 3  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public List<PostFeedDTO> listarComentarios(String comentPostId, String usuarioLogadoUsername){
        List<Post> comentarios = postRepository.findByComentPostIdOrderByDataCriacaoDesc(comentPostId);

        return mapearFeedDTO(comentarios, usuarioLogadoUsername);
    }

    // ALT 4  - Método privado refatorado para aceitar usuarioLogadoUsername
    private List<PostFeedDTO> mapearFeedDTO(List<Post> posts, String usuarioLogadoUsername){
        return posts.stream().map(post -> {
            Usuario autor = usuarioRepository.findByUsername(post.getAutorId()).orElse(null);
            String autorNome = autor != null ? autor.getUsername() : "Usuario removido";
            String autorFoto = autor != null ? autor.getImg_perfil() : null;

            long totalCurtidas = curtidaRepository.countByPostId(post.getId());
            long totalComentarios = postRepository.countByComentPostId(post.getId());
            
            // ALT 4  - Verificar se o usuário logado curtiu este post
            boolean curtido = usuarioLogadoUsername != null &&
                    curtidaRepository.findByPostIdAndAutorId(post.getId(), usuarioLogadoUsername).isPresent();

            return new PostFeedDTO(post, autorNome, autorFoto, totalCurtidas, totalComentarios, curtido);
        }).toList();
    }

    // ALT 5  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public PostDetalhesDTO buscarDetalhes(String id, String usuarioLogadoUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        PostFeedDTO postDTO = mapearFeedDTO(List.of(post), usuarioLogadoUsername).get(0);
        List<PostFeedDTO> comentariosDTO = mapearFeedDTO(
            postRepository.findByComentPostIdOrderByDataCriacaoDesc(id),
            usuarioLogadoUsername
        );

        return new PostDetalhesDTO(postDTO, comentariosDTO);
    }
}