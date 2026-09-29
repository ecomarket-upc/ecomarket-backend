package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record ProductResource(
        Long id,
        Long storeId,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String imageUrl,
        Set<Long> ecoLabelIds,
        boolean classifiedByAi,
        Instant createdAt
) {
}
