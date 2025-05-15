package com.sistema.pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Sistema de Pedidos")
                        .description("API para gerenciamento de pedidos, itens de pedidos e produtos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Desenvolvedor")
                                .email("dev@sistema.com")));
    }
}