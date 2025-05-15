package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final ItemPedidoService itemPedidoService;

    @Autowired
    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoService produtoService,
            ItemPedidoService itemPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
        this.itemPedidoService = itemPedidoService;
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorCliente(String cliente) {
        return pedidoRepository.findByClienteContainingIgnoreCase(cliente);
    }

