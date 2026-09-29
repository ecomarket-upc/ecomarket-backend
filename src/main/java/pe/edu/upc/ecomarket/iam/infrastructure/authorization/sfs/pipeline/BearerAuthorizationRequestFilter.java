package pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upc.ecomarket.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.io.IOException;

/**
 * Reads {@code Authorization: Bearer <token>} on every request. When the token is valid and the
 * account is still active, the user is authenticated for that request. Otherwise the request
 * continues anonymously and Spring Security answers 401 if the endpoint needs a user.
 * Not a @Component on purpose: it is registered only inside the security filter chain.
 */
@RequiredArgsConstructor
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private final BearerTokenService tokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            tokenService.getBearerTokenFrom(request).ifPresent(token -> authenticate(token, request));
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(tokenService.getEmailFromToken(token));
            if (!user.isEnabled()) {
                return;
            }
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
        }
    }
}
