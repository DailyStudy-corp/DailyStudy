package com.dailystudy.backend.migration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MigracaoRelatorioTest {

    @Test
    void relatorioNovo_comecaZerado() {
        MigracaoRelatorio relatorio = new MigracaoRelatorio();

        assertEquals(0, relatorio.getMigrados());
        assertEquals(0, relatorio.getIgnorados());
        assertTrue(relatorio.getFalhas().isEmpty());
    }

    @Test
    void registrar_acumulaMigradosEIgnorados() {
        MigracaoRelatorio relatorio = new MigracaoRelatorio();

        relatorio.registrarMigrado();
        relatorio.registrarMigrado();
        relatorio.registrarIgnorado();

        assertEquals(2, relatorio.getMigrados());
        assertEquals(1, relatorio.getIgnorados());
    }

    @Test
    void registrarFalha_guardaReferenciaEMotivo() {
        MigracaoRelatorio relatorio = new MigracaoRelatorio();

        relatorio.registrarFalha("usuario 3 / banner_perfil", "imagem excede 5MB");

        assertEquals(List.of("usuario 3 / banner_perfil: imagem excede 5MB"), relatorio.getFalhas());
    }

    @Test
    void getFalhas_devolveListaQueNaoPodeSerAlteradaDeFora() {
        MigracaoRelatorio relatorio = new MigracaoRelatorio();
        relatorio.registrarFalha("usuario 3 / banner_perfil", "imagem excede 5MB");

        assertThrows(UnsupportedOperationException.class, () -> relatorio.getFalhas().clear());
    }
}
