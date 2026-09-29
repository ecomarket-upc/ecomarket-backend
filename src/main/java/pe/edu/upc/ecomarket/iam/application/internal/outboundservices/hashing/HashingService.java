package pe.edu.upc.ecomarket.iam.application.internal.outboundservices.hashing;

/**
 * Port for password hashing. The BCrypt implementation lives in infrastructure.
 */
public interface HashingService {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
