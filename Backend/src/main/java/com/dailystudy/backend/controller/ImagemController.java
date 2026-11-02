package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.ImageUploadDTO;
import com.dailystudy.backend.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/imagens")
@RequiredArgsConstructor
public class ImagemController {

    private final ImageUploadService imageUploadService;

    @PostMapping
    public ResponseEntity<ImageUploadDTO> upload(@RequestParam("arquivo")MultipartFile arquivo) throws IOException{

        String url = imageUploadService.upload(arquivo);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadDTO(url));
    }
}
