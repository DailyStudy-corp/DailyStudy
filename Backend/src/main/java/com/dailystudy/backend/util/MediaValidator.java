package com.dailystudy.backend.util;

import java.util.regex.Pattern;

public class MediaValidator {
    private static final Pattern DATA_IMAGE_PATTERN =                                        //CASE_INSENSITIVE ignora lower case e upper case
            Pattern.compile("^data:image/(png|jpe?g|webp|gif);base64,[A-Za-z0-9+/=]+$", Pattern.CASE_INSENSITIVE);

    private static final Pattern HTTPS_URL_PATTERN =
            Pattern.compile("^https://[^\\\"'<>\\\\s]+$");

    private MediaValidator() {
    }

    public static boolean isSafeImage(String url){
        if (url == null || url.isBlank()) {
            return true;
        }
        return DATA_IMAGE_PATTERN.matcher(url).matches()
                || HTTPS_URL_PATTERN.matcher(url).matches();
    }
}
