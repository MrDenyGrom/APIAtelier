package com.example.api_backend_atelier.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${swagger.server.url}")
    private String serverUrl;


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(new Server().url(serverUrl + ":" + serverPort)))
                .security(List.of(new SecurityRequirement().addList("bearer-key")))
                .components(new Components()
                        .addSecuritySchemes("bearer-key", securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("API Ателье")
                .version("1.0.0")
                .description("API для управления заказами и клиентами ателье.")
                .termsOfService("http://swagger.io/terms/")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Имя Контакта")
                .url("http://www.example.com/support")
                .email("support@example.com");
    }

    private License apiLicense() {
        return new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");
    }


    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Схема аутентификации Bearer Token.  Добавьте 'Bearer ' + ваш токен в поле 'Value'.");
    }
}