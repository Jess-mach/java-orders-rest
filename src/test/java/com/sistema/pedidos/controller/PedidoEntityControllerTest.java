package com.sistema.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.pedidos.entity.ItemPedidoEntity;
import com.sistema.pedidos.entity.PedidoEntity;
import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
public class PedidoEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private PedidoEntity pedidoEntity;
    private LocalDateTime dataPedido;

    @BeforeEach
    void setUp() {
        dataPedido = LocalDateTime.now();

        // Configurando o produto
        ProdutoEntity produtoEntity = new ProdutoEntity(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);

        // Configurando o pedido
        pedidoEntity = new PedidoEntity(1L, "Cliente Teste", dataPedido, "Observação teste",
                new BigDecimal("99.90"), PedidoEntity.StatusPedido.PENDENTE);

        // Configurando o item do pedido
        ItemPedidoEntity itemPedidoEntity = new ItemPedidoEntity(1L, pedidoEntity, produtoEntity, 1, new BigDecimal("99.90"));
        itemPedidoEntity.calcularValorTotal();

        // Adicionando o item ao pedido
        pedidoEntity.getItens().add(itemPedidoEntity);
    }

    @Test
    @DisplayName("Deve retornar todos os pedidos")
    void testListarTodos() throws Exception {
        // Arrange
        when(pedidoService.buscarTodos()).thenReturn(Arrays.asList(pedidoEntity));

        // Act & Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].cliente", is("Cliente Teste")))
                .andExpect(jsonPath("$[0].itens", hasSize(1)))
                .andExpect(jsonPath("$[0].itens[0].quantidade", is(1)))
                .andExpect(jsonPath("$[0].itens[0].precoUnitario", is(99.90)));

        verify(pedidoService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("Deve retornar pedido por ID")
    void testBuscarPorId() throws Exception {
        // Arrange
        when(pedidoService.buscarPorId(1L)).thenReturn(pedidoEntity);

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cliente", is("Cliente Teste")))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].id", is(1)))
                .andExpect(jsonPath("$.itens[0].quantidade", is(1)))
                .andExpect(jsonPath("$.itens[0].produto.id", is(1)))
                .andExpect(jsonPath("$.itens[0].produto.nome", is("Produto Teste")));

        verify(pedidoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando pedido não encontrado")
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(pedidoService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Pedido", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/99"))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1)).buscarPorId(99L);
    }

    @Test
    @DisplayName("Deve retornar pedidos por cliente")
    void testBuscarPorCliente() throws Exception {
        // Arrange
        when(pedidoService.buscarPorCliente("Cliente")).thenReturn(Collections.singletonList(pedidoEntity));

        // Act & Assert
        mockMvc.perform(get("/api/pedidos/cliente").param("cliente", "Cliente"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].cliente", is("Cliente Teste")))
                .andExpect(jsonPath("$[0].itens", hasSize(1)))
                .andExpect(jsonPath("$[0].itens[0].quantidade", is(1)));

        verify(pedidoService, times(1)).buscarPorCliente("Cliente");
    }

    @Test
    @DisplayName("Deve criar um novo pedido com itens")
    void testCriar() throws Exception {
        // Arrange
        PedidoEntity novoPedidoEntity = new PedidoEntity();
        novoPedidoEntity.setCliente("Novo Cliente");
        novoPedidoEntity.setObservacao("Nova observação");

        ProdutoEntity produtoEntityExistente = new ProdutoEntity(1L, "Produto", "Descrição", new BigDecimal("10.00"), 20);
        ItemPedidoEntity novoItem = new ItemPedidoEntity();
        novoItem.setProduto(produtoEntityExistente);
        novoItem.setQuantidade(2);

        novoPedidoEntity.getItens().add(novoItem);

        when(pedidoService.salvar(any(PedidoEntity.class))).thenReturn(pedidoEntity);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoPedidoEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cliente", is("Cliente Teste")))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].quantidade", is(1)));

        verify(pedidoService, times(1)).salvar(any(PedidoEntity.class));
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void testAtualizarStatus() throws Exception {
        // Arrange
        PedidoEntity pedidoEntityAtualizado = new PedidoEntity(1L, "Cliente Teste", dataPedido, "Observação teste",
                new BigDecimal("99.90"), PedidoEntity.StatusPedido.APROVADO);
        pedidoEntityAtualizado.getItens().add(pedidoEntity.getItens().get(0));

        when(pedidoService.atualizarStatus(eq(1L), eq(PedidoEntity.StatusPedido.APROVADO))).thenReturn(pedidoEntityAtualizado);

        // Act & Assert
        mockMvc.perform(patch("/api/pedidos/1/status")
                        .param("status", "APROVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APROVADO")))
                .andExpect(jsonPath("$.itens", hasSize(1)));

        verify(pedidoService, times(1)).atualizarStatus(eq(1L), eq(PedidoEntity.StatusPedido.APROVADO));
    }

    @Test
    @DisplayName("Deve excluir um pedido")
    void testExcluir() throws Exception {
        // Arrange
        doNothing().when(pedidoService).excluir(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).excluir(1L);
    }
}