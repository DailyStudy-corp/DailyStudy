package com.dailystudy.backend.controller;

import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro") // Mapeia a requisicao HTTP para criar um novo usuario no banco de dados
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRegistro dto) {
        usuarioService.registroUsuario(dto);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login (@Valid @RequestBody LoginDTO dto) {
        String token = usuarioService.autenticar(dto);

        return ResponseEntity.ok(Map.of("token", token));
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

}
