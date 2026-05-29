package com.dailystudy.backend.repository;

import com.dailystudy.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    //Aqui o JPA funciona como as queries do SQL: SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    //Aqui da mesma forma porém trocando pelo username: SELECT * FROM usuarios WHERE username = ?
    Optional<Usuario> findByUsername(String username);
}
