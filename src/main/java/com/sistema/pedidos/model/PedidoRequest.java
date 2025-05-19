package com.sistema.pedidos.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoRequest {

    @NotBlank(message = "O nome do cliente é obrigatório")
    private String cliente;

    private String observacao;

    private BigDecimal valorTotal;

    private String status;

    private List<ItemPedidoRequest> itens;

    public PedidoRequest(
            String cliente, LocalDateTime dataPedido, String observacao, BigDecimal valorTotal,
            String status,
            List<ItemPedidoRequest> itens
    ) {
        this.cliente = cliente;
        this.observacao = observacao;
        this.valorTotal = valorTotal;
        this.status = status;
        this.itens = itens;
    }

    public String getCliente() {
        return cliente;
    }

    public String getObservacao() {
        return observacao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }
}
