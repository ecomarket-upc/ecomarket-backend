package pe.edu.upc.ecomarket.commerce;

import org.junit.jupiter.api.Test;
import pe.edu.upc.ecomarket.shared.IntegrationTestSupport;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreIntegrationTests extends IntegrationTestSupport {

    /** Parque Kennedy, Miraflores. */
    private static final String STORE_IN_MIRAFLORES = """
            {"name": "Granel Verde", "description": "Tienda a granel", "phone": "987654321",
             "contactEmail": "contacto@granelverde.pe", "openingHours": "Lun-Sáb 9:00-19:00",
             "addressLine": "Av. Larco 345", "district": "Miraflores", "city": "Lima",
             "latitude": -12.1211, "longitude": -77.0297}
            """;

    private Long createStore(String sellerToken) throws Exception {
        return idOf(mockMvc.perform(json(post("/api/comercios"), sellerToken, STORE_IN_MIRAFLORES))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString());
    }

    private void approve(Long storeId) throws Exception {
        mockMvc.perform(json(patch("/api/comercios/" + storeId + "/validacion"), adminToken(), """
                        {"status": "APPROVED", "comment": "Datos verificados"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void onlySellersCanCreateStores() throws Exception {
        mockMvc.perform(json(post("/api/comercios"), registerAndGetToken("ROLE_CONSUMER"), STORE_IN_MIRAFLORES))
                .andExpect(status().isForbidden());
    }

    @Test
    void createValidatesRequiredFields() throws Exception {
        mockMvc.perform(json(post("/api/comercios"), registerAndGetToken("ROLE_SELLER"), """
                        {"name": "", "addressLine": "", "district": "Miraflores", "city": "Lima"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.addressLine").exists());
    }

    @Test
    void createWithoutCoordinatesFailsWhenAddressCannotBeGeocoded() throws Exception {
        mockMvc.perform(json(post("/api/comercios"), registerAndGetToken("ROLE_SELLER"), """
                        {"name": "Sin mapa", "addressLine": "Calle 1", "district": "Lince", "city": "Lima"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pendingStoreIsHiddenUntilApproved() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller);

        mockMvc.perform(get("/api/comercios"))
                .andExpect(jsonPath("$[*].id", not(hasItem(storeId.intValue()))));
        mockMvc.perform(get("/api/comercios/" + storeId))
                .andExpect(status().isNotFound());
        mockMvc.perform(auth(get("/api/comercios/" + storeId), seller))
                .andExpect(status().isOk());
        mockMvc.perform(auth(get("/api/comercios/pendientes"), adminToken()))
                .andExpect(jsonPath("$[*].id", hasItem(storeId.intValue())));

        approve(storeId);

        mockMvc.perform(get("/api/comercios"))
                .andExpect(jsonPath("$[*].id", hasItem(storeId.intValue())));
        mockMvc.perform(get("/api/comercios/" + storeId))
                .andExpect(status().isOk());
    }

    @Test
    void rejectingRequiresAComment() throws Exception {
        Long storeId = createStore(registerAndGetToken("ROLE_SELLER"));
        mockMvc.perform(json(patch("/api/comercios/" + storeId + "/validacion"), adminToken(), """
                        {"status": "REJECTED"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sellerCannotEditOrDeleteAnotherSellersStore() throws Exception {
        Long storeId = createStore(registerAndGetToken("ROLE_SELLER"));
        String otherSeller = registerAndGetToken("ROLE_SELLER");

        mockMvc.perform(json(put("/api/comercios/" + storeId), otherSeller, STORE_IN_MIRAFLORES))
                .andExpect(status().isForbidden());
        mockMvc.perform(auth(delete("/api/comercios/" + storeId), otherSeller))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanUpdateAndDeleteTheirStore() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller);

        mockMvc.perform(json(put("/api/comercios/" + storeId), seller,
                        STORE_IN_MIRAFLORES.replace("Granel Verde", "Granel Verde Express")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Granel Verde Express"));
        mockMvc.perform(auth(get("/api/comercios/mis-comercios"), seller))
                .andExpect(jsonPath("$[*].id", hasItem(storeId.intValue())));
        mockMvc.perform(auth(delete("/api/comercios/" + storeId), seller))
                .andExpect(status().isNoContent());
        mockMvc.perform(auth(get("/api/comercios/" + storeId), seller))
                .andExpect(status().isNotFound());
    }

    @Test
    void nearbySearchReturnsApprovedStoresWithinRadius() throws Exception {
        Long storeId = createStore(registerAndGetToken("ROLE_SELLER"));
        approve(storeId);

        // Óvalo Gutiérrez, about 2.5 km from Parque Kennedy.
        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1020").param("lng", "-77.0420").param("radio", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].store.id", hasItem(storeId.intValue())));
        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1020").param("lng", "-77.0420").param("radio", "1"))
                .andExpect(jsonPath("$[*].store.id", not(hasItem(storeId.intValue()))));
    }

    @Test
    void nearbySearchValidatesRadius() throws Exception {
        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1").param("lng", "-77.0").param("radio", "500"))
                .andExpect(status().isBadRequest());
    }
}
