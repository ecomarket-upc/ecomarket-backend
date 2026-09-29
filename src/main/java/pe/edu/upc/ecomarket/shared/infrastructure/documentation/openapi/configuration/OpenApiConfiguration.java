package pe.edu.upc.ecomarket.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation. Swagger UI is served at /swagger-ui.html.
 * Protected endpoints declare {@code @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH)}.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "EcoMarket API",
        version = "v1",
        description = "RESTful API de EcoMarket: buscador inteligente de comercio local y sostenible"))
@SecurityScheme(
        name = OpenApiConfiguration.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfiguration {

    public static final String BEARER_AUTH = "bearerAuth";
}
