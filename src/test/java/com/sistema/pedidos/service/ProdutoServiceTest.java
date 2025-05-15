package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.Produto;
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
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);
    }

    @Test
    @DisplayName("Deve retornar todos os produtos")
    void testBuscarTodos() {
        // Arrange
        List<Produto> produtosEsperados = Arrays.asList(
                produto,
                new Produto(2L, "Outro Produto", "Outra descrição", new BigDecimal("49.90"), 5)
        );

        when(produtoRepository.findAll()).thenReturn(produtosEsperados);

        // Act
        List<Produto> produtosRetornados = produtoService.buscarTodos();

        // Assert
        assertEquals(produtosEsperados.size(), produtosRetornados.size());
        assertEquals(produtosEsperados, produtosRetornados);
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar produto por ID")
    void testBuscarPorId() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act
        Produto produtoRetornado = produtoService.buscarPorId(1L);

        // Assert
        assertNotNull(produtoRetornado);
        assertEquals(produto.getId(), produtoRetornado.getId());
        assertEquals(produto.getNome(), produtoRetornado.getNome());
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
        List<Produto> produtosEsperados = Arrays.asList(produto);
        when(produtoRepository.findByNomeContainingIgnoreCase("Teste")).thenReturn(produtosEsperados);

        // Act
        List<Produto> produtosRetornados = produtoService.buscarPorNome("Teste");

        // Assert
        assertEquals(produtosEsperados.size(), produtosRetornados.size());
        assertEquals(produtosEsperados, produtosRetornados);
        verify(produtoRepository, times(1)).findByNomeContainingIgnoreCase("Teste");
    }

    @Test
    @DisplayName("Deve salvar um produto")
    void testSalvar() {
        // Arrange
        Produto novoProduto = new Produto("Novo Produto", "Nova descrição", new BigDecimal("29.90"), 20);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        Produto produtoSalvo = produtoService.salvar(novoProduto);

        // Assert
        assertNotNull(produtoSalvo);
        assertEquals(produto.getId(), produtoSalvo.getId());
        assertEquals(produto.getNome(), produtoSalvo.getNome());
        verify(produtoRepository, times(1)).save(novoProduto);
    }

    @Test
    @DisplayName("Deve atualizar um produto")
    void testAtualizar() {
        // Arrange
        Produto produtoAtualizado = new Produto("Produto Atualizado", "Descrição atualizada", new BigDecimal("109.90"), 15);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Produto resultado = produtoService.atualizar(1L, produtoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(produtoAtualizado.getNome(), resultado.getNome());
        assertEquals(produtoAtualizado.getDescricao(), resultado.getDescricao());
        assertEquals(produtoAtualizado.getPreco(), resultado.getPreco());
        assertEquals(produtoAtualizado.getQuantidadeEstoque(), resultado.getQuantidadeEstoque());

        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve excluir um produto")
    void testExcluir() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        doNothing().when(produtoRepository).delete(produto);

        // Act
        produtoService.excluir(1L);

        // Assert
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).delete(produto);
    }

    @Test
    @DisplayName("Deve atualizar o estoque de um produto")
    void testAtualizarEstoque() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produto/**