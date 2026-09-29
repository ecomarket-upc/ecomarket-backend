package pe.edu.upc.ecomarket.iam.application.acl;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pe.edu.upc.ecomarket.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.ecomarket.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.ecomarket.shared.domain.model.valueobjects.Requester;

import java.util.Optional;

@Service
public class IamContextFacadeImpl implements IamContextFacade {

    @Override
    public Optional<Requester> fetchCurrentRequester() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            return Optional.empty();
        }
        boolean admin = user.getAuthorities().stream()
                .anyMatch(authority -> Roles.ROLE_ADMIN.name().equals(authority.getAuthority()));
        return Optional.of(new Requester(user.getId(), admin));
    }

    @Override
    public Requester requireCurrentRequester() {
        return fetchCurrentRequester()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Debes iniciar sesión"));
    }
}
