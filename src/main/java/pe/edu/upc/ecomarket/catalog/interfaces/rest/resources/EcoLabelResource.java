package pe.edu.upc.ecomarket.catalog.interfaces.rest.resources;

import java.util.Set;

public record EcoLabelResource(Long id, String name, String description, String criteria, Set<String> keywords) {
}
