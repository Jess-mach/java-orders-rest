package com.sistema.pedidos.service;

import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoEntityServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoEntity produtoEntity;

    @BeforeEach
    void setUp() {
        produtoEntity = new ProdutoEntity(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);
    }

    @Test
    @DisplayName("Deve retornar todos os produtos")
    void testBuscarTodos() {
        // Arrange
        List<ProdutoEntity> produtosEsperados = Arrays.asList(
                produtoEntity,
                new ProdutoEntity(2L, "Outro Produto", "Outra descrição", new BigDecimal("49.90"), 5)
        );

        when(produtoRepository.findAll()).thenReturn(produtosEsperados);

        // Act
        List<ProdutoEntity> produtosRetornados = produtoService.buscarTodos();

        // Assert
        assertEquals(produtosEsperados.size(), produtosRetornados.size());
        assertEquals(produtosEsperados, produtosRetornados);
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar produto por ID")
    void testBuscarPorId() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntity));

        // Act
        ProdutoEntity produtoEntityRetornado = produtoService.buscarPorId(1L);

        // Assert
        assertNotNull(produtoEntityRetornado);
        assertEquals(produtoEntity.getId(), produtoEntityRetornado.getId());
        assertEquals(produtoEntity.getNome(), produtoEntityRetornado.getNome());
        verify(produtoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto por ID inexistente")
    void testBuscarPorIdNaoExistente() {
        // Arrange
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> produtoService.buscarPorId(99L));
        verify(produtoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve retornar produtos por nome")
    void testBuscarPorNome() {
        // Arrange
        List<ProdutoEntity> produtosEsperados = Arrays.asList(produtoEntity);
        when(produtoRepository.findByNomeContainingIgnoreCase("Teste")).thenReturn(produtosEsperados);

        // Act
        List<ProdutoEntity> produtosRetornados = produtoService.buscarPorNome("Teste");

        // Assert
        assertEquals(produtosEsperados.size(), produtosRetornados.size());
        assertEquals(produtosEsperados, produtosRetornados);
        verify(produtoRepository, times(1)).findByNomeContainingIgnoreCase("Teste");
    }

    @Test
    @DisplayName("Deve salvar um produto")
    void testSalvar() {
        // Arrange
        ProdutoEntity novoProdutoEntity = new ProdutoEntity("Novo Produto", "Nova descrição", new BigDecimal("29.90"), 20);
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(
                new ProdutoEntity(3L, "Novo Produto", "Nova descrição", new BigDecimal("29.90"), 20));

        // Act
        ProdutoEntity produtoEntitySalvo = produtoService.salvar(novoProdutoEntity);

        // Assert
        assertNotNull(produtoEntitySalvo);
        assertEquals(3L, produtoEntitySalvo.getId());
        assertEquals(novoProdutoEntity.getNome(), produtoEntitySalvo.getNome());
        verify(produtoRepository, times(1)).save(novoProdutoEntity);
    }

    @Test
    @DisplayName("Deve atualizar um produto")
    void testAtualizar() {
        // Arrange
        ProdutoEntity produtoEntityAtualizado = new ProdutoEntity("Produto Atualizado", "Descrição atualizada", new BigDecimal("109.90"), 15);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntity));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenAnswer(invocation -> {
            ProdutoEntity p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Act
        ProdutoEntity resultado = produtoService.atualizar(1L, produtoEntityAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(produtoEntityAtualizado.getNome(), resultado.getNome());
        assertEquals(produtoEntityAtualizado.getDescricao(), resultado.getDescricao());
        assertEquals(produtoEntityAtualizado.getPreco(), resultado.getPreco());
        assertEquals(produtoEntityAtualizado.getQuantidadeEstoque(), resultado.getQuantidadeEstoque());

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve excluir um produto")
    void testExcluir() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntity));
        doNothing().when(produtoRepository).delete(produtoEntity);

        // Act
        produtoService.excluir(1L);

        // Assert
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).delete(produtoEntity);
    }

    @Test
    @DisplayName("Deve atualizar o estoque de um produto")
    void testAtualizarEstoque() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntity));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        produtoService.atualizarEstoque(1L, 3);

        // Assert
        assertEquals(7, produtoEntity.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(produtoEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar reduzir estoque para quantidade negativa")
    void testAtualizarEstoqueQuantidadeInsuficiente() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntity));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> produtoService.atualizarEstoque(1L, 15));
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).save(any(ProdutoEntity.class));
    }
}