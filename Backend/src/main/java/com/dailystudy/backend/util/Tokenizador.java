package com.dailystudy.backend.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Tokenizador {

    private static final Pattern PONTUACAO_NAS_BORDAS = Pattern.compile("^[^a-z0-9+#]+|[^a-z0-9+#]+$");

    // Depois de aparar a pontuação das bordas, um termo só é válido se
    // sobrar pelo menos uma letra/dígito — evita indexar "..." ou "+++".
    private static final Pattern TEM_ALFANUMERICO = Pattern.compile(".*[a-z0-9].*");

    // Stopwords PT-BR: palavras de ligação com pouquíssima informação pra
    // busca. Sem elas, "de"/"para"/"com" (presentes em quase todo texto em
    // português) criariam entradas gigantes e irrelevantes no índice — um
    // problema de bloat/precisão que "não salvar histórico sem resultado"
    private static final Set<String> STOPWORDS = Set.of(
            "a", "ao", "aos", "aquela", "aquelas", "aquele", "aqueles", "aquilo",
            "as", "ate", "com", "como", "da", "das", "de", "dela", "delas", "dele",
            "deles", "depois", "do", "dos", "e", "ela", "elas", "ele", "eles", "em",
            "entre", "era", "essa", "essas", "esse", "esses", "esta", "estas",
            "este", "estes", "eu", "foi", "foram", "isso", "isto", "ja", "lhe",
            "mais", "mas", "me", "mesmo", "meu", "meus", "minha", "minhas", "muito",
            "na", "nao", "nas", "nem", "no", "nos", "nossa", "nossas", "nosso",
            "nossos", "num", "numa", "o", "os", "ou", "para", "pela", "pelas",
            "pelo", "pelos", "por", "qual", "quando", "que", "quem", "se", "seu",
            "seus", "so", "sua", "suas", "tambem", "te", "tem", "teu", "teus",
            "tu", "tua", "tuas", "um", "uma", "voce", "voces"
    );

    private Tokenizador(){
    }

    public static Set<String> tokenizar(String texto) {
        String normalizado = Normalizador.normalizar(texto);

        if (normalizado.isBlank()){
            return Set.of();
        }

        return Arrays.stream(normalizado.split("\\s+"))
                .map(palavra -> PONTUACAO_NAS_BORDAS.matcher(palavra).replaceAll(""))
                .filter(termo -> !termo.isBlank())
                .filter(termo -> TEM_ALFANUMERICO.matcher(termo).matches())
                .filter(termo ->!STOPWORDS.contains(termo))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
