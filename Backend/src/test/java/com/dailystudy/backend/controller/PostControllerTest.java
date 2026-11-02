package com.dailystudy.backend.controller;

import com.dailystudy.backend.config.SecurityConfig;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.model.UsuarioRole;
import com.dailystudy.backend.repository.UsuarioRepository;
import com.dailystudy.backend.service.CurtidaService;
import com.dailystudy.backend.service.PostService;
import com.dailystudy.backend.service.RateLimiterService;
import com.dailystudy.backend.service.TokenService;
import com.dailystudy.backend.util.ImageUrlValidator;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PostService postService;
    @MockitoBean
    CurtidaService curtidaService;
    @MockitoBean
    ImageUrlValidator imageUrlValidator;
    // exigidos pelo SecurityFilter e pelo WebConfig, como no ImagemControllerTest
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    UsuarioRepository usuarioRepository;
    @MockitoBean
    RateLimiterService rateLimiterService;

    private RequestPostProcessor logadoComo(Usuario usuario) {
        return authentication(new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()));
    }

    @Test
    void postar_comImagemInvalida_retorna400ENaoCria() throws Exception {
        Usuario autor = new Usuario();
        autor.setId(1L);
        autor.setRole(UsuarioRole.USER);   // ajuste ao nome real do seu enum
        when(imageUrlValidator.isSafeImage("https://evil.com/x.png")).thenReturn(false);

        mockMvc.perform(post("/api/posts")
                        .with(logadoComo(autor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"content":"oi","mediaUrl":"https://evil.com/x.png"}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postService);
    }

    @Test
    void postar_comImagemValida_retorna201ECriaOPost() throws Exception {
        Usuario autor = new Usuario();
        autor.setId(1L);
        autor.setRole(UsuarioRole.USER);

        String url = "https://exemplo.test/o/abc";   // o valor não importa: o validador é mock
        Post criado = new Post();
        criado.setId("p1");
        when(imageUrlValidator.isSafeImage(url)).thenReturn(true);
        when(postService.criarPost(any(), eq(1L))).thenReturn(criado);

        mockMvc.perform(post("/api/posts")
                        .with(logadoComo(autor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"content":"oi","mediaUrl":"https://exemplo.test/o/abc"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p1"));

        verify(postService).criarPost(any(), eq(1L));
    }
}
