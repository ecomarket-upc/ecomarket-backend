package pe.edu.upc.ecomarket.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 401 (missing or invalid token) and 403 (role not allowed) happen inside the filter chain,
 * before any controller runs. This forwards them to the GlobalExceptionHandler so they use
 * the same JSON body as every other error.
 */
@RequiredArgsConstructor
public class SecurityErrorForwarder implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        resolver.resolveException(request, response, null, accessDeniedException);
    }
}
