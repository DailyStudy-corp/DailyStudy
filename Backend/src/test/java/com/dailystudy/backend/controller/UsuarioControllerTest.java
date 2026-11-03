package com.dailystudy.backend.controller;

import com.dailystudy.backend.config.SecurityConfig;
import com.dailystudy.backend.dto.ImagemPerfil;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.model.UsuarioRole;
import com.dailystudy.backend.repository.UsuarioRepository;
import com.dailystudy.backend.service.RateLimiterService;
import com.dailystudy.backend.service.TokenService;
import com.dailystudy.backend.service.UsuarioService;
import com.dailystudy.backend.util.ImageUrlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
public class UsuarioControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UsuarioService usuarioService;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    ImageUrlValidator imageUrlValidator;
    @MockitoBean
    UsuarioRepository usuarioRepository;
    @MockitoBean
    RateLimiterService rateLimiterService;

    private RequestPostProcessor logadoComo(Usuario usuario) {
        return authentication(new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()));
    }

    @Test
    void uploadImagemPerfil_comImgPerfilInvalida_retorna400ENaoAtualiza() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRole(UsuarioRole.USER);   // ajuste ao nome real do seu enum

        when(imageUrlValidator.isSafeImage("https://evil.com/x.png")).thenReturn(false);

        mockMvc.perform(put("/api/usuarios/me/img_perfil")
                        .with(logadoComo(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"img_perfil":"https://evil.com/x.png","banner_perfil":null}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void uploadImagemPerfil_comBannerInvalido_retorna400ENaoAtualiza() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRole(UsuarioRole.USER);

        String imgBoa = "https://exemplo.test/o/abc";
        when(imageUrlValidator.isSafeImage(imgBoa)).thenReturn(true);
        when(imageUrlValidator.isSafeImage("https://evil.com/b.png")).thenReturn(false);

        mockMvc.perform(put("/api/usuarios/me/img_perfil")
                        .with(logadoComo(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"img_perfil":"https://exemplo.test/o/abc","banner_perfil":"https://evil.com/b.png"}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void uploadImagemPerfil_comImagensValidas_retorna204EAtualiza() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRole(UsuarioRole.USER);

        String img = "https://exemplo.test/o/abc";
        String banner = "https://exemplo.test/o/def";
        when(imageUrlValidator.isSafeImage(img)).thenReturn(true);
        when(imageUrlValidator.isSafeImage(banner)).thenReturn(true);

        mockMvc.perform(put("/api/usuarios/me/img_perfil")
                        .with(logadoComo(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"img_perfil":"https://exemplo.test/o/abc","banner_perfil":"https://exemplo.test/o/def"}"""))
                .andExpect(status().isNoContent());

        verify(usuarioService).atualizarImgPerfil(eq(1L), eq(new ImagemPerfil(img, banner)));
    }
}
