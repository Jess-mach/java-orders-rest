package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoService produtoService;

    @Autowired
    public ItemPedidoService(
            ItemPedidoRepository itemPedidoRepository,
            ProdutoService produtoService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoService = produtoService;
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> buscarTodos() {
        return itemPedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ItemPedido buscarPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> buscarPorPedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> buscarPorProduto(Long produtoId) {
        return itemPedidoRepository.findByProdutoId(produtoId);
    }

    @Transactional
    public ItemPedido salvar(ItemPedido itemPedido) {
        if (itemPedido.getProduto() == null || itemPedido.getProduto().getId() == null) {
            throw new BadRequestException("Produto não informado");
        }

        if (itemPedido.getQuantidade() <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }

        // Busca o produto para confirmar existência e obter preço
        Produto produto = produtoService.buscarPorId(itemPedido.getProduto().getId());
        itemPedido.setProduto(produto);
        itemPedido.setPrecoUnitario(produto.getPreco());
        itemPedido.calcularValorTotal();

        return itemPedidoRepository.save(itemPedido);
    }

    @Transactional
    public ItemPedido atualizar(Long id, ItemPedido itemPedidoAtualizado) {
        ItemPedido itemExistente = buscarPorId(id);

        // Não permite alterar o pedido ou o produto, apenas a quantidade
        if (itemPedidoAtualizado.getQuantidade() <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }

        // Atualiza a quantidade e recalcula o valor total
        itemExistente.setQuantidade(itemPedidoAtualizado.getQuantidade());
        itemExistente.calcularValorTotal();

        // Se o pedido está no status PENDENTE, recalcula o valor total do pedido
        Pedido pedido = itemExistente.getPedido();
        if (pedido != null && pedido.getStatus() == Pedido.StatusPedido.PENDENTE) {
            pedido.recalcularValorTotal();
        }

        return itemPedidoRepository.save(itemExistente);
    }

    @Transactional
    public void excluir(Long id) {
        ItemPedido item = buscarPorId(id);

        // Apenas permite excluir se o pedido estiver com status PENDENTE
        Pedido pedido = item.getPedido();
        if (pedido != null && pedido.getStatus() != Pedido.StatusPedido.PENDENTE) {
            throw new BadRequestException("Não é possível excluir itens de um pedido que não esteja com status PENDENTE");
        }

        // Remove o item do pedido e recalcula o valor total
        if (pedido != null) {
            pedido.removerItem(item);
        }

        itemPedidoRepository.delete(item);
    }
}
