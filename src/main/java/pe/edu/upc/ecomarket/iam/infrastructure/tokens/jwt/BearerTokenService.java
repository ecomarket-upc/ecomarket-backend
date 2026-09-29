package pe.edu.upc.ecomarket.iam.infrastructure.tokens.jwt;

import jakarta.servlet.http.HttpServletRequest;
import pe.edu.upc.ecomarket.iam.application.internal.outboundservices.tokens.TokenService;

import java.util.Optional;

/**
 * JWT token service that also knows how to read the token from the Authorization header.
 */
public interface BearerTokenService extends TokenService {

    Optional<String> getBearerTokenFrom(HttpServletRequest request);
}
