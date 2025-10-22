package com.login.Login.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Add JWT Authentication support in Swagger
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // General API Info
                .info(new Info()
                        .title("Kavach")
                        .description("""
                                This is the project to store the credentials and the files of the Clients and assign that to a specific user
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IDEA NEURON")
                                .email("hello@ideaneuron.com")
                                .url("https://www.ideaneuron.com"))
                        .license(new License()
                                .name("IDEA NEURON")
                                .url("https://www.ideaneuron.com"))
                );
    }
}

