package com.dailystudy.backend.controller;

import com.dailystudy.backend.config.SecurityConfig;
import com.dailystudy.backend.exception.InvalidImageException;
import com.dailystudy.backend.repository.UsuarioRepository;
import com.dailystudy.backend.service.ImageUploadService;
import com.dailystudy.backend.service.RateLimiterService;
import com.dailystudy.backend.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImagemController.class)
@Import(SecurityConfig.class)
class ImagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageUploadService imageUploadService;

    // exigidos pelos beans web reais que o slice carrega (SecurityFilter e RateLimitInterceptor)
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UsuarioRepository usuarioRepository;
    @MockitoBean
    private RateLimiterService rateLimiterService;

    @Test
    void upload_imagemValida_devolve201ComUrl() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.png", "image/png", "conteudo".getBytes(StandardCharsets.UTF_8));
        when(imageUploadService.upload(any(MultipartFile.class))).thenReturn("https://exemplo.test/o/abc");

        mockMvc.perform(multipart("/api/imagens").file(arquivo).with(user("paulo")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://exemplo.test/o/abc"));
    }

    @Test
    void upload_imagemInvalida_devolve400ComMensagemDaExcecao() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.png", "image/png", "qualquer".getBytes(StandardCharsets.UTF_8));
        when(imageUploadService.upload(any(MultipartFile.class)))
                .thenThrow(new InvalidImageException("Arquivo vazio"));

        mockMvc.perform(multipart("/api/imagens").file(arquivo).with(user("paulo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Arquivo vazio"));
    }

    @Test
    void upload_semAutenticacao_eRecusadoESemChamarServico() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "foto.png", "image/png", "qualquer".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/imagens").file(arquivo))   // sem .with(user(...))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imageUploadService);
    }
}
