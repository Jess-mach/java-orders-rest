package com.sistema.pedidos.service;

import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.model.ItemPedido;
import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.model.Produto;
import com.sistema.pedidos.repository.PedidoRepository;
import com.sistema.pedidos.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    @Autowired
    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
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

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataPedidoBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Pedido> buscarPorStatus(Pedido.StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    @Transactional
    public Pedido salvar(Pedido pedido) {
        if (pedido.getId() != null) {
            throw new BadRequestException("Não é permitido informar o ID ao criar um novo pedido");
        }

        validarItensPedido(pedido);

        // Define a data do pedido como agora se não for informada
        if (pedido.getDataPedido() == null) {
            pedido.setDataPedido(LocalDateTime.now());
        }

        // Define o status inicial como PENDENTE se não for informado
        if (pedido.getStatus() == null) {
            pedido.setStatus(Pedido.StatusPedido.PENDENTE);
        }

        // Vincula os itens ao pedido e configura os produtos
        pedido.getItens().forEach(item -> {
            item.setPedido(pedido);
        });

        // Salva o pedido com seus itens
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Atualiza o estoque dos produtos
        for (ItemPedido item : pedido.getItens()) {
            produtoService.atualizarEstoque(item.getProduto().getId(), item.getQuantidade());
        }

        return pedidoSalvo;
    }

    @Transactional
    public Pedido atualizar(Long id, Pedido pedidoAtualizado) {
        Pedido pedidoExistente = buscarPorId(id);

        // Só permite atualizar pedidos com status PENDENTE
        if (pedidoExistente.getStatus() != Pedido.StatusPedido.PENDENTE) {
            throw new BadRequestException("Não é possível atualizar um pedido que não esteja com status PENDENTE");
        }

        // Atualiza apenas os campos permitidos
        pedidoExistente.setCliente(pedidoAtualizado.getCliente());
        pedidoExistente.setObservacao(pedidoAtualizado.getObservacao());

        // Se o status está sendo alterado, verifica se a transição é válida
        if (pedidoAtualizado.getStatus() != null &&
                pedidoExistente.getStatus() != pedidoAtualizado.getStatus()) {
            validarAlteracaoStatus(pedidoExistente.getStatus(), pedidoAtualizado.getStatus());
            pedidoExistente.setStatus(pedidoAtualizado.getStatus());
        }

        // Se houver novos itens, validar e atualizar
        if (pedidoAtualizado.getItens() != null && !pedidoAtualizado.getItens().isEmpty()) {
            // Remove os itens antigos e restaura o estoque
            for (ItemPedido itemAntigo : pedidoExistente.getItens()) {
                Produto produto = itemAntigo.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + itemAntigo.getQuantidade());
                produtoService.salvar(produto);
            }

            // Limpa todos os itens atuais
            pedidoExistente.getItens().clear();

            // Adiciona os novos itens
            for (ItemPedido novoItem : pedidoAtualizado.getItens()) {
                Produto produto = produtoService.buscarPorId(novoItem.getProduto().getId());

                if (novoItem.getQuantidade() <= 0) {
                    throw new BadRequestException("A quantidade deve ser maior que zero");
                }

                if (novoItem.getQuantidade() > produto.getQuantidadeEstoque()) {
                    throw new BadRequestException("Quantidade insuficiente em estoque para o produto: " + produto.getNome());
                }

                // Configura o novo item
                ItemPedido item = new ItemPedido();
                item.setPedido(pedidoExistente);
                item.setProduto(produto);
                item.setQuantidade(novoItem.getQuantidade());
                item.setPrecoUnitario(produto.getPreco());
                item.calcularValorTotal();

                // Adiciona o item ao pedido
                pedidoExistente.getItens().add(item);

                // Atualiza o estoque
                produtoService.atualizarEstoque(produto.getId(), novoItem.getQuantidade());
            }
        }

        // Recalcula o valor total
        pedidoExistente.recalcularValorTotal();

        return pedidoRepository.save(pedidoExistente);
    }

    @Transactional
    public Pedido atualizarStatus(Long id, Pedido.StatusPedido novoStatus) {
        Pedido pedido = buscarPorId(id);

        validarAlteracaoStatus(pedido.getStatus(), novoStatus);

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscarPorId(id);

        // Só permite excluir pedidos com status PENDENTE
        if (pedido.getStatus() != Pedido.StatusPedido.PENDENTE) {
            throw new BadRequestException("Não é possível excluir um pedido que não esteja com status PENDENTE");
        }

        // Devolve os itens ao estoque
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
            produtoService.salvar(produto);
        }

        pedidoRepository.delete(pedido);
    }

    // Métodos de validação
    private void validarItensPedido(Pedido pedido) {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new BadRequestException("O pedido deve ter pelo menos um item");
        }

        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto() == null || item.getProduto().getId() == null) {
                throw new BadRequestException("Produto não informado para um dos itens do pedido");
            }

            Produto produto = produtoService.buscarPorId(item.getProduto().getId());
            item.setProduto(produto);

            if (item.getQuantidade() <= 0) {
                throw new BadRequestException("A quantidade deve ser maior que zero");
            }

            if (item.getQuantidade() > produto.getQuantidadeEstoque()) {
                throw new BadRequestException("Quantidade insuficiente em estoque para o produto: " + produto.getNome());
            }

            // Define o preço unitário com base no preço atual do produto
            item.setPrecoUnitario(produto.getPreco());
            item.calcularValorTotal();

            // Associa o item ao pedido
            item.setPedido(pedido);
        }

        // Recalcula o valor total do pedido
        pedido.recalcularValorTotal();
    }

    private void validarAlteracaoStatus(Pedido.StatusPedido statusAtual, Pedido.StatusPedido novoStatus) {
        // Regras de transição de status
        switch (statusAtual) {
            case PENDENTE:
                // De PENDENTE pode ir para APROVADO ou CANCELADO
                if (novoStatus != Pedido.StatusPedido.APROVADO && novoStatus != Pedido.StatusPedido.CANCELADO) {
                    throw new BadRequestException("De PENDENTE só pode alterar para APROVADO ou CANCELADO");
                }
                break;
            case APROVADO:
                // De APROVADO pode ir para ENTREGUE ou CANCELADO
                if (novoStatus != Pedido.StatusPedido.ENTREGUE && novoStatus != Pedido.StatusPedido.CANCELADO) {
                    throw new BadRequestException("De APROVADO só pode alterar para ENTREGUE ou CANCELADO");
                }
                break;
            case CANCELADO:
            case ENTREGUE:
                // Status final, não pode ser alterado
                throw new BadRequestException("Não é possível alterar o status de um pedido " + statusAtual);
            default:
                throw new BadRequestException("Status inválido");
        }
    }
}