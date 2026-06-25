package com.dailystudy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Usuario implements UserDetails {
    //O Id garante que cada usuario tenha um registro proprio
    @Id
    //Um valor para o Id é gerado para cada registro inserido no banco.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Long é utilizado para numeros inteiros, que perfomam melhor no banco
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(columnDefinition = "TEXT")
    private String img_perfil;

    @Column(columnDefinition = "TEXT")
    private String banner_perfil;

    @Column(length = 60)
    private String cargo;

    @Column(length = 200)
    private String bio;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioRole role;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UsuarioRole.ADM){
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADM"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
