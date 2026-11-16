package com.sadevolui.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI sadEvoluiOpenApi() {
        return new OpenAPI()
                .info(new Info().title("SAD Evolui API").version("v1").description("API do Sistema de Apoio à Decisão SAD Evolui."))
                .components(new Components().addSecuritySchemes("integrationApiKey",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-Key")));
    }
}

