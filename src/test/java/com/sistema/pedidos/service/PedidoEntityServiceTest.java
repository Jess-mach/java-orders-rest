package com.sistema.pedidos.service;

import com.sistema.pedidos.entity.ItemPedidoEntity;
import com.sistema.pedidos.entity.PedidoEntity;
import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
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
public class PedidoEntityServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private ItemPedidoService itemPedidoService;

    @InjectMocks
    private PedidoService pedidoService;

    private PedidoEntity pedidoEntity;
    private ProdutoEntity produtoEntity;
    private ItemPedidoEntity itemPedidoEntity;

    @BeforeEach
    void setUp() {
        // Configurando o produto
        produtoEntity = new ProdutoEntity(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);

        // Configurando o pedido
        pedidoEntity = new PedidoEntity(1L, "Cliente Teste", LocalDateTime.now(), "Observação teste",
                new BigDecimal("99.90"), PedidoEntity.StatusPedido.PENDENTE);

        // Configurando o item do pedido
        itemPedidoEntity = new ItemPedidoEntity(1L, pedidoEntity, produtoEntity, 1, new BigDecimal("99.90"));
        itemPedidoEntity.calcularValorTotal();

        // Adicionando o item ao pedido
        pedidoEntity.getItens().add(itemPedidoEntity);
    }

    @Test
    @DisplayName("Deve retornar todos os pedidos")
    void testBuscarTodos() {
        // Arrange
        List<PedidoEntity> pedidosEsperados = Arrays.asList(pedidoEntity);
        when(pedidoRepository.findAll()).thenReturn(pedidosEsperados);

        // Act
        List<PedidoEntity> pedidosRetornados = pedidoService.buscarTodos();

        // Assert
        assertEquals(pedidosEsperados.size(), pedidosRetornados.size());
        assertEquals(pedidosEsperados, pedidosRetornados);
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar pedido por ID")
    void testBuscarPorId() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));

        // Act
        PedidoEntity pedidoEntityRetornado = pedidoService.buscarPorId(1L);

        // Assert
        assertNotNull(pedidoEntityRetornado);
        assertEquals(pedidoEntity.getId(), pedidoEntityRetornado.getId());
        assertEquals(pedidoEntity.getCliente(), pedidoEntityRetornado.getCliente());
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
        List<PedidoEntity> pedidosEsperados = Arrays.asList(pedidoEntity);
        when(pedidoRepository.findByClienteContainingIgnoreCase("Cliente")).thenReturn(pedidosEsperados);

        // Act
        List<PedidoEntity> pedidosRetornados = pedidoService.buscarPorCliente("Cliente");

        // Assert
        assertEquals(pedidosEsperados.size(), pedidosRetornados.size());
        assertEquals(pedidosEsperados, pedidosRetornados);
        verify(pedidoRepository, times(1)).findByClienteContainingIgnoreCase("Cliente");
    }

    @Test
    @DisplayName("Deve salvar um pedido")
    void testSalvar() {
        // Arrange
        PedidoEntity novoPedidoEntity = new PedidoEntity();
        novoPedidoEntity.setCliente("Novo Cliente");
        novoPedidoEntity.setObservacao("Nova observação");

        ProdutoEntity produtoEntityExistente = new ProdutoEntity(1L, "Produto", "Descrição", new BigDecimal("10.00"), 20);
        ItemPedidoEntity novoItem = new ItemPedidoEntity();
        novoItem.setProduto(produtoEntityExistente);
        novoItem.setQuantidade(2);

        novoPedidoEntity.getItens().add(novoItem);

        when(produtoService.buscarPorId(1L)).thenReturn(produtoEntityExistente);
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(pedidoEntity);

        // Act
        PedidoEntity pedidoEntitySalvo = pedidoService.salvar(novoPedidoEntity);

        // Assert
        assertNotNull(pedidoEntitySalvo);
        assertEquals(pedidoEntity.getId(), pedidoEntitySalvo.getId());
        verify(pedidoRepository, times(1)).save(novoPedidoEntity);
        verify(produtoService, times(1)).atualizarEstoque(eq(1L), eq(2));
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar pedido sem itens")
    void testSalvarPedidoSemItens() {
        // Arrange
        PedidoEntity novoPedidoEntity = new PedidoEntity();
        novoPedidoEntity.setCliente("Novo Cliente");
        novoPedidoEntity.setObservacao("Nova observação");
        novoPedidoEntity.setItens(new ArrayList<>());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> pedidoService.salvar(novoPedidoEntity));
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void testAtualizarStatus() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PedidoEntity resultado = pedidoService.atualizarStatus(1L, PedidoEntity.StatusPedido.APROVADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(PedidoEntity.StatusPedido.APROVADO, resultado.getStatus());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(pedidoEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar para status inválido")
    void testAtualizarStatusInvalido() {
        // Arrange
        pedidoEntity.setStatus(PedidoEntity.StatusPedido.ENTREGUE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                pedidoService.atualizarStatus(1L, PedidoEntity.StatusPedido.PENDENTE));
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("Deve excluir um pedido pendente")
    void testExcluir() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));
        doNothing().when(pedidoRepository).delete(pedidoEntity);

        // Act
        pedidoService.excluir(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).delete(pedidoEntity);
        // Verificar se o estoque foi restaurado
        verify(produtoService, times(1)).salvar(produtoEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir pedido não pendente")
    void testExcluirPedidoNaoPendente() {
        // Arrange
        pedidoEntity.setStatus(PedidoEntity.StatusPedido.APROVADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEntity));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> pedidoService.excluir(1L));
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).delete(any(PedidoEntity.class));
    }
}

