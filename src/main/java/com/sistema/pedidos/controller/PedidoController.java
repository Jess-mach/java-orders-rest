package com.sistema.pedidos.controller;

import com.sistema.pedidos.model.Pedido;
import com.sistema.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @Autowired
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna uma lista de todos os pedidos cadastrados com seus itens")
    public ResponseEntity<List<Pedido>> listarTodos() {
        List<Pedido> pedidos = pedidoService.buscarTodos();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico pelo seu ID com todos os seus itens")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/cliente")
    @Operation(summary = "Buscar pedidos por cliente", description = "Retorna uma lista de pedidos de um cliente específico com seus itens")
    public ResponseEntity<List<Pedido>> buscarPorCliente(@RequestParam String cliente) {
        List<Pedido> pedidos = pedidoService.buscarPorCliente(cliente);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/periodo")
    @Operation(summary = "Buscar pedidos por período", description = "Retorna uma lista de pedidos realizados dentro de um período específico com seus itens")
    public ResponseEntity<List<Pedido>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Pedido> pedidos = pedidoService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pedidos por status", description = "Retorna uma lista de pedidos com um status específico com seus itens")
    public ResponseEntity<List<Pedido>> buscarPorStatus(@PathVariable Pedido.StatusPedido status) {
        List<Pedido> pedidos = pedidoService.buscarPorStatus(status);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping
    @Operation(summary = "Criar um novo pedido", description = "Cria um novo pedido com os dados informados, incluindo seus itens")
    public ResponseEntity<Pedido> criar(@Valid @RequestBody Pedido pedido) {
        Pedido novoPedido = pedidoService.salvar(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um pedido", description = "Atualiza os dados de um pedido existente")
    public ResponseEntity<Pedido> atualizar(@PathVariable Long id, @Valid @RequestBody Pedido pedido) {
        Pedido pedidoAtualizado = pedidoService.atualizar(id, pedido);
        return ResponseEntity.ok(pedidoAtualizado);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza apenas o status de um pedido existente")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Pedido.StatusPedido status) {
        Pedido pedidoAtualizado = pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(pedidoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um pedido", description = "Remove um pedido do sistema")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}