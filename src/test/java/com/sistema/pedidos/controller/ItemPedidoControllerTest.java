package com.sistema.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.service.ItemPedidoService;
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

@WebMvcTest(ItemPedidoController.class)
public class ItemPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemPedidoService itemPedidoService;

    private ItemPedido itemPedido;
    private Pedido pedido;
    private Produto produto;

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
    void testListarTodos() throws Exception {
        // Arrange
        when(itemPedidoService.buscarTodos()).thenReturn(Arrays.asList(itemPedido));

        // Act & Assert
        mockMvc.perform(get("/api/itens-pedido"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].quantidade", is(1)));

        verify(itemPedidoService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("Deve retornar item de pedido por ID")
    void testBuscarPorId() throws Exception {
        // Arrange
        when(itemPedidoService.buscarPorId(1L)).thenReturn(itemPedido);

        // Act & Assert
        mockMvc.perform(get("/api/itens-pedido/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantidade", is(1)))
                .andExpect(jsonPath("$.precoUnitario", is(99.90)));

        verify(itemPedidoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando item não encontrado")
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(itemPedidoService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("ItemPedido", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/itens-pedido/99"))
                .andExpect(status().isNotFound());

        verify(itemPedidoService, times(1)).buscarPorId(99L);
    }

    @Test
    @DisplayName("Deve retornar itens por pedido")
    void testBuscarPorPedido() throws Exception {
        // Arrange
        when(itemPedidoService.buscarPorPedido(1L)).thenReturn(Collections.singletonList(itemPedido));

        // Act & Assert
        mockMvc.perform(get("/api/itens-pedido/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(itemPedidoService, times(1)).buscarPorPedido(1L);
    }

    @Test
    @DisplayName("Deve atualizar um item de pedido")
    void testAtualizar() throws Exception {
        // Arrange
        ItemPedido itemAtualizado = new ItemPedido();
        itemAtualizado.setQuantidade(3);

        when(itemPedidoService.atualizar(eq(1L), any(ItemPedido.class))).thenAnswer(invocation -> {
            ItemPedido item = new ItemPedido(1L, pedido, produto, 3, new BigDecimal("99.90"));
            item.calcularValorTotal();
            return item;
        });

        // Act & Assert
        mockMvc.perform(put("/api/itens-pedido/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantidade", is(3)));

        verify(itemPedidoService, times(1)).atualizar(eq(1L), any(ItemPedido.class));
    }

    @Test
    @DisplayName("Deve excluir um item de pedido")
    void testExcluir() throws Exception {
        // Arrange
        doNothing().when(itemPedidoService).excluir(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/itens-pedido/1"))
                .andExpect(status().isNoContent());

        verify(itemPedidoService, times(1)).excluir(1L);
    }
}