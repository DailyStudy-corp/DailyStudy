package com.dailystudy.backend.service;

import com.dailystudy.backend.dto.DadosPerfil;
import com.dailystudy.backend.dto.ImagemPerfil;
import com.dailystudy.backend.dto.LoginDTO;
import com.dailystudy.backend.dto.UsuarioRegistro;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final TokenService tokenService;

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public void registroUsuario(UsuarioRegistro dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            return;
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(dto.getUsername());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        usuarioRepository.save(novoUsuario);
    }

    public String autenticar(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        return tokenService.gerarToken(usuario);

    }

    public void atualizarImgPerfil(Long id, ImagemPerfil dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        usuario.setImg_perfil(dto.img_perfil());
        usuario.setBanner_perfil(dto.banner_perfil());

        usuarioRepository.save(usuario);
    }

    public void atualizarDadosPerfil(Long id, DadosPerfil dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario não encontrado"));
        usuario.setUsername(dto.username());
        usuario.setCargo(dto.cargo());
        usuario.setBio(dto.bio());

        usuarioRepository.save(usuario);
    }
}