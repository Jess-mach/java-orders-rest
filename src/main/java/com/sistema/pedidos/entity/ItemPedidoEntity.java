package com.sistema.pedidos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "itens_pedido")
public class ItemPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "pedido_id")
    private Long pedidoId;

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull(message = "O preço unitário é obrigatório")
    @Positive(message = "O preço unitário deve ser maior que zero")
    @Column(nullable = false)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    // Construtores
    public ItemPedidoEntity() {
        this.quantidade = 0;
        this.precoUnitario = BigDecimal.ZERO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public ItemPedidoEntity(ProdutoEntity produtoEntity, Integer quantidade) {
        this();
        this.produto = produtoEntity;
        this.quantidade = quantidade;
        this.precoUnitario = produtoEntity.getPreco();
        this.calcularValorTotal();
    }

    public ItemPedidoEntity(Long id, PedidoEntity pedidoEntity, ProdutoEntity produtoEntity, Integer quantidade, BigDecimal precoUnitario) {
        this.id = id;
        this.produto = produtoEntity;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.calcularValorTotal();
    }

    // Método para calcular o valor total do item
    public void calcularValorTotal() {
        if (quantidade != null && precoUnitario != null) {
            valorTotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            valorTotal = BigDecimal.ZERO;
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProdutoEntity getProduto() {
        return produto;
    }

    public void setProduto(ProdutoEntity produtoEntity) {
        this.produto = produtoEntity;
        if (produtoEntity != null) {
            this.precoUnitario = produtoEntity.getPreco();
            calcularValorTotal();
        }
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        calcularValorTotal();
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        calcularValorTotal();
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    // Equals, HashCode e ToString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedidoEntity that = (ItemPedidoEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "id=" + id +
                ", produto=" + (produto != null ? produto.getNome() : null) +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
