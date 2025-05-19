package com.sistema.pedidos.repository;

import com.sistema.pedidos.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByClienteContainingIgnoreCase(String cliente);
    List<PedidoEntity> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<PedidoEntity> findByStatus(PedidoEntity.StatusPedido status);
}
