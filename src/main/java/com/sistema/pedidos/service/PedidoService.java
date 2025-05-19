package com.sistema.pedidos.service;

import com.sistema.pedidos.entity.ItemPedidoEntity;
import com.sistema.pedidos.entity.PedidoEntity;
import com.sistema.pedidos.exception.BadRequestException;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.model.ItemPedidoRequest;
import com.sistema.pedidos.model.PedidoRequest;
import com.sistema.pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final ItemPedidoService itemPedidoService;

    @Autowired
    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoService produtoService,
            ItemPedidoService itemPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
        this.itemPedidoService = itemPedidoService;
    }

    @Transactional(readOnly = true)
    public List<PedidoEntity> buscarTodos() {
        List<PedidoEntity> all = pedidoRepository.findAll();
        //alterar o retorno para uma nova entidade chamada PedidoResponse com uma lista de ItemResponse
        return all;
    }

    @Transactional(readOnly = true)
    public PedidoEntity buscarPorId(Long id) {
        Optional<PedidoEntity> byId = pedidoRepository.findById(id);

        return byId.orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
    }

    @Transactional(readOnly = true)
    public List<PedidoEntity> buscarPorCliente(String cliente) {
        return pedidoRepository.findByClienteContainingIgnoreCase(cliente);
    }

    @Transactional(readOnly = true)
    public List<PedidoEntity> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataPedidoBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<PedidoEntity> buscarPorStatus(PedidoEntity.StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    @Transactional
    public PedidoEntity salvar(PedidoRequest request) {
        PedidoEntity pedidoEntity = new PedidoEntity();
        pedidoEntity.setCliente(request.getCliente());
        pedidoEntity.setObservacao(request.getObservacao());
        pedidoEntity.setValorTotal(request.getValorTotal());

       pedidoEntity = validarItensPedido(request, pedidoEntity);

        // Define a data do pedido como agora se não for informada
        if (pedidoEntity.getDataPedido() == null) {
            pedidoEntity.setDataPedido(LocalDateTime.now());
        }

        // Define o status inicial como PENDENTE se não for informado
        if (request.getStatus() == null) {
            pedidoEntity.setStatus(PedidoEntity.StatusPedido.PENDENTE);
        }

        // Salva o pedido com seus itens
        PedidoEntity pedidoSalvo = pedidoRepository.save(pedidoEntity);

        // Atualiza o estoque dos produtos
        for (ItemPedidoEntity item : pedidoEntity.getItens()) {
            item.setPedidoId(pedidoSalvo.getId());

            itemPedidoService.salvar(item);

            produtoService.atualizarEstoque(item.getProduto().getId(), item.getQuantidade());
        }

        return pedidoRepository.findById(pedidoSalvo.getId())
                .orElseThrow();
    }

    @Transactional
    public PedidoEntity atualizar(Long id, PedidoEntity pedidoEntityAtualizado) {
        PedidoEntity pedidoEntityExistente = buscarPorId(id);

        // Só permite atualizar pedidos com status PENDENTE
        if (pedidoEntityExistente.getStatus() != PedidoEntity.StatusPedido.PENDENTE) {
            throw new BadRequestException("Não é possível atualizar um pedido que não esteja com status PENDENTE");
        }

        // Atualiza apenas os campos permitidos
        pedidoEntityExistente.setCliente(pedidoEntityAtualizado.getCliente());
        pedidoEntityExistente.setObservacao(pedidoEntityAtualizado.getObservacao());

        // Se o status está sendo alterado, verifica se a transição é válida
        if (pedidoEntityAtualizado.getStatus() != null &&
                pedidoEntityExistente.getStatus() != pedidoEntityAtualizado.getStatus()) {
            validarAlteracaoStatus(pedidoEntityExistente.getStatus(), pedidoEntityAtualizado.getStatus());
            pedidoEntityExistente.setStatus(pedidoEntityAtualizado.getStatus());
        }

        // Se houver novos itens, validar e atualizar
        if (pedidoEntityAtualizado.getItens() != null && !pedidoEntityAtualizado.getItens().isEmpty()) {
            // Remove os itens antigos e restaura o estoque
            for (ItemPedidoEntity itemAntigo : pedidoEntityExistente.getItens()) {
                ProdutoEntity produtoEntity = itemAntigo.getProduto();
                produtoEntity.setQuantidadeEstoque(produtoEntity.getQuantidadeEstoque() + itemAntigo.getQuantidade());
                produtoService.salvar(produtoEntity);
            }

            // Limpa todos os itens atuais
            pedidoEntityExistente.getItens().clear();

            // Adiciona os novos itens
            for (ItemPedidoEntity novoItem : pedidoEntityAtualizado.getItens()) {
                ProdutoEntity produtoEntity = produtoService.buscarPorId(novoItem.getProduto().getId());

                if (novoItem.getQuantidade() <= 0) {
                    throw new BadRequestException("A quantidade deve ser maior que zero");
                }

                if (novoItem.getQuantidade() > produtoEntity.getQuantidadeEstoque()) {
                    throw new BadRequestException("Quantidade insuficiente em estoque para o produto: " + produtoEntity.getNome());
                }

                // Configura o novo item
                ItemPedidoEntity item = new ItemPedidoEntity();
                item.setPedidoId(pedidoEntityExistente.getId());
                item.setProduto(produtoEntity);
                item.setQuantidade(novoItem.getQuantidade());
                item.setPrecoUnitario(produtoEntity.getPreco());
                item.calcularValorTotal();

                // Adiciona o item ao pedido
                pedidoEntityExistente.getItens().add(item);

                // Atualiza o estoque
                produtoService.atualizarEstoque(produtoEntity.getId(), novoItem.getQuantidade());
            }
        }

        // Recalcula o valor total
        pedidoEntityExistente.recalcularValorTotal();

        return pedidoRepository.save(pedidoEntityExistente);
    }

    @Transactional
    public PedidoEntity atualizarStatus(Long id, PedidoEntity.StatusPedido novoStatus) {
        PedidoEntity pedidoEntity = buscarPorId(id);

        validarAlteracaoStatus(pedidoEntity.getStatus(), novoStatus);

        pedidoEntity.setStatus(novoStatus);
        return pedidoRepository.save(pedidoEntity);
    }

    @Transactional
    public void excluir(Long id) {
        PedidoEntity pedidoEntity = buscarPorId(id);

        // Só permite excluir pedidos com status PENDENTE
        if (pedidoEntity.getStatus() != PedidoEntity.StatusPedido.PENDENTE) {
            throw new BadRequestException("Não é possível excluir um pedido que não esteja com status PENDENTE");
        }

        // Devolve os itens ao estoque
        for (ItemPedidoEntity item : pedidoEntity.getItens()) {
            ProdutoEntity produtoEntity = item.getProduto();
            produtoEntity.setQuantidadeEstoque(produtoEntity.getQuantidadeEstoque() + item.getQuantidade());
            produtoService.salvar(produtoEntity);
        }

        pedidoRepository.delete(pedidoEntity);
    }

    // Métodos de validação
    private PedidoEntity validarItensPedido(PedidoRequest request, PedidoEntity pedidoEntity) {
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new BadRequestException("O pedido deve ter pelo menos um item");
        }

        List <ItemPedidoEntity> itens = new ArrayList<>();


        for (ItemPedidoRequest item : request.getItens()) {
            ProdutoEntity produtoEntity = produtoService.buscarPorId(item.getProdutoId());
            ItemPedidoEntity itemPedido = new ItemPedidoEntity();

            itemPedido.setProduto(produtoEntity);
            itemPedido.setQuantidade(item.getQuantidade());

            if (item.getQuantidade() <= 0) {
                throw new BadRequestException("A quantidade deve ser maior que zero");
            }

            if (item.getQuantidade() > produtoEntity.getQuantidadeEstoque()) {
                throw new BadRequestException("Quantidade insuficiente em estoque para o produto: " + produtoEntity.getNome());
            }

            // Define o preço unitário com base no preço atual do produto
            itemPedido.setPrecoUnitario(produtoEntity.getPreco());
            itemPedido.calcularValorTotal();

            itens.add(itemPedido);
        }

        pedidoEntity.setItens(itens);
        // Recalcula o valor total do pedido
        pedidoEntity.recalcularValorTotal();

        return pedidoEntity;
    }

    private void validarAlteracaoStatus(PedidoEntity.StatusPedido statusAtual, PedidoEntity.StatusPedido novoStatus) {
        // Regras de transição de status
        switch (statusAtual) {
            case PENDENTE:
                // De PENDENTE pode ir para APROVADO ou CANCELADO
                if (novoStatus != PedidoEntity.StatusPedido.APROVADO && novoStatus != PedidoEntity.StatusPedido.CANCELADO) {
                    throw new BadRequestException("De PENDENTE só pode alterar para APROVADO ou CANCELADO");
                }
                break;
            case APROVADO:
                // De APROVADO pode ir para ENTREGUE ou CANCELADO
                if (novoStatus != PedidoEntity.StatusPedido.ENTREGUE && novoStatus != PedidoEntity.StatusPedido.CANCELADO) {
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