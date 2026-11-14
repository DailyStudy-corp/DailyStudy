package com.dailystudy.backend.migration;

import com.dailystudy.backend.model.Post;
import com.dailystudy.backend.repository.PostRepository;
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
public class MigracaoPostExecutorTest {

    private static final String PNG_1X1 =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Mock
    PostRepository postRepository;

    @Mock
    ImagemMigracaoService imagemService;

    @InjectMocks
    MigracaoPostExecutor executor;

    @Test
    void migrarPosts_aplicar_sobeEGravaAUrlNova() {
        String urlNova = "https://exemplo.test/o/xyz";
        Post post = new Post();
        post.setId("abc123");
        post.setMediaUrl(PNG_1X1);

        AtomicReference<String> mediaNoSave = new AtomicReference<>();
        when(postRepository.findAll()).thenReturn(List.of(post));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.migrar(PNG_1X1)).thenReturn(Optional.of(urlNova));
        when(postRepository.save(any(Post.class))).thenAnswer(invocacao -> {
            Post salvo = invocacao.getArgument(0);
            mediaNoSave.set(salvo.getMediaUrl());
            return salvo;
        });

        MigracaoRelatorio relatorio = executor.migrarPosts(true);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(urlNova, mediaNoSave.get());
        verify(postRepository).save(post);
        verify(imagemService, never()).validar(any());
    }

    @Test
    void migrarPosts_simulacao_validaMasNaoSobeNemGrava() {
        Post comImagem = new Post();
        comImagem.setId("p1");
        comImagem.setMediaUrl(PNG_1X1);

        Post semImagem = new Post();
        semImagem.setId("p2");

        when(postRepository.findAll()).thenReturn(List.of(comImagem, semImagem));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.precisaMigrar(null)).thenReturn(false);

        MigracaoRelatorio relatorio = executor.migrarPosts(false);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(1, relatorio.getIgnorados());
        verify(imagemService).validar(PNG_1X1);
        verify(imagemService, never()).migrar(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void migrarPosts_aplicar_falhaEmUmPostNaoInterrompeOsDemais() {
        String invalida = "data:image/png;base64,imagem-quebrada";
        String urlNova = "https://exemplo.test/o/xyz";

        Post comProblema = new Post();
        comProblema.setId("p1");
        comProblema.setMediaUrl(invalida);

        Post saudavel = new Post();
        saudavel.setId("p2");
        saudavel.setMediaUrl(PNG_1X1);

        when(postRepository.findAll()).thenReturn(List.of(comProblema, saudavel));
        when(imagemService.precisaMigrar(invalida)).thenReturn(true);
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.migrar(invalida)).thenThrow(new IllegalArgumentException("Data URI inválida"));
        when(imagemService.migrar(PNG_1X1)).thenReturn(Optional.of(urlNova));

        MigracaoRelatorio relatorio = executor.migrarPosts(true);

        assertEquals(1, relatorio.getMigrados());
        assertEquals(1, relatorio.getFalhas().size());
        assertEquals("post p1 / mediaUrl: Data URI inválida", relatorio.getFalhas().get(0));
        assertEquals(invalida, comProblema.getMediaUrl());
        assertEquals(urlNova, saudavel.getMediaUrl());
        verify(postRepository, never()).save(comProblema);
        verify(postRepository).save(saudavel);
    }

    @Test
    void migrarPosts_aplicar_falhaDeUploadNaoEhEngolida() {
        Post post = new Post();
        post.setId("p1");
        post.setMediaUrl(PNG_1X1);

        when(postRepository.findAll()).thenReturn(List.of(post));
        when(imagemService.precisaMigrar(PNG_1X1)).thenReturn(true);
        when(imagemService.migrar(PNG_1X1)).thenThrow(new IllegalStateException("bucket indisponível"));

        assertThrows(IllegalStateException.class, () -> executor.migrarPosts(true));

        verify(postRepository, never()).save(any());
    }
}
