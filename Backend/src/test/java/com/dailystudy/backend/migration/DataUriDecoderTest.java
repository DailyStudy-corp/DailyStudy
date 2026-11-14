package com.dailystudy.backend.migration;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataUriDecoderTest {

    // PNG de 1x1 pixel, válido
    private static final String PNG_1X1 =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Test
    void decodificar_dataUriValida_devolveOsBytesDaImagem() {
        byte[] bytes = DataUriDecoder.decodificar(PNG_1X1);

        byte[] assinaturaPng = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertArrayEquals(assinaturaPng, Arrays.copyOf(bytes, 8));
    }

    @Test
    void decodificar_formatoForaDaLista_lancaIllegalArgumentException() {
        String svg = "data:image/svg+xml;base64,PHN2Zz48L3N2Zz4=";

        assertThrows(IllegalArgumentException.class, () -> DataUriDecoder.decodificar(svg));
    }

    @Test
    void decodificar_semPrefixoData_lancaIllegalArgumentException() {
        String semPrefixo = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

        assertThrows(IllegalArgumentException.class, () -> DataUriDecoder.decodificar(semPrefixo));
    }

    @Test
    void decodificar_base64ComPreenchimentoInvalido_lancaIllegalArgumentException() {
        // passa no regex (só caracteres do alfabeto), mas o tamanho não fecha em múltiplo de 4
        String truncado = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=";

        assertThrows(IllegalArgumentException.class, () -> DataUriDecoder.decodificar(truncado));
    }
}
