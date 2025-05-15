package com.sistema.pedidos.repository;

import com.sistema.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteContainingIgnoreCase(String cliente);
    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Pedido> findByStatus(Pedido.StatusPedido status);
}
