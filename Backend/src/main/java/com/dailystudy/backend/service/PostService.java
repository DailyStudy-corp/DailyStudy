package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.*;
import com.dailystudy.backend.util.CursorCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AtividadeRepository atividadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurtidaRepository curtidaRepository;

    public Post criarPost(PostCreateDTO dto, Long autorId) {
        Post post = new Post();
        post.setId(null);
        post.setContent(dto.content());
        post.setMediaUrl(dto.mediaUrl());
        post.setAutorId(autorId);
        post.setDataCriacao(LocalDateTime.now());

        return postRepository.save(post);
    }

    public Post editarPost(String id, EditarPostDTO dto, Long autorId){

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if(!post.getAutorId().equals(autorId)){
            throw new RuntimeException("Não pode editar esse post");
        }

        post.setContent(dto.content());
        post.setMediaUrl(dto.mediaUrl());
        post.setDataEdicao(LocalDateTime.now());

        return postRepository.save(post);
    }

    public void deletarPost(String id, Long autorId){
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        if (!post.getAutorId().equals(autorId)){
            throw new RuntimeException("Não pode deletar esse post");
        }

        postRepository.deleteById(id);
    }

    // ALT 1  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public List<PostFeedDTO> listarFeed(String usuarioLogadoUsername) {
        List<Post> posts = postRepository.findByComentPostIdIsNullOrderByDataCriacaoDesc();

        return mapearFeedDTO(posts, usuarioLogadoUsername);
    }

    public List<PostFeedDTO> listarFeedAutor(Long autorId){
        List<Post> posts = postRepository.findByAutorIdOrderByDataCriacaoDesc(autorId);

    return mapearFeedDTO(posts, usuarioLogadoUsername);
    }

    public Post criarComentario(String comentPostId, ComentarioDTO dto, Long autorId){
    postRepository.findById(comentPostId)
            .orElseThrow(() -> new RuntimeException("Post não encontrado"));

    Post comentario = new Post();
    comentario.setContent(dto.content());
    comentario.setMediaUrl(dto.mediaUrl());
    comentario.setDataCriacao(LocalDateTime.now());
    comentario.setComentPostId(comentPostId);

    // ALTERAÇÃO 2 -  Adicionado setAutorId — estava faltando,
    // causando autor nulo nos comentários e exibindo "Usuario removido" na tela.
    comentario.setAutorId(autorId);

    return postRepository.save(comentario);
    }

    // ALT 3  - Adicionado parâmetro usuarioLogadoUsername para calcular curtido
    public List<PostFeedDTO> listarComentarios(String comentPostId, String usuarioLogadoUsername){
        List<Post> comentarios = postRepository.findByComentPostIdOrderByDataCriacaoDesc(comentPostId);

        return mapearFeedDTO(comentarios, usuarioLogadoUsername);
    }

    private List<PostFeedDTO> mapearFeedDTO(List<Post> posts){
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> autorIds = posts.stream()
                .map(Post::getAutorId)
                .distinct()
                .toList();

        List<String> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        Map<Long, Usuario> autoresPorId = usuarioRepository.findAllById(autorIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        Map<String, Long> curtidasPorPost = curtidaRepository.countGroupedByPostId(postIds);
        Map<String, Long> comentariosPorPost = postRepository.countGroupedByComentPostId(postIds);

        return posts.stream().map(post -> {
            Usuario autor = autoresPorId.get(post.getAutorId());
            String autorNome = autor != null ? autor.getUsername() : "Usuário removido";
            String autorFoto = autor != null ? autor.getImg_perfil() : null;

            long totalCurtidas = curtidasPorPost.getOrDefault(post.getId(), 0L);
            long totalComentarios = comentariosPorPost.getOrDefault(post.getId(), 0L);

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

    public FeedPageDTO listarFeedCursor(String cursorCodificado, int limit) {
        var cursor = CursorCodec.decode(cursorCodificado);

        List<Post> posts = postRepository.ordenarFeedCursor(
                cursor != null ? cursor.dataCriacao() : null,
                cursor != null ? cursor.id() : null,
                limit
        );

        boolean hasMore = posts.size() > limit;
        List<Post> pagina = hasMore ? posts.subList(0, limit) : posts;
        List<PostFeedDTO> dtos = mapearFeedDTO(pagina);

        String nextCursor = null;
        if (hasMore) {
            Post ultimo = pagina.get(pagina.size() - 1);
            nextCursor = CursorCodec.encode(ultimo.getDataCriacao(), ultimo.getId());
        }

        return new FeedPageDTO(dtos, nextCursor, hasMore);
    }
}