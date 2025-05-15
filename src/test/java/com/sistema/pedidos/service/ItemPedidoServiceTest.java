package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.repository.ItemPedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemPedidoServiceTest {

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ItemPedidoService itemPedidoService;

    private Pedido pedido;
    private Produto produto;
    private ItemPedido itemPedido;

    @BeforeEach
    void setUp() {
        // Configurando o produto
        produto = new Produto(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);

        // Configurando o pedido
        pedido = new Pedido(1L, "Cliente Teste", LocalDateTime.now(), "Observação teste",
                new BigDecimal("99.90"), Pedido.StatusPedido.PENDENTE);

        // Configurando o item do pedido
        itemPedido = new ItemPedido(1L, pedido, produto, 1, new BigDecimal("99.90"));
        itemPedido.calcularValorTotal();

        // Adicionando o item ao pedido
        pedido.getItens().add(itemPedido);
    }

    @Test
    @DisplayName("Deve retornar todos os itens de pedido")
    void testBuscarTodos() {
        // Arrange
        List<ItemPedido> itensEsperados = Arrays.asList(itemPedido);
        when(itemPedidoRepository.findAll()).thenReturn(itensEsperados);

        // Act
        List<ItemPedido> itensRetornados = itemPedidoService.buscarTodos();

        // Assert
        assertEquals(itensEsperados.size(), itensRetornados.size());
        assertEquals(itensEsperados, itensRetornados);
        verify(itemPedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar item de pedido por ID")
    void testBuscarPorId() {
        // Arrange
        when(itemPedidoRepository.findById(1L)).thenReturn(Optional.of(itemPedido));

        // Act
        ItemPedido itemRetornado = itemPedidoService.buscarPorId(1L);

        // Assert
        assertNotNull(itemRetornado);
        assertEquals(itemPedido.getId(), itemRetornado.getId());
        verify(itemPedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar item por ID inexistente")
    void testBuscarPorIdNaoExistente() {
        // Arrange
        when(itemPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> itemPedidoService.buscarPorId(99L));
        verify(itemPedidoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve retornar itens por pedido")
    void testBuscarPorPedido() {
        // Arrange
        List<ItemPedido> itensEsperados = Arrays.asList(itemPedido);
        when(itemPedidoRepository.findByPedidoId(1L)).thenReturn(itensEsperados);

        // Act
        List<ItemPedido> itensRetornados = itemPedidoService.buscarPorPedido(1L);

        // Assert
        assertEquals(itensEsperados.size(), itensRetornados.size());
        assertEquals(itensEsperados, itensRetornados);
        verify(itemPedidoRepository, times(1)).findByPedidoId(1L);
    }

    @Test
    @DisplayName("Deve salvar um item de pedido")
    void testSalvar() {
        // Arrange
        ItemPedido novoItem = new ItemPedido();
        novoItem.setProduto(produto);
        novoItem.setQuantidade(2);

        when(produtoService.buscarPorId(1L)).thenReturn(produto);
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenReturn(itemPedido);

        // Act
        ItemPedido itemSalvo = itemPedidoService.salvar(novoItem);

        // Assert
        assertNotNull(itemSalvo);
        assertEquals(itemPedido.getId(), itemSalvo.getId());
        verify(produtoService, times(1)).buscarPorId(1L);
        verify(itemPedidoRepository, times(1)).save(novoItem);
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar item sem produto")
    void testSalvarItemSemProduto() {
        // Arrange
        ItemPedido novoItem = new ItemPedido();
        novoItem.setQuantidade(2);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> itemPedidoService.salvar(novoItem));
        verify(itemPedidoRepository, never()).save(any(ItemPedido.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar item com quantidade inválida")
    void testSalvarItemQuantidadeInvalida() {
        // Arrange
        ItemPedido novoItem = new ItemPedido();
        novoItem.setProduto(produto);
        novoItem.setQuantidade(0);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> itemPedidoService.salvar(novoItem));
        verify(itemPedidoRepository, never()).save(any(ItemPedido.class));
    }

    @Test
    @DisplayName("Deve atualizar a quantidade de um item")
    void testAtualizar() {
        // Arrange
        ItemPedido itemAtualizado = new ItemPedido();
        itemAtualizado.setQuantidade(3);

        when(itemPedidoRepository.findById(1L)).thenReturn(Optional.of(itemPedido));
        when(itemPedidoRepository.save(any(ItemPedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemPedido resultado = itemPedidoService.atualizar(1L, itemAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.getQuantidade());
        verify(itemPedidoRepository, times(1)).findById(1L);
        verify(itemPedidoRepository, times(1)).save(itemPedido);
    }

    @Test
    @DisplayName("Deve excluir um item de pedido pendente")
    void testExcluir() {
        // Arrange
        when(itemPedidoRepository.findById(1L)).thenReturn(Optional.of(itemPedido));
        doNothing().when(itemPedidoRepository).delete(itemPedido);

        // Act
        itemPedidoService.excluir(1L);

        // Assert
        verify(itemPedidoRepository, times(1)).findById(1L);
        verify(itemPedidoRepository, times(1)).delete(itemPedido);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir item de pedido não pendente")
    void testExcluirItemPedidoNaoPendente() {
        // Arrange
        pedido.setStatus(Pedido.StatusPedido.APROVADO);
        when(itemPedidoRepository.findById(1L)).thenReturn(Optional.of(itemPedido));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> itemPedidoService.excluir(1L));
        verify(itemPedidoRepository, times(1)).findById(1L);
        verify(itemPedidoRepository, never()).delete(any(ItemPedido.class));
    }
}
