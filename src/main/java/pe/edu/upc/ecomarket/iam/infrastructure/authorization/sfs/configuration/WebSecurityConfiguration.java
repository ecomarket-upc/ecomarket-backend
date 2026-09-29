package pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.pipeline.SecurityErrorForwarder;
import pe.edu.upc.ecomarket.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.util.Arrays;
import java.util.List;

/**
 * API security rules:
 * - Stateless: every request is authenticated by its own JWT, there is no HTTP session.
 * - Public: sign up, sign in, Swagger and the GET queries of the catalog, stores and search.
 * - Everything else needs a token; role checks are done with @PreAuthorize in controllers.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error"
    };

    /** GET endpoints under public paths that still belong to a specific user. */
    private static final String[] PRIVATE_QUERIES = {
            "/api/comercios/mis-comercios",
            "/api/comercios/pendientes",
            "/api/productos/mis-productos"
    };

    private static final String[] PUBLIC_QUERIES = {
            "/api/categorias/**",
            "/api/eco-etiquetas/**",
            "/api/comercios/**",
            "/api/productos/**",
            "/api/busqueda/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BearerTokenService tokenService,
                                                   UserDetailsService userDetailsService,
                                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver)
            throws Exception {
        SecurityErrorForwarder errorForwarder = new SecurityErrorForwarder(resolver);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> { })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PRIVATE_QUERIES).authenticated()
                        .requestMatchers(HttpMethod.GET, PUBLIC_QUERIES).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(errorForwarder)
                        .accessDeniedHandler(errorForwarder))
                .addFilterBefore(new BearerAuthorizationRequestFilter(tokenService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
