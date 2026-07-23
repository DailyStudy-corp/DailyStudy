package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private static final String COOKIE_NAME = "access_token";
    private static final Duration COOKIE_MAX_AGE = Duration.ofHours(2);

    @Value("${app.security.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/registro") // Mapeia a requisicao HTTP para criar um novo usuario no banco de dados
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRegistro dto) {
        usuarioService.registroUsuario(dto);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login (@Valid @RequestBody LoginDTO dto, HttpServletResponse response, CsrfToken csrfToken) {
        String token = usuarioService.autenticar(dto);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //Forca o cookie a ser gerado, para resolver o bug de post/put
        csrfToken.getToken();

        return ResponseEntity.ok(Map.of("message", "Login feito com sucesso"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obterUsuarioLogado(@AuthenticationPrincipal Usuario usuarioLogado){
        if (usuarioLogado == null){
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioLogado));
    }

    @PutMapping("/me/img_perfil")
    public ResponseEntity<Void> uploadImagemPerfil(@AuthenticationPrincipal Usuario usuarioLogado, @RequestBody ImagemPerfil dto){
        if (usuarioLogado == null){
            return ResponseEntity.status(401).build();
        }

        usuarioService.atualizarImgPerfil(usuarioLogado.getId(), dto);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/perfil")
    public ResponseEntity<Void> editarPerfil(@AuthenticationPrincipal Usuario usuarioLogado, @RequestBody DadosPerfil dto){
        if (usuarioLogado == null){
            return ResponseEntity.status(401).build();
        }

        usuarioService.atualizarDadosPerfil(usuarioLogado.getId(), dto);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/perfil/{username}")
    public ResponseEntity<PerfilPublicoDTO> listarPostsPorUsuarios(@PathVariable String username){
        return ResponseEntity.ok(usuarioService.buscarPerfilPublico(username));
    }

}
