package pe.edu.upc.ecomarket.iam.interfaces.acl;

import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Optional;

/**
 * Anti-corruption layer that other bounded contexts use to learn who is calling,
 * without depending on IAM internals.
 */
public interface IamContextFacade {

    /**
     * @return the authenticated user, or empty for anonymous requests
     */
    Optional<Requester> fetchCurrentRequester();

    /**
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException if nobody is signed in
     */
    Requester requireCurrentRequester();
}
