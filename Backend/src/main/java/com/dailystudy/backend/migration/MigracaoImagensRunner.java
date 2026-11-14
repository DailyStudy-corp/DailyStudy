package com.dailystudy.backend.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "migracao.imagens", name = "habilitada", havingValue = "true")
class MigracaoImagensRunner implements ApplicationRunner {

    private final MigracaoExecutor usuarioExecutor;
    private final MigracaoPostExecutor postExecutor;
    private final boolean aplicar;

    MigracaoImagensRunner(MigracaoExecutor usuarioExecutor, MigracaoPostExecutor postExecutor, @Value("${migracao.imagens.aplicar:false}") boolean aplicar) {
        this.usuarioExecutor = usuarioExecutor;
        this.postExecutor = postExecutor;
        this.aplicar = aplicar;
    }

    @Override
    public void run(ApplicationArguments args) {

        log.info("Migracao de imagens iniciada. Modo: {}", aplicar ? "APLICACAO" : "SIMULACAO");

        registrar("usuarios", usuarioExecutor.migrarUsuarios(aplicar), aplicar);
        registrar("posts", postExecutor.migrarPosts(aplicar), aplicar);
    }

    private void registrar(String tabela, MigracaoRelatorio relatorio, boolean aplicar) {
        String rotulo = aplicar ? "migrados" : "migraveis";

        log.info("{} - {}: {}, ignorados: {}, falhas: {}",
                tabela,
                rotulo,
                relatorio.getMigrados(),
                relatorio.getIgnorados(),
                relatorio.getFalhas().size());

        for (String falha : relatorio.getFalhas()) {
            log.warn("Falha na migracao de {} - {}", tabela, falha);
        }
    }
}
