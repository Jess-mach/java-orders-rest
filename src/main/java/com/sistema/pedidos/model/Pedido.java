package com.sistema.pedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Column(nullable = false)
    private String cliente;

    @NotNull(message = "A data do pedido é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataPedido;

    @Column
    private String observacao;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public enum StatusPedido {
        PENDENTE, APROVADO, CANCELADO, ENTREGUE
    }

    // Construtores
    public Pedido() {
        this.dataPedido = LocalDateTime.now();
        this.valorTotal = BigDecimal.ZERO;
        this.status = StatusPedido.PENDENTE;
    }

    public Pedido(String cliente, String observacao) {
        this();
        this.cliente = cliente;
        this.observacao = observacao;
    }

    public Pedido(Long id, String cliente, LocalDateTime dataPedido, String observacao, BigDecimal valorTotal, StatusPedido status) {
        this.id = id;
        this.cliente = cliente;
        this.dataPedido = dataPedido;
        this.observacao = observacao;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    // Métodos para gerenciar os itens do pedido
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
        recalcularValorTotal();
    }

    public void removerItem(ItemPedido item) {
        itens.remove(item);
        item.setPedido(null);
        recalcularValorTotal();
    }

    public void recalcularValorTotal() {
        valorTotal = itens.stream()
                .map(ItemPedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        recalcularValorTotal();
    }

    // Equals, HashCode e ToString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente='" + cliente + '\'' +
                ", dataPedido=" + dataPedido +
                ", observacao='" + observacao + '\'' +
                ", valorTotal=" + valorTotal +
                ", status=" + status +
                '}';
    }
}
