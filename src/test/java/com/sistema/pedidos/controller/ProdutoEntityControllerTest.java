package com.sistema.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
public class ProdutoEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoService produtoService;

    private ProdutoEntity produtoEntity;

    @BeforeEach
    void setUp() {
        produtoEntity = new ProdutoEntity(1L, "Produto Teste", "Descrição teste", new BigDecimal("99.90"), 10);
    }

    @Test
    @DisplayName("Deve retornar todos os produtos")
    void testListarTodos() throws Exception {
        // Arrange
        List<ProdutoEntity> produtoEntities = Arrays.asList(
                produtoEntity,
                new ProdutoEntity(2L, "Outro Produto", "Outra descrição", new BigDecimal("49.90"), 5)
        );
        when(produtoService.buscarTodos()).thenReturn(produtoEntities);

        // Act & Assert
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nome", is("Produto Teste")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].nome", is("Outro Produto")));

        verify(produtoService, times(1)).buscarTodos();
    }

    @Test
    @DisplayName("Deve retornar produto por ID")
    void testBuscarPorId() throws Exception {
        // Arrange
        when(produtoService.buscarPorId(1L)).thenReturn(produtoEntity);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Produto Teste")))
                .andExpect(jsonPath("$.preco", is(99.90)));

        verify(produtoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando produto não encontrado")
    void testBuscarPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(produtoService.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Produto", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/produtos/99"))
                .andExpect(status().isNotFound());

        verify(produtoService, times(1)).buscarPorId(99L);
    }

    @Test
    @DisplayName("Deve retornar produtos por nome")
    void testBuscarPorNome() throws Exception {
        // Arrange
        List<ProdutoEntity> produtoEntities = Collections.singletonList(produtoEntity);
        when(produtoService.buscarPorNome("Teste")).thenReturn(produtoEntities);

        // Act & Assert
        mockMvc.perform(get("/api/produtos/buscar").param("nome", "Teste"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nome", is("Produto Teste")));

        verify(produtoService, times(1)).buscarPorNome("Teste");
    }

    @Test
    @DisplayName("Deve criar um novo produto")
    void testCriar() throws Exception {
        // Arrange
        ProdutoEntity novoProdutoEntity = new ProdutoEntity("Novo Produto", "Nova descrição", new BigDecimal("29.90"), 20);
        when(produtoService.salvar(any(ProdutoEntity.class))).thenReturn(
                new ProdutoEntity(3L, "Novo Produto", "Nova descrição", new BigDecimal("29.90"), 20));

        // Act & Assert
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProdutoEntity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nome", is("Novo Produto")))
                .andExpect(jsonPath("$.preco", is(29.90)));

        verify(produtoService, times(1)).salvar(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve atualizar um produto existente")
    void testAtualizar() throws Exception {
        // Arrange
        ProdutoEntity produtoEntityAtualizado = new ProdutoEntity("Produto Atualizado", "Descrição atualizada", new BigDecimal("109.90"), 15);
        when(produtoService.atualizar(eq(1L), any(ProdutoEntity.class))).thenReturn(
                new ProdutoEntity(1L, "Produto Atualizado", "Descrição atualizada", new BigDecimal("109.90"), 15));

        // Act & Assert
        mockMvc.perform(put("/api/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoEntityAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Produto Atualizado")))
                .andExpect(jsonPath("$.preco", is(109.90)));

        verify(produtoService, times(1)).atualizar(eq(1L), any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("Deve excluir um produto")
    void testExcluir() throws Exception {
        // Arrange
        doNothing().when(produtoService).excluir(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoService, times(1)).excluir(1L);
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios ao criar produto")
    void testCriarProdutoInvalido() throws Exception {
        // Arrange
        ProdutoEntity produtoEntityInvalido = new ProdutoEntity();
        produtoEntityInvalido.setDescricao("Apenas descrição");

        // Act & Assert
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoEntityInvalido)))
                .andExpect(status().isBadRequest());

        verify(produtoService, never()).salvar(any(ProdutoEntity.class));
    }
}
