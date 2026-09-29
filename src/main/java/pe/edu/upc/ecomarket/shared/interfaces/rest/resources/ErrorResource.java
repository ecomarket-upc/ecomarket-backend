package pe.edu.upc.ecomarket.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Single error format returned by the whole API.
 *
 * @param errors per-field messages when validation fails; omitted otherwise
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResource(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {
}
