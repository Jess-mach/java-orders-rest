package com.sistema.pedidos.controller;

import com.sistema.pedidos.entity.ProdutoEntity;
import com.sistema.pedidos.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @Autowired
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados")
    public ResponseEntity<List<ProdutoEntity>> listarTodos() {
        List<ProdutoEntity> produtoEntities = produtoService.buscarTodos();
        return ResponseEntity.ok(produtoEntities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo seu ID")
    public ResponseEntity<ProdutoEntity> buscarPorId(@PathVariable Long id) {
        ProdutoEntity produtoEntity = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produtoEntity);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar produtos por nome", description = "Retorna uma lista de produtos que contenham o nome informado")
    public ResponseEntity<List<ProdutoEntity>> buscarPorNome(@RequestParam String nome) {
        List<ProdutoEntity> produtoEntities = produtoService.buscarPorNome(nome);
        return ResponseEntity.ok(produtoEntities);
    }

    @PostMapping
    @Operation(summary = "Criar um novo produto", description = "Cria um novo produto com os dados informados")
    public ResponseEntity<ProdutoEntity> criar(@Valid @RequestBody ProdutoEntity produtoEntity) {
        ProdutoEntity novoProdutoEntity = produtoService.salvar(produtoEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProdutoEntity);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um produto", description = "Atualiza os dados de um produto existente")
    public ResponseEntity<ProdutoEntity> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoEntity produtoEntity) {
        ProdutoEntity produtoEntityAtualizado = produtoService.atualizar(id, produtoEntity);
        return ResponseEntity.ok(produtoEntityAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um produto", description = "Remove um produto do sistema")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
