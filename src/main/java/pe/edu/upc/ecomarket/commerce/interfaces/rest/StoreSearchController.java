package pe.edu.upc.ecomarket.commerce.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.ecomarket.commerce.domain.model.queries.GetNearbyStoresQuery;
import pe.edu.upc.ecomarket.commerce.domain.model.valueobjects.GeoLocation;
import pe.edu.upc.ecomarket.commerce.domain.services.StoreQueryService;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.resources.NearbyStoreResource;
import pe.edu.upc.ecomarket.commerce.interfaces.rest.transform.StoreAssemblers;

import java.util.List;

@Tag(name = "Búsqueda", description = "Búsqueda de comercios y productos")
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class StoreSearchController {

    private final StoreQueryService storeQueryService;

    @Operation(summary = "Buscar comercios aprobados cercanos", description = "Ordenados del más cercano al más lejano.")
    @GetMapping("/cercanos")
    public List<NearbyStoreResource> getNearby(
            @Parameter(example = "-12.1040") @RequestParam Double lat,
            @Parameter(example = "-76.9630") @RequestParam Double lng,
            @Parameter(description = "Radio en km (máximo 50)", example = "5")
            @RequestParam(name = "radio", defaultValue = "5") double radiusKm) {
        return storeQueryService.handle(new GetNearbyStoresQuery(new GeoLocation(lat, lng), radiusKm)).stream()
                .map(StoreAssemblers::toResource)
                .toList();
    }
}
