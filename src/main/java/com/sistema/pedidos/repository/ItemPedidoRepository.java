package com.sistema.pedidos.repository;

import com.sistema.pedidos.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    List<ItemPedido> findByPedidoId(Long pedidoId);
    List<ItemPedido> findByProdutoId(Long produtoId);
}
