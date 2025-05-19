package com.sistema.pedidos.controller;

import com.sistema.pedidos.entity.PedidoEntity;
import com.sistema.pedidos.model.PedidoRequest;
import com.sistema.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna uma lista de todos os pedidos cadastrados com seus itens")
    public ResponseEntity<List<PedidoEntity>> listarTodos() {
        List<PedidoEntity> pedidoEntities = pedidoService.buscarTodos();
        return ResponseEntity.ok(pedidoEntities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico pelo seu ID com todos os seus itens")
    public ResponseEntity<PedidoEntity> buscarPorId(@PathVariable Long id) {
        PedidoEntity pedidoEntity = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedidoEntity);
    }

    @GetMapping("/cliente")
    @Operation(summary = "Buscar pedidos por cliente", description = "Retorna uma lista de pedidos de um cliente específico com seus itens")
    public ResponseEntity<List<PedidoEntity>> buscarPorCliente(@RequestParam String cliente) {
        List<PedidoEntity> pedidoEntities = pedidoService.buscarPorCliente(cliente);
        return ResponseEntity.ok(pedidoEntities);
    }

    @GetMapping("/periodo")
    @Operation(summary = "Buscar pedidos por período", description = "Retorna uma lista de pedidos realizados dentro de um período específico com seus itens")
    public ResponseEntity<List<PedidoEntity>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<PedidoEntity> pedidoEntities = pedidoService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(pedidoEntities);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pedidos por status", description = "Retorna uma lista de pedidos com um status específico com seus itens")
    public ResponseEntity<List<PedidoEntity>> buscarPorStatus(@PathVariable PedidoEntity.StatusPedido status) {
        List<PedidoEntity> pedidoEntities = pedidoService.buscarPorStatus(status);
        return ResponseEntity.ok(pedidoEntities);
    }

    @PostMapping
    @Operation(summary = "Criar um novo pedido", description = "Cria um novo pedido com os dados informados, incluindo seus itens")
    public ResponseEntity<PedidoEntity> criar(@Valid @RequestBody PedidoRequest request) {
        logger.info("message=Inicio metodo criar novo pedido.");
        PedidoEntity novoPedidoEntity = pedidoService.salvar(request);
        logger.info("message=Finalização do metodo novo pedido.");
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPedidoEntity);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um pedido", description = "Atualiza os dados de um pedido existente")
    public ResponseEntity<PedidoEntity> atualizar(@PathVariable Long id, @Valid @RequestBody PedidoEntity pedidoEntity) {
        PedidoEntity pedidoEntityAtualizado = pedidoService.atualizar(id, pedidoEntity);
        return ResponseEntity.ok(pedidoEntityAtualizado);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza apenas o status de um pedido existente")
    public ResponseEntity<PedidoEntity> atualizarStatus(
            @PathVariable Long id,
            @RequestParam PedidoEntity.StatusPedido status) {
        PedidoEntity pedidoEntityAtualizado = pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(pedidoEntityAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um pedido", description = "Remove um pedido do sistema")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}