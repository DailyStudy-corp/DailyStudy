package com.dailystudy.backend.service;

import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.repository.PostRepository;
import java.util.List;
import com.dailystudy.backend.dto.*;
import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.model.UsuarioRole;
import com.dailystudy.backend.repository.UsuarioRepository;
import com.dailystudy.backend.exception.UsuarioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final TokenService tokenService;

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final PostService postService;

    public void registroUsuario(UsuarioRegistro dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UsuarioException("Email inválido");
        }

        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()){
            throw new UsuarioException("Este nome já esta em uso");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(dto.getUsername());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setRole(UsuarioRole.USER);

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
      // Gui - Criei um metodo auxiliar para buscar o user por id
    public Usuario buscarPorId(Long id) {
     return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
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

    if (!usuario.getUsername().equals(dto.username())
            && usuarioRepository.findByUsername(dto.username()).isPresent()) {
        throw new UsuarioException("Este nome de usuário já está em uso");
    }

    usuario.setUsername(dto.username());
    usuario.setCargo(dto.cargo());
    usuario.setBio(dto.bio());

    usuarioRepository.save(usuario);

    }

    public PerfilPublicoDTO buscarPerfilPublico(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        List<PostFeedDTO> posts = postService.listarFeedAutor(usuario.getId());

        return new PerfilPublicoDTO(
                usuario.getUsername(),
                usuario.getImg_perfil(),
                usuario.getBanner_perfil(),
                usuario.getCargo(),
                usuario.getBio(),
                posts
        );
    }
}