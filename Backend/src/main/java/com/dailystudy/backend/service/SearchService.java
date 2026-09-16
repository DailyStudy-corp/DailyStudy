package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.PostResultadoDTO;
import com.dailystudy.backend.dto.ResultadoBuscaDTO;
import com.dailystudy.backend.dto.UsuarioResponseDTO;
import com.dailystudy.backend.dto.UsuarioResultadoDTO;
import com.dailystudy.backend.model.IndiceBusca;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.TipoReferencia;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.IndiceBuscaRepository;
import com.dailystudy.backend.repository.PostRepository;
import com.dailystudy.backend.repository.UsuarioRepository;
import com.dailystudy.backend.util.Tokenizador;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final IndiceBuscaRepository indiceBuscaRepository;

    private final UsuarioRepository usuarioRepository;

    private final PostRepository postRepository;

    // Chave composta pra agrupar entradas do índice por resultado final.
    // record gera equals/hashCode automaticamente a partir dos campos,
    // então dá pra usar direto como chave de HashMap sem escrever nada a mais.
    private record ChaveResultado(TipoReferencia tipo, String refId) {
    }

    public ResultadoBuscaDTO buscar(String query){
        Set<String> termos = Tokenizador.tokenizar(query);

        if (termos.isEmpty()){
            return new ResultadoBuscaDTO(List.of(), List.of());
        }

        List<IndiceBusca> entradas = indiceBuscaRepository.findByTermoIn(termos);

        Map<ChaveResultado, Integer> pontuacoes = new HashMap<>();
        for (IndiceBusca entrada : entradas) {
            ChaveResultado chave = new ChaveResultado(entrada.getTipo(), entrada.getRefId());
            pontuacoes.merge(chave, entrada.getCampo().getPeso(), Integer::sum);
        }

        Map<ChaveResultado, Integer> pontuacoesUsuario = new HashMap<>();
        Map<ChaveResultado, Integer> pontuacoesPost = new HashMap<>();

        for (var entry : pontuacoes.entrySet()){
            if (entry.getKey().tipo() == TipoReferencia.USUARIO) {
                pontuacoesUsuario.put(entry.getKey(), entry.getValue());
            } else {
                pontuacoesPost.put(entry.getKey(), entry.getValue());
            }
        }

        return new ResultadoBuscaDTO(montarResultadosUsuario(pontuacoesUsuario), montarResultadosPost(pontuacoesPost));
    }

    private List<UsuarioResultadoDTO> montarResultadosUsuario(Map<ChaveResultado, Integer> pontuacoes) {
        if (pontuacoes.isEmpty())
            return List.of();

        List<Long> ids = pontuacoes.keySet().stream()
                .map(chave -> Long.valueOf(chave.refId()))
                .toList();

        Map<Long, Usuario> usuariosPorId = usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        return pontuacoes.entrySet().stream()
                .map(entry -> {
                    Usuario usuario = usuariosPorId.get(Long.valueOf(entry.getKey().refId));
                    return usuario == null ? null : new UsuarioResultadoDTO(usuario, entry.getValue());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(UsuarioResultadoDTO::score).reversed())
                .toList();
    }

    private List<PostResultadoDTO> montarResultadosPost(Map<ChaveResultado, Integer> pontuacoes) {
        if (pontuacoes.isEmpty())
            return List.of();

        List<String> ids = pontuacoes.keySet().stream()
                .map(ChaveResultado::refId)
                .toList();

        Map<String, Post> postsPorId = postRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));

        List<Long> autorIds = postsPorId.values().stream()
                .map(Post::getAutorId)
                .distinct()
                .toList();

        Map<Long, Usuario> autoresPorId = usuarioRepository.findAllById(autorIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        return pontuacoes.entrySet().stream()
                .map(entry -> {
                    Post post = postsPorId.get(entry.getKey().refId());
                    if (post == null)
                        return null;

                    Usuario autor = autoresPorId.get(post.getAutorId());
                    String autorUsername = autor != null ? autor.getUsername() : "Usuário removido";

                    return new PostResultadoDTO(post, autorUsername, entry.getValue());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(PostResultadoDTO::score).reversed())
                .toList();
    }
}
