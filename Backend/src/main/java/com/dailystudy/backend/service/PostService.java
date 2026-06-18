package com.dailystudy.backend.service;

import com.dailystudy.backend.model.Atividade;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.repository.AtividadeRepository;
import com.dailystudy.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AtividadeRepository atividadeRepository;

    public Post criarPost(Post post, String emailAutor, String nomeAutor) {
        post.setAutorId(emailAutor);
        post.setDataCriacao(LocalDateTime.now());

        Post postSalvo = postRepository.save(post);

        Atividade atividade = new Atividade();
        atividade.setAutorId(emailAutor);
        atividade.setStatus("POSTED");
        atividade.setObjetoId(postSalvo.getId());
        atividade.setDataCriacao(LocalDateTime.now());

        atividade.getMetadata().put("autorId", nomeAutor);
        atividade.getMetadata().put("snippet", extrairSnippet(postSalvo.getContent()));

        atividadeRepository.save(atividade);

        return postSalvo;
    }

    public List<Atividade> listarFeed() {
        return atividadeRepository.findAllByOrderByDataCriacaoDesc();
    }

    private String extrairSnippet(String content){
        if (content == null)
            return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
