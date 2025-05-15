package com.sistema.pedidos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SistemaPedidosApplicationTests {

    @Test
    void contextLoads() {
        // Teste para verificar se o contexto da aplicação carrega corretamente
    }
}

// Arquivo: src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

spring.h2.console.enabled=true

        # Configuração Swagger UI
springdoc.swagger-ui.path=/swagger-ui.html
@Test
@DisplayName("Deve atualizar o estoque de um produto")
void testAtualizarEstoque() {
    // Arrange
    when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
    when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    produtoService.atualizarEstoque(1L, 3);

    // Assert
    assertEquals(7, produto.getQuantidadeEstoque());
    verify(produtoRepository, times(1)).findById(1L);
    verify(produtoRepository, times(1)).save(produto);
}

@Test
@DisplayName("Deve lançar exceção ao tentar reduzir estoque para quantidade negativa")
void testAtualizarEstoqueQuantidadeInsuficiente() {
    // Arrange
    when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> produtoService.atualizarEstoque(1L, 15));
    verify(produtoRepository, times(1)).findById(1L);
    verify(produtoRepository, never()).save(any(Produto.class));
}
}
