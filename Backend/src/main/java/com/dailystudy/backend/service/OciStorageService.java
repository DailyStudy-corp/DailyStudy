package com.dailystudy.backend.service;

import com.dailystudy.backend.config.OciStorageProperties;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciStorageService {

    private final ObjectStorageClient objectStorageClient;

    private final OciStorageProperties ociStorageProperties;

    public String upload(InputStream conteudo, long tamanho, String contentType) {
        String nomeObjeto = UUID.randomUUID().toString();

        PutObjectRequest requisicao = PutObjectRequest.builder()
                .namespaceName(ociStorageProperties.namespace())
                .bucketName(ociStorageProperties.bucket())
                .objectName(nomeObjeto)
                .contentLength(tamanho)
                .contentType(contentType)
                .putObjectBody(conteudo)
                .build();

        objectStorageClient.putObject(requisicao);

        log.info("Objeto enviado ao bucket: nome={}, tamanho={}, bytes, tipo={}", nomeObjeto, tamanho, contentType);

        return montarUrlPublica(nomeObjeto);
    }

    private String montarUrlPublica(String nomeObjeto) {
        return ociStorageProperties.publicBaseUrl() + nomeObjeto;
    }

    public void deleteObject(String url) {
        String prefixo = ociStorageProperties.publicBaseUrl();

        if (url == null || !url.startsWith(prefixo)) {
            log.warn("Remocao ignorada: A URL nao pertence ao bucket");
            return;
        }

        String nomeObjeto = url.substring(prefixo.length());

        if (nomeObjeto.isBlank()) {
            log.warn("Remocao ignorada: URL sem nome de objeto");
            return;
        }

        DeleteObjectRequest requisicao = DeleteObjectRequest.builder()
                .namespaceName(ociStorageProperties.namespace())
                .bucketName(ociStorageProperties.bucket())
                .objectName(nomeObjeto)
                .build();

        objectStorageClient.deleteObject(requisicao);
        log.info("Objeto removido do bucket: nome={}", nomeObjeto);
    }
}
