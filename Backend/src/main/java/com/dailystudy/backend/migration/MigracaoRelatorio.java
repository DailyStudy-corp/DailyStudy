package com.dailystudy.backend.migration;

import java.util.ArrayList;
import java.util.List;

public class MigracaoRelatorio {

    private int migrados;
    private int ignorados;
    private final List<String> falhas = new ArrayList<>();

    public void registrarMigrado() { migrados++; }

    public void registrarIgnorado() { ignorados++; }

    public void registrarFalha(String referencia, String motivo) {
        falhas.add(referencia + ": " + motivo);
    }

    public int getMigrados() { return migrados; }

    public int getIgnorados() { return ignorados; }

    public List<String> getFalhas() { return List.copyOf(falhas); }
}
