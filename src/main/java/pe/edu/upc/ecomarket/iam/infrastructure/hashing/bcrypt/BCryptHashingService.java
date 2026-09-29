package pe.edu.upc.ecomarket.iam.infrastructure.hashing.bcrypt;

import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upc.ecomarket.iam.application.internal.outboundservices.hashing.HashingService;

/**
 * BCrypt hashing that also works as the Spring Security {@link PasswordEncoder}.
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
