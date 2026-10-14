package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.CurtidaDTO;
import com.dailystudy.backend.model.Curtida;
import com.dailystudy.backend.repository.CurtidaRepository;
import com.dailystudy.backend.repository.PostRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurtidaService {

    private final PostRepository postRepository;
    private final CurtidaRepository curtidaRepository;

    public CurtidaDTO toggleCurtida(String postId, Long autorId){
        postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Curtida falhou: post não encontrado (id={})", postId);
                    return new RuntimeException("Post não encontrado");
                });

        Optional<Curtida> curtidaExistente = curtidaRepository.findByPostIdAndAutorId(postId, autorId);

        boolean curtido;
        if (curtidaExistente.isPresent()) {
            curtidaRepository.delete(curtidaExistente.get());
            curtido = false;
        } else {
            try {
                Curtida novaCurtida = new Curtida(null, autorId, postId, LocalDateTime.now());
                curtidaRepository.save(novaCurtida);
                curtido = true;
            } catch (DuplicateKeyException e) {
                log.warn("Corrida detectada ao curtir post (postId={}, autorId={}): curtida já existia", postId, autorId);
                curtido = true;
            }
        }

        long total = curtidaRepository.countByPostId(postId);
        return new CurtidaDTO(curtido, total);

        }

        public CurtidaDTO statusCurtida(String postId, Long autorId){
        boolean curtido = curtidaRepository.findByPostIdAndAutorId(postId, autorId).isPresent();
        long total = curtidaRepository.countByPostId(postId);

        return new CurtidaDTO(curtido, total);
    }
}
