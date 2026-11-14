package com.dailystudy.backend.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class MigracaoImagensRunnerTest {

    @Mock
    MigracaoExecutor usuarioExecutor;
    @Mock
    MigracaoPostExecutor postExecutor;

    @Test
    void semAplicar_rodaSimulacaoNosDois() throws Exception {
        when(usuarioExecutor.migrarUsuarios(false)).thenReturn(new MigracaoRelatorio());
        when(postExecutor.migrarPosts(false)).thenReturn(new MigracaoRelatorio());

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, false)
                .run(new DefaultApplicationArguments());

        verify(usuarioExecutor).migrarUsuarios(false);
        verify(postExecutor).migrarPosts(false);
        verify(usuarioExecutor, never()).migrarUsuarios(true);
        verify(postExecutor, never()).migrarPosts(true);
    }

    @Test
    void comAplicar_gravaNosDois() throws Exception {
        when(usuarioExecutor.migrarUsuarios(true)).thenReturn(new MigracaoRelatorio());
        when(postExecutor.migrarPosts(true)).thenReturn(new MigracaoRelatorio());

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, true)
                .run(new DefaultApplicationArguments());

        verify(usuarioExecutor).migrarUsuarios(true);
        verify(postExecutor).migrarPosts(true);
    }

    @Test
    void falhaNosUsuarios_propagaENaoComecaOsPosts() {
        when(usuarioExecutor.migrarUsuarios(true))
                .thenThrow(new IllegalStateException("bucket indisponível"));

        MigracaoImagensRunner runner = new MigracaoImagensRunner(usuarioExecutor, postExecutor, true);

        assertThrows(IllegalStateException.class,
                () -> runner.run(new DefaultApplicationArguments()));
        verifyNoInteractions(postExecutor);
    }

    @Test
    void semAplicar_logaModoSimulacao(CapturedOutput output) {
        when(usuarioExecutor.migrarUsuarios(false)).thenReturn(new MigracaoRelatorio());
        when(postExecutor.migrarPosts(false)).thenReturn(new MigracaoRelatorio());

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, false)
                .run(new DefaultApplicationArguments());

        assertThat(output).contains("SIMULACAO");
    }

    @Test
    void comAplicar_logaModoAplicacao(CapturedOutput output) {
        when(usuarioExecutor.migrarUsuarios(true)).thenReturn(new MigracaoRelatorio());
        when(postExecutor.migrarPosts(true)).thenReturn(new MigracaoRelatorio());

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, true)
                .run(new DefaultApplicationArguments());

        assertThat(output).contains("APLICACAO");
    }

    @Test
    void falhaNosUsuarios_apareceNoLogComoWarn(CapturedOutput output) {
        MigracaoRelatorio relatorioUsuarios = new MigracaoRelatorio();
        relatorioUsuarios.registrarFalha("usuario 7 / img_perfil", "formato invalido");

        when(usuarioExecutor.migrarUsuarios(false)).thenReturn(relatorioUsuarios);
        when(postExecutor.migrarPosts(false)).thenReturn(new MigracaoRelatorio());

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, false)
                .run(new DefaultApplicationArguments());

        assertThat(output)
                .containsPattern("WARN\\s+com\\.dailystudy\\.backend\\.migration\\.MigracaoImagensRunner")
                .contains("usuario 7 / img_perfil")
                .contains("formato invalido");
    }

    private MigracaoRelatorio relatorio(int migrados, int ignorados, int falhas) {
        MigracaoRelatorio r = new MigracaoRelatorio();
        for (int n = 0; n < migrados; n++) r.registrarMigrado();
        for (int n = 0; n < ignorados; n++) r.registrarIgnorado();
        for (int n = 0; n < falhas; n++) r.registrarFalha("falha-" + n, "formato invalido");
        return r;
    }

    @Test
    void comAplicar_logaContadoresPorTabela(CapturedOutput output) {
        when(usuarioExecutor.migrarUsuarios(true)).thenReturn(relatorio(3, 2, 1));
        when(postExecutor.migrarPosts(true)).thenReturn(relatorio(4, 5, 0));

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, true)
                .run(new DefaultApplicationArguments());

        assertThat(output)
                .contains("usuarios - migrados: 3, ignorados: 2, falhas: 1")
                .contains("posts - migrados: 4, ignorados: 5, falhas: 0");
    }

    @Test
    void semAplicar_logaContadoresComoMigraveis(CapturedOutput output) {
        when(usuarioExecutor.migrarUsuarios(false)).thenReturn(relatorio(3, 2, 1));
        when(postExecutor.migrarPosts(false)).thenReturn(relatorio(4, 5, 0));

        new MigracaoImagensRunner(usuarioExecutor, postExecutor, false)
                .run(new DefaultApplicationArguments());

        assertThat(output)
                .contains("usuarios - migraveis: 3, ignorados: 2, falhas: 1")
                .contains("posts - migraveis: 4, ignorados: 5, falhas: 0");
    }
}
