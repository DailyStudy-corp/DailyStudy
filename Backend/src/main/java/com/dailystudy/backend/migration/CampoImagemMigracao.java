package com.dailystudy.backend.migration;

import com.dailystudy.backend.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class CampoImagemMigracao {

    private final ImagemMigracaoService imagemService;

    Optional<String> processarCampo(String valor, boolean aplicar,
                                    MigracaoRelatorio relatorio, String referencia) {
        if (!imagemService.precisaMigrar(valor)) {
            relatorio.registrarIgnorado();
            return Optional.empty();
        }
        try {
            if (!aplicar) {
                imagemService.validar(valor);
                relatorio.registrarMigrado();
                return Optional.empty();
            }
            String url = imagemService.migrar(valor).orElseThrow();
            relatorio.registrarMigrado();
            return Optional.of(url);
        } catch (IllegalArgumentException | InvalidImageException e) {
            relatorio.registrarFalha(referencia, e.getMessage());
            return Optional.empty();
        }
    }
}
