package com.jcaa.usersmanagement.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configura Swagger UI para enviar el JWT: agrega el botón "Authorize" (esquema Bearer).
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Users Management API", version = "v1"),
    security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(
    name = OpenApiConfig.BEARER_AUTH,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class OpenApiConfig {

  public static final String BEARER_AUTH = "bearerAuth";
}
