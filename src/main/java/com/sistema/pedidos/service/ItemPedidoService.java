package com.sistema.pedidos.service;

import com.sistema.pedidos.entity.ItemPedidoEntity;
import com.sistema.pedidos.entity.PedidoEntity;
import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.entity.ProdutoEntity;
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
    public List<ItemPedidoEntity> buscarTodos() {
        return itemPedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ItemPedidoEntity buscarPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ItemPedidoEntity> buscarPorPedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }

    @Transactional(readOnly = true)
    public List<ItemPedidoEntity> buscarPorProduto(Long produtoId) {
        return itemPedidoRepository.findByProdutoId(produtoId);
    }

    @Transactional
    public ItemPedidoEntity salvar(ItemPedidoEntity itemPedidoEntity) {
        if (itemPedidoEntity.getProduto() == null || itemPedidoEntity.getProduto().getId() == null) {
            throw new BadRequestException("Produto não informado");
        }

        if (itemPedidoEntity.getQuantidade() <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }

        // Busca o produto para confirmar existência e obter preço
        ProdutoEntity produtoEntity = produtoService.buscarPorId(itemPedidoEntity.getProduto().getId());
        itemPedidoEntity.setProduto(produtoEntity);
        itemPedidoEntity.setPrecoUnitario(produtoEntity.getPreco());
        itemPedidoEntity.calcularValorTotal();

        return itemPedidoRepository.save(itemPedidoEntity);
    }

//    @Transactional //TODO:Remover codigo posteriormente caso não utilize
//    public ItemPedidoEntity atualizar(Long id, ItemPedidoEntity itemPedidoEntityAtualizado) {
//        ItemPedidoEntity itemExistente = buscarPorId(id);
//
//        // Não permite alterar o pedido ou o produto, apenas a quantidade
//        if (itemPedidoEntityAtualizado.getQuantidade() <= 0) {
//            throw new BadRequestException("A quantidade deve ser maior que zero");
//        }
//
//        // Atualiza a quantidade e recalcula o valor total
//        itemExistente.setQuantidade(itemPedidoEntityAtualizado.getQuantidade());
//        itemExistente.calcularValorTotal();
//
//        // Se o pedido está no status PENDENTE, recalcula o valor total do pedido
//        PedidoEntity pedidoEntity = itemExistente.getPedido();
//        if (pedidoEntity != null && pedidoEntity.getStatus() == PedidoEntity.StatusPedido.PENDENTE) {
//            pedidoEntity.recalcularValorTotal();
//        }
//
//        return itemPedidoRepository.save(itemExistente);
//    }
//
//    @Transactional
//    public void excluir(Long id) {
//        ItemPedidoEntity item = buscarPorId(id);
//
//        // Apenas permite excluir se o pedido estiver com status PENDENTE
//        PedidoEntity pedidoEntity = item.getPedido();
//        if (pedidoEntity != null && pedidoEntity.getStatus() != PedidoEntity.StatusPedido.PENDENTE) {
//            throw new BadRequestException("Não é possível excluir itens de um pedido que não esteja com status PENDENTE");
//        }
//
//        // Remove o item do pedido e recalcula o valor total
//        if (pedidoEntity != null) {
//            pedidoEntity.removerItem(item);
//        }
//
//        itemPedidoRepository.delete(item);
//    }
}
