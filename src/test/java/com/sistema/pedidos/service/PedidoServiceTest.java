package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private PedidoService pedidoService;

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
    @DisplayName("Deve retornar todos os pedidos")
    void testBuscarTodos() {
        // Arrange
        List<Pedido> pedidosEsperados = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidosEsperados);

        // Act
        List<Pedido> pedidosRetornados = pedidoService.buscarTodos();

        // Assert
        assertEquals(pedidosEsperados.size(), pedidosRetornados.size());
        assertEquals(pedidosEsperados, pedidosRetornados);
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar pedido por ID")
    void testBuscarPorId() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        Pedido pedidoRetornado = pedidoService.buscarPorId(1L);

        // Assert
        assertNotNull(pedidoRetornado);
        assertEquals(pedido.getId(), pedidoRetornado.getId());
        assertEquals(pedido.getCliente(), pedidoRetornado.getCliente());
        assertEquals(1, pedidoRetornado.getItens().size());
        assertEquals(itemPedido.getId(), pedidoRetornado.getItens().get(0).getId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar pedido por ID inexistente")
    void testBuscarPorIdNaoExistente() {
        // Arrange
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> pedidoService.buscarPorId(99L));
        verify(pedidoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve retornar pedidos por cliente")
    void testBuscarPorCliente() {
        // Arrange
        List<Pedido> pedidosEsperados = Arrays.asList(pedido);
        when(pedidoRepository.findByClienteContainingIgnoreCase("Cliente")).thenReturn(pedidosEsperados);

        // Act
        List<Pedido> pedidosRetornados = pedidoService.buscarPorCliente("Cliente");

        // Assert
        assertEquals(pedidosEsperados.size(), pedidosRetornados.size());
        assertEquals(pedidosEsperados, pedidosRetornados);
        verify(pedidoRepository, times(1)).findByClienteContainingIgnoreCase("Cliente");
    }

    @Test
    @DisplayName("Deve salvar um pedido com seus itens")
    void testSalvar() {
        // Arrange
        Pedido novoPedido = new Pedido();
        novoPedido.setCliente("Novo Cliente");
        novoPedido.setObservacao("Nova observação");

        Produto produtoExistente = new Produto(1L, "Produto", "Descrição", new BigDecimal("10.00"), 20);
        ItemPedido novoItem = new ItemPedido();
        novoItem.setProduto(produtoExistente);
        novoItem.setQuantidade(2);

        novoPedido.getItens().add(novoItem);

        when(produtoService.buscarPorId(1L)).thenReturn(produtoExistente);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        Pedido pedidoSalvo = pedidoService.salvar(novoPedido);

        // Assert
        assertNotNull(pedidoSalvo);
        assertEquals(pedido.getId(), pedidoSalvo.getId());
        verify(pedidoRepository, times(1)).save(novoPedido);
        verify(produtoService, times(1)).buscarPorId(eq(1L));
        verify(produtoService, times(1)).atualizarEstoque(eq(1L), eq(2));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar pedido sem itens")
    void testSalvarPedidoSemItens() {
        // Arrange
        Pedido novoPedido = new Pedido();
        novoPedido.setCliente("Novo Cliente");
        novoPedido.setObservacao("Nova observação");
        novoPedido.setItens(new ArrayList<>());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> pedidoService.salvar(novoPedido));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve atualizar um pedido com novos itens")
    void testAtualizarComNovosItens() {
        // Arrange
        Pedido pedidoAtualizado = new Pedido();
        pedidoAtualizado.setCliente("Cliente Atualizado");
        pedidoAtualizado.setObservacao("Observação atualizada");

        Produto produtoExistente = new Produto(2L, "Produto 2", "Descrição 2", new BigDecimal("29.90"), 15);
        ItemPedido novoItem = new ItemPedido();
        novoItem.setProduto(produtoExistente);
        novoItem.setQuantidade(3);

        pedidoAtualizado.getItens().add(novoItem);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoService.buscarPorId(2L)).thenReturn(produtoExistente);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.atualizar(1L, pedidoAtualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals("Cliente Atualizado", resultado.getCliente());
        assertEquals("Observação atualizada", resultado.getObservacao());
        assertEquals(1, resultado.getItens().size());
        assertEquals(2L, resultado.getItens().get(0).getProduto().getId());
        assertEquals(3, resultado.getItens().get(0).getQuantidade());

        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoService, times(1)).buscarPorId(eq(2L));
        verify(produtoService, times(1)).salvar(eq(produto));
        verify(produtoService, times(1)).atualizarEstoque(eq(2L), eq(3));
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void testAtualizarStatus() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setStatus(Pedido.StatusPedido.APROVADO);
            return p;
        });

        // Act
        Pedido resultado = pedidoService.atualizarStatus(1L, Pedido.StatusPedido.APROVADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(Pedido.StatusPedido.APROVADO, resultado.getStatus());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar para status inválido")
    void testAtualizarStatusInvalido() {
        // Arrange
        pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                pedidoService.atualizarStatus(1L, Pedido.StatusPedido.PENDENTE));
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve excluir um pedido pendente e restaurar o estoque")
    void testExcluir() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).delete(pedido);

        // Act
        pedidoService.excluir(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).delete(pedido);
        // Verificar se o estoque foi restaurado
        verify(produtoService, times(1)).salvar(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir pedido não pendente")
    void testExcluirPedidoNaoPendente() {
        // Arrange
        pedido.setStatus(Pedido.StatusPedido.APROVADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> pedidoService.excluir(1L));
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).delete(any(Pedido.class));
    }
}