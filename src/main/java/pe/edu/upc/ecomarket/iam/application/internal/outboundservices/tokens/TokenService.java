package pe.edu.upc.ecomarket.iam.application.internal.outboundservices.tokens;

import pe.edu.upc.ecomarket.iam.domain.model.aggregates.User;

/**
 * Port for issuing and reading access tokens. The JWT implementation lives in infrastructure.
 */
public interface TokenService {

    String generateToken(User user);

    /**
     * @return the email stored in a valid token
     * @throws IllegalArgumentException if the token is invalid, tampered or expired
     */
    String getEmailFromToken(String token);

    long getExpirationInSeconds();
}
