package com.dailystudy.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("oci.storage")
public record OciStorageProperties(@NotBlank String configFile, @NotBlank String profile, @NotBlank String namespace, @NotBlank String bucket, @NotBlank String publicBaseUrl) {
}
