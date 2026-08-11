package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.CurtidaDTO;
import com.dailystudy.backend.model.Curtida;
import com.dailystudy.backend.repository.CurtidaRepository;
import com.dailystudy.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurtidaService {

    private final PostRepository postRepository;
    private final CurtidaRepository curtidaRepository;

    public CurtidaDTO toggleCurtida(String postId, String username){
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Optional<Curtida> curtidaExistente = curtidaRepository.findByPostIdAndAutorId(postId, username);

        boolean curtido;
        if (curtidaExistente.isPresent()) {
            curtidaRepository.delete(curtidaExistente.get());
            curtido = false;
        } else {
            Curtida novaCurtida = new Curtida(null, username, postId, LocalDateTime.now());
            curtidaRepository.save(novaCurtida);
            curtido = true;
        }

        long total = curtidaRepository.countByPostId(postId);
        return new CurtidaDTO(curtido, total);

        }

        public CurtidaDTO statusCurtida(String postId, String username){
        boolean curtido = curtidaRepository.findByPostIdAndAutorId(postId, username).isPresent();
        long total = curtidaRepository.countByPostId(postId);

        return new CurtidaDTO(curtido, total);
    }
}
