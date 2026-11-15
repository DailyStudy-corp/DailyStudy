package com.dailystudy.backend.util;

import com.dailystudy.backend.config.OciStorageProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ImageUrlValidator {

    private static final String UUID_MINUSCULO = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private final Pattern padraoUrlDoBucket;

    public ImageUrlValidator(OciStorageProperties properties) {
        this.padraoUrlDoBucket = Pattern.compile(Pattern.quote(properties.publicBaseUrl()) + UUID_MINUSCULO);

    }

    //Sera removido pos migracao
    private static final Pattern DATA_IMAGE_PATTERN =
            Pattern.compile("^data:image/(png|jpe?g|webp|gif);base64,[A-Za-z0-9+/=]+$",
                    Pattern.CASE_INSENSITIVE);

    public boolean isSafeImage(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        return padraoUrlDoBucket.matcher(url).matches()
                || DATA_IMAGE_PATTERN.matcher(url).matches();
    }
}
