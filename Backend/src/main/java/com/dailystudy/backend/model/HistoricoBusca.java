package com.dailystudy.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_busca", uniqueConstraints = @UniqueConstraint(name = "uk_historico_usuario_termo", columnNames = {"usuario_id", "termo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoBusca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String termo;

    @Column(name = "data_busca", nullable = false)
    private LocalDateTime dataBusca;
}
