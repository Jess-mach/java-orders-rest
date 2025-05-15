package com.sistema.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private Pedido pedido;
    private LocalDateTime dataPedido;

    @BeforeEach
    void setUp() {
        dataPedido = LocalDateTime.now();

        // Configurando o produto
        Produto produto = new Produto(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);

        // Configurando o pedido
        pedido = new Pedido(1L, "Cliente Teste", dataPedido, "Observação teste",
                new BigDecimal("99.90"), Pedido.StatusPedido.PENDENTE);

        // Configurando o item do pedido
        ItemPedido itemPedido = new ItemPedido(1L, pedido, produto, 1, new BigDecimal("99.90"));
        itemPedido.calcularValorTotal();

        // Adicionando o item ao pedido
        pedido.getItens().add(itemPedido);
    }

    @Test
    @DisplayName("Deve retornar todos os pedidos")
    void testListarTodos() throws Exception {
        // Arrange
        when(pedidoService.buscarTodos()).thenReturn(Arrays.asList(pedido));

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
        when(pedidoService.buscarPorId(1L)).thenReturn(pedido);

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
        when(pedidoService.buscarPorCliente("Cliente")).thenReturn(Collections.singletonList(pedido));

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
        Pedido novoPedido = new Pedido();
        novoPedido.setCliente("Novo Cliente");
        novoPedido.setObservacao("Nova observação");

        Produto produtoExistente = new Produto(1L, "Produto", "Descrição", new BigDecimal("10.00"), 20);
        ItemPedido novoItem = new ItemPedido();
        novoItem.setProduto(produtoExistente);
        novoItem.setQuantidade(2);

        novoPedido.getItens().add(novoItem);

        when(pedidoService.salvar(any(Pedido.class))).thenReturn(pedido);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoPedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cliente", is("Cliente Teste")))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].quantidade", is(1)));

        verify(pedidoService, times(1)).salvar(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void testAtualizarStatus() throws Exception {
        // Arrange
        Pedido pedidoAtualizado = new Pedido(1L, "Cliente Teste", dataPedido, "Observação teste",
                new BigDecimal("99.90"), Pedido.StatusPedido.APROVADO);
        pedidoAtualizado.getItens().add(pedido.getItens().get(0));

        when(pedidoService.atualizarStatus(eq(1L), eq(Pedido.StatusPedido.APROVADO))).thenReturn(pedidoAtualizado);

        // Act & Assert
        mockMvc.perform(patch("/api/pedidos/1/status")
                        .param("status", "APROVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APROVADO")))
                .andExpect(jsonPath("$.itens", hasSize(1)));

        verify(pedidoService, times(1)).atualizarStatus(eq(1L), eq(Pedido.StatusPedido.APROVADO));
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