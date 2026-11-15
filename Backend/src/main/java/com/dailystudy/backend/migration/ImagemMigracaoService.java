package com.dailystudy.backend.migration;

import com.dailystudy.backend.service.OciStorageService;
import com.dailystudy.backend.util.ImageValidator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@Service
public class ImagemMigracaoService {

    private final OciStorageService storage;
    private record ImagemLida(byte[] bytes, String contentType){}

    public ImagemMigracaoService(OciStorageService storage) {
        this.storage = storage;
    }

    public Optional<String> migrar(String valorAtual) {
        if (!precisaMigrar(valorAtual)) {
            return Optional.empty();
        }
        ImagemLida imagem = lerImagem(valorAtual);
        String url = storage.upload(
                new ByteArrayInputStream(imagem.bytes()), imagem.bytes().length, imagem.contentType());
        return Optional.of(url);
    }

    public boolean precisaMigrar(String valor) {
            return valor != null && !valor.isBlank() && !valor.startsWith("https://");
    }

    public void validar(String valor) {
        lerImagem(valor);
    }

    private ImagemLida lerImagem(String valor) {
        byte[] bytes = DataUriDecoder.decodificar(valor);
        String contentType = ImageValidator.validar(bytes);
        return new ImagemLida(bytes, contentType);
    }
}
