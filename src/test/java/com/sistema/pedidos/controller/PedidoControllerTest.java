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
            .andExpect(jsonPath("$[0].cliente", is("Cliente Teste")));

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
            .andExpect(jsonPath("$.status", is("PENDENTE")));

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
            .andExpect(jsonPath("$[0].cliente", is("Cliente Teste")));

    verify(pedidoService, times(1)).buscarPorCliente("Cliente");
}

@Test
@DisplayName("Deve criar um novo pedido")
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
            .andExpect(jsonPath("$.status", is("PENDENTE")));

    verify(pedidoService, times(1)).salvar(any(Pedido.class));
}

@Test
@DisplayName("Deve atualizar o status de um pedido")
void testAtualizarStatus() throws Exception {
    // Arrange
    Pedido pedidoAtualizado = new Pedido(1L, "Cliente Teste", dataPedido, "Observação teste",
            new BigDecimal("99.90"), Pedido.StatusPedido.APROVADO);

    when(pedidoService.atualizarStatus(eq(1L), eq(Pedido.StatusPedido.APROVADO))).thenReturn(pedidoAtualizado);

    // Act & Assert
    mockMvc.perform(patch("/api/pedidos/1/status")
                    .param("status", "APROVADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("APROVADO")));

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