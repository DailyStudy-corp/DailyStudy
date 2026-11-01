package com.dailystudy.backend.service;

import com.dailystudy.backend.exception.InvalidImageException;
import com.dailystudy.backend.util.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;


@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final OciStorageService ociStorageService;

    public String upload(MultipartFile arquivo) throws IOException{
        if (arquivo.isEmpty()) {
            throw new InvalidImageException("Arquivo vazio");
        }

        byte[] bytes = arquivo.getBytes();

        String tipo = ImageValidator.validar(bytes);

        return ociStorageService.upload(new ByteArrayInputStream(bytes), bytes.length, tipo);
    }
}
