package com.dailystudy.backend.migration;

import com.dailystudy.backend.exception.InvalidImageException;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.repository.PostRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MigracaoPostExecutor {

    private final PostRepository postRepository;
    private final CampoImagemMigracao migrador;

    public MigracaoPostExecutor(PostRepository postRepository, ImagemMigracaoService imagemService) {
        this.postRepository = postRepository;
        this.migrador = new CampoImagemMigracao(imagemService);
    }

    public MigracaoRelatorio migrarPosts(boolean aplicar) {
        MigracaoRelatorio relatorio = new MigracaoRelatorio();

        for (Post post : postRepository.findAll()) {
            Optional<String> novaUrl = migrador.processarCampo(post.getMediaUrl(), aplicar, relatorio, "post " + post.getId() + " / mediaUrl");

            novaUrl.ifPresent(post::setMediaUrl);

            if (novaUrl.isPresent()) {
                postRepository.save(post);
            }
        }
        return relatorio;
    }
}
