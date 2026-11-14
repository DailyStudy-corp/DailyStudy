package com.dailystudy.backend.migration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MigracaoImagensRunnerTrancaTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MigracaoImagensRunner.class)
            .withBean(MigracaoExecutor.class, () -> mock(MigracaoExecutor.class))
            .withBean(MigracaoPostExecutor.class, () -> mock(MigracaoPostExecutor.class));

    @Test
    void semPropriedade_runnerNaoExiste() {
        contextRunner.run(ctx -> assertThat(ctx).doesNotHaveBean(MigracaoImagensRunner.class));
    }

    @Test
    void habilitadaFalse_runnerNaoExiste() {
        contextRunner.withPropertyValues("migracao.imagens.habilitada=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(MigracaoImagensRunner.class));
    }

    @Test
    void habilitadaTrue_runnerExiste() {
        contextRunner.withPropertyValues("migracao.imagens.habilitada=true")
                .run(ctx -> assertThat(ctx).hasSingleBean(MigracaoImagensRunner.class));
    }

    @Test
    void semAplicar_runnerSimula() {
        MigracaoExecutor usuarios = mock(MigracaoExecutor.class);
        MigracaoPostExecutor posts = mock(MigracaoPostExecutor.class);
        when(usuarios.migrarUsuarios(false)).thenReturn(new MigracaoRelatorio());
        when(posts.migrarPosts(false)).thenReturn(new MigracaoRelatorio());

        new ApplicationContextRunner()
                .withUserConfiguration(MigracaoImagensRunner.class)
                .withBean(MigracaoExecutor.class, () -> usuarios)
                .withBean(MigracaoPostExecutor.class, () -> posts)
                .withPropertyValues("migracao.imagens.habilitada=true")
                .run(ctx -> {
                    ctx.getBean(MigracaoImagensRunner.class).run(new DefaultApplicationArguments());
                    verify(usuarios).migrarUsuarios(false);
                    verify(posts).migrarPosts(false);
                });
    }

    @Test
    void aplicarTrue_runnerGrava() {
        MigracaoExecutor usuarios = mock(MigracaoExecutor.class);
        MigracaoPostExecutor posts = mock(MigracaoPostExecutor.class);
        when(usuarios.migrarUsuarios(true)).thenReturn(new MigracaoRelatorio());
        when(posts.migrarPosts(true)).thenReturn(new MigracaoRelatorio());

        new ApplicationContextRunner()
                .withUserConfiguration(MigracaoImagensRunner.class)
                .withBean(MigracaoExecutor.class, () -> usuarios)
                .withBean(MigracaoPostExecutor.class, () -> posts)
                .withPropertyValues("migracao.imagens.habilitada=true",
                        "migracao.imagens.aplicar=true")
                .run(ctx -> {
                    ctx.getBean(MigracaoImagensRunner.class).run(new DefaultApplicationArguments());
                    verify(usuarios).migrarUsuarios(true);
                    verify(posts).migrarPosts(true);
                });
    }
}
