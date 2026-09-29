package pe.edu.upc.ecomarket.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Fills createdAt and updatedAt automatically on every aggregate.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
