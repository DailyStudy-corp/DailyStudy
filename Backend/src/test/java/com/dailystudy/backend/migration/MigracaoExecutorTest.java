package com.dailystudy.backend.migration;

import com.dailystudy.backend.model.Usuario;
import com.dailystudy.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MigracaoExecutorTest {

    private static final String PNG_1X1 =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ImagemMigracaoService imagemService;

    @InjectMocks
    private MigracaoExecutor executor;

    @Test
    void migrarUsuarios_simulacao_validaMasNaoSobeNemGrava() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setImg_perfil(PNG_1X1);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.precisaMigrar(null)).thenReturn(false);

        MigracaoRelatorio relatorio = executor.migrarUsuarios(false);

        assertEquals(1, relatorio.getMigrados());

        verify(imagemService).validar(PNG_1X1);
        verify(imagemService, never()).migrar(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void migrarUsuarios_camposVaziosOuJaMigrados_saoContadosComoIgnorados() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(imagemService.precisaMigrar(null)).thenReturn(false);

        MigracaoRelatorio relatorio = executor.migrarUsuarios(false);

        assertEquals(0, relatorio.getMigrados());
        assertEquals(2, relatorio.getIgnorados());
    }

    @Test
    void migrarUsuarios_aplicar_sobeEGravaAUrlNova() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setImg_perfil(PNG_1X1);
        String urlNova = "https://exemplo.test/o/abc";
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.precisaMigrar(null)).thenReturn(false);
        when(imagemService.migrar(PNG_1X1)).thenReturn(Optional.of(urlNova));

        AtomicReference<String> fotoNoSave = new AtomicReference<>();
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario salvo = invocacao.getArgument(0);
            fotoNoSave.set(salvo.getImg_perfil());
            return salvo;
        });

        MigracaoRelatorio relatorio = executor.migrarUsuarios(true);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(urlNova, fotoNoSave.get());
        assertEquals(urlNova, usuario.getImg_perfil());
        verify(usuarioRepository).save(usuario);
        verify(imagemService, never()).validar(any());
    }

    @Test
    void migrarUsuarios_aplicar_falhaEmUmCampoNaoInterrompeOsDemais() {
        String invalida = "data:image/png;base64,imagem-quebrada";
        String urlNova = "https://exemplo.test/o/abc";

        Usuario comProblema = new Usuario();
        comProblema.setId(1L);
        comProblema.setImg_perfil(invalida);

        Usuario saudavel = new Usuario();
        saudavel.setId(2L);
        saudavel.setImg_perfil(PNG_1X1);

        when(usuarioRepository.findAll()).thenReturn(List.of(comProblema, saudavel));
        when(imagemService.precisaMigrar(invalida)).thenReturn(true);
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.precisaMigrar(null)).thenReturn(false);
        when(imagemService.migrar(invalida)).thenThrow(new IllegalArgumentException("Data URI inválida"));
        when(imagemService.migrar(PNG_1X1)).thenReturn(Optional.of(urlNova));

        MigracaoRelatorio relatorio = executor.migrarUsuarios(true);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(1, relatorio.getFalhas().size());
        assertEquals("usuario 1 / img_perfil: Data URI inválida", relatorio.getFalhas().get(0));
        assertEquals(invalida, comProblema.getImg_perfil());
        assertEquals(urlNova, saudavel.getImg_perfil());
        verify(usuarioRepository, never()).save(comProblema);
        verify(usuarioRepository).save(saudavel);
    }

    @Test
    void migrarUsuarios_aplicar_falhaDeUploadNaoEhEngolida() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setImg_perfil(PNG_1X1);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.migrar(PNG_1X1)).thenThrow(new IllegalStateException("bucket indisponível"));

        assertThrows(IllegalStateException.class, () -> executor.migrarUsuarios(true));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void migrarUsuarios_simulacao_falhaNoBannerEhRegistradaComONomeDoCampo() {
        String invalida = "data:image/png;base64,imagem-quebrada";

        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setImg_perfil(PNG_1X1);
        usuario.setBanner_perfil(invalida);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.precisaMigrar(invalida)).thenReturn(true);
        doNothing().when(imagemService).validar(PNG_1X1);
        doThrow(new IllegalArgumentException("Data URI inválida")).when(imagemService).validar(invalida);

        MigracaoRelatorio relatorio = executor.migrarUsuarios(false);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(1, relatorio.getFalhas().size());
        assertEquals("usuario 5 / banner_perfil: Data URI inválida", relatorio.getFalhas().get(0));
        verify(imagemService, never()).migrar(any());
        verify(usuarioRepository, never()).save(any());
    }
}
