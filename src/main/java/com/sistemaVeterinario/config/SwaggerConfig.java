package com.sistemaVeterinario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Sistema Veterinario de gestión de usuarios, mascotas y citas")
                        .version("1.0")
                        .description("Esta API permite crear, ver, editar y eliminar, citas y mascotas, ademas de la respectiva gestión de usuarios por parte del administrador.")
                        .contact(new Contact()
                                .name("soporteVetPlanet")
                                .email("soporte@veterinaria.com")
                        )
                );
    }
}
