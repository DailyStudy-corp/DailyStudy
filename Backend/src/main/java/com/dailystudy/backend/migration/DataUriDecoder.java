package com.dailystudy.backend.migration;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUriDecoder {

    private static final Pattern DATA_URI = Pattern.compile(
            "^data:image/(png|jpe?g|webp|gif);base64,([A-Za-z0-9+/=]+)$",
            Pattern.CASE_INSENSITIVE);

    public static byte[] decodificar(String dataUri){
        Matcher matcher = DATA_URI.matcher(dataUri);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Data URI inválida ou de formato não permitido");
        }
        return Base64.getDecoder().decode(matcher.group(2));
    }
}
