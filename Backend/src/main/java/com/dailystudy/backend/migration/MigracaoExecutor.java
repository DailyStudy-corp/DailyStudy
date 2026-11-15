package com.dailystudy.backend.migration;

import com.dailystudy.backend.exception.InvalidImageException;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MigracaoExecutor {

    private final UsuarioRepository usuarioRepository;
    private final CampoImagemMigracao migrador;

    public MigracaoExecutor(UsuarioRepository usuarioRepository, ImagemMigracaoService imagemService) {
        this.usuarioRepository = usuarioRepository;
        this.migrador = new CampoImagemMigracao(imagemService);
    }

    public MigracaoRelatorio migrarUsuarios(boolean aplicar) {

        MigracaoRelatorio relatorio = new MigracaoRelatorio();

        for (Usuario usuario : usuarioRepository.findAll()) {
            Optional<String> novaFoto = migrador.processarCampo(usuario.getImg_perfil(), aplicar, relatorio, "usuario " + usuario.getId() + " / img_perfil");
            Optional<String> novoBanner = migrador.processarCampo(usuario.getBanner_perfil(), aplicar, relatorio, "usuario " + usuario.getId() + " / banner_perfil");

            novaFoto.ifPresent(usuario::setImg_perfil);
            novoBanner.ifPresent(usuario::setBanner_perfil);

            if (novaFoto.isPresent() || novoBanner.isPresent()) {
                usuarioRepository.save(usuario);
            }
        }
        return relatorio;
    }
}
