package com.sistema.pedidos.service;

import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.exception.ResourceNotFoundException;
import com.sistema.pedidos.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoEntity> buscarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProdutoEntity buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoEntity> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional
    public ProdutoEntity salvar(ProdutoEntity produtoEntity) {
        return produtoRepository.save(produtoEntity);
    }

    @Transactional
    public ProdutoEntity atualizar(Long id, ProdutoEntity produtoEntityAtualizado) {
        ProdutoEntity produtoEntityExistente = buscarPorId(id);

        produtoEntityExistente.setNome(produtoEntityAtualizado.getNome());
        produtoEntityExistente.setDescricao(produtoEntityAtualizado.getDescricao());
        produtoEntityExistente.setPreco(produtoEntityAtualizado.getPreco());
        produtoEntityExistente.setQuantidadeEstoque(produtoEntityAtualizado.getQuantidadeEstoque());

        return produtoRepository.save(produtoEntityExistente);
    }

    @Transactional
    public void atualizarEstoque(Long id, int quantidade) {
        ProdutoEntity produtoEntity = buscarPorId(id);
        int novoEstoque = produtoEntity.getQuantidadeEstoque() - quantidade;

        if (novoEstoque < 0) {
            throw new RuntimeException("Quantidade insuficiente em estoque para o produto: " + produtoEntity.getNome());
        }

        produtoEntity.setQuantidadeEstoque(novoEstoque);
        produtoRepository.save(produtoEntity);
    }

    @Transactional
    public void excluir(Long id) {
        ProdutoEntity produtoEntity = buscarPorId(id);
        produtoRepository.delete(produtoEntity);
    }
}