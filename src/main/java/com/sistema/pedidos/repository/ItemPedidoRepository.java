package com.sistema.pedidos.repository;

import com.sistema.pedidos.entity.ItemPedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedidoEntity, Long> {
    List<ItemPedidoEntity> findByPedidoId(Long pedidoId);
    List<ItemPedidoEntity> findByProdutoId(Long produtoId);
}
