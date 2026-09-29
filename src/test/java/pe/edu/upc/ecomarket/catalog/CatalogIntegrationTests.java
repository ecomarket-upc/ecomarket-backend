package pe.edu.upc.ecomarket.catalog;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import pe.edu.upc.ecomarket.shared.IntegrationTestSupport;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogIntegrationTests extends IntegrationTestSupport {

    private static final String STORE = """
            {"name": "Bodega Verde", "addressLine": "Av. Arequipa 1200", "district": "Lince", "city": "Lima",
             "latitude": -12.0850, "longitude": -77.0350}
            """;

    private Long idByName(String path, String name) throws Exception {
        String body = mockMvc.perform(get(path)).andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst().longValue();
    }

    private Long createStore(String sellerToken, boolean approved) throws Exception {
        Long storeId = idOf(mockMvc.perform(json(post("/api/comercios"), sellerToken, STORE))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        if (approved) {
            mockMvc.perform(json(patch("/api/comercios/" + storeId + "/validacion"), adminToken(), """
                            {"status": "APPROVED"}
                            """))
                    .andExpect(status().isOk());
        }
        return storeId;
    }

    private String productJson(Long storeId, String name, String description) throws Exception {
        return """
                {"storeId": %d, "categoryId": %d, "name": "%s", "description": "%s", "price": 12.50, "stock": 40}
                """.formatted(storeId, idByName("/api/categorias", "Alimentos a granel"), name, description);
    }

    private Long createProduct(String sellerToken, Long storeId, String name, String description) throws Exception {
        return idOf(mockMvc.perform(json(post("/api/productos"), sellerToken, productJson(storeId, name, description)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void baseCategoriesAndEcoLabelsArePublic() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Alimentos a granel", "Cuidado personal")));
        mockMvc.perform(get("/api/eco-etiquetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Venta a granel", "Sin envase plástico", "Orgánico")));
    }

    @Test
    void onlyAdminManagesCategories() throws Exception {
        String body = """
                {"name": "Mascotas %s", "description": "Productos para mascotas"}
                """.formatted(System.nanoTime());
        mockMvc.perform(json(post("/api/categorias"), registerAndGetToken("ROLE_SELLER"), body))
                .andExpect(status().isForbidden());
        Long categoryId = idOf(mockMvc.perform(json(post("/api/categorias"), adminToken(), body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(json(post("/api/categorias"), adminToken(), body))
                .andExpect(status().isConflict());
        mockMvc.perform(auth(delete("/api/categorias/" + categoryId), adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void categoryWithProductsCannotBeDeleted() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        String admin = adminToken();
        Long categoryId = idOf(mockMvc.perform(json(post("/api/categorias"), admin, """
                        {"name": "Semillas %s"}
                        """.formatted(System.nanoTime())))
                .andReturn().getResponse().getContentAsString());
        Long storeId = createStore(seller, false);
        mockMvc.perform(json(post("/api/productos"), seller, """
                        {"storeId": %d, "categoryId": %d, "name": "Semillas de girasol", "price": 5, "stock": 10}
                        """.formatted(storeId, categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(auth(delete("/api/categorias/" + categoryId), admin))
                .andExpect(status().isConflict());
    }

    @Test
    void sellerCannotPublishInAnotherSellersStore() throws Exception {
        Long storeId = createStore(registerAndGetToken("ROLE_SELLER"), true);
        mockMvc.perform(json(post("/api/productos"), registerAndGetToken("ROLE_SELLER"),
                        productJson(storeId, "Arroz", "Arroz integral")))
                .andExpect(status().isForbidden());
    }

    @Test
    void productValidatesPriceAndReferences() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, false);
        mockMvc.perform(json(post("/api/productos"), seller,
                        productJson(storeId, "Arroz", "Arroz").replace("12.50", "-3")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
        mockMvc.perform(json(post("/api/productos"), seller, """
                        {"storeId": %d, "categoryId": 999999, "name": "Arroz", "price": 5, "stock": 1}
                        """.formatted(storeId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(json(post("/api/productos"), seller,
                        productJson(storeId, "Arroz", "Arroz").replace("\"stock\": 40", "\"stock\": 40, \"ecoLabelIds\": [999999]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productsOfPendingStoresAreHiddenUntilApproval() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, false);
        Long productId = createProduct(seller, storeId, "Lentejas bebé", "Lentejas por kilo");

        mockMvc.perform(get("/api/productos/" + productId)).andExpect(status().isNotFound());
        mockMvc.perform(auth(get("/api/productos/" + productId), seller)).andExpect(status().isOk());
        mockMvc.perform(auth(get("/api/productos/mis-productos"), seller))
                .andExpect(jsonPath("$[*].id", hasItem(productId.intValue())));
        mockMvc.perform(get("/api/productos"))
                .andExpect(jsonPath("$[*].id", not(hasItem(productId.intValue()))));

        mockMvc.perform(json(patch("/api/comercios/" + storeId + "/validacion"), adminToken(), """
                        {"status": "APPROVED"}
                        """));

        mockMvc.perform(get("/api/productos").param("comercioId", storeId.toString()))
                .andExpect(jsonPath("$[*].id", hasItem(productId.intValue())));
        mockMvc.perform(get("/api/busqueda/productos").param("nombre", "LENTEJAS"))
                .andExpect(jsonPath("$[*].id", hasItem(productId.intValue())));
    }

    @Test
    void classifyAssignsEcoLabelsWithKeywordsWhenGeminiIsNotConfigured() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, true);
        Long productId = createProduct(seller, storeId, "Quinua blanca",
                "Quinua orgánica de productores de Puno, vendida a granel por kilo");
        Long bulk = idByName("/api/eco-etiquetas", "Venta a granel");
        Long organic = idByName("/api/eco-etiquetas", "Orgánico");
        Long reusable = idByName("/api/eco-etiquetas", "Reutilizable");

        mockMvc.perform(post("/api/productos/" + productId + "/clasificar"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(auth(post("/api/productos/" + productId + "/clasificar"), registerAndGetToken("ROLE_SELLER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(auth(post("/api/productos/" + productId + "/clasificar"), seller))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.engine").value("keywords"))
                .andExpect(jsonPath("$.product.classifiedByAi").value(true))
                .andExpect(jsonPath("$.product.ecoLabelIds", hasItems(bulk.intValue(), organic.intValue())))
                .andExpect(jsonPath("$.product.ecoLabelIds", not(hasItem(reusable.intValue()))));

        mockMvc.perform(get("/api/busqueda/productos").param("ecoEtiqueta", organic.toString()))
                .andExpect(jsonPath("$[*].id", hasItem(productId.intValue())));
    }

    @Test
    void ownerUpdatesAndDeletesProducts() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, true);
        Long productId = createProduct(seller, storeId, "Jabón de avena", "Jabón artesanal");
        String update = """
                {"categoryId": %d, "name": "Jabón de avena y miel", "price": 9.90, "stock": 5}
                """.formatted(idByName("/api/categorias", "Cuidado personal"));

        mockMvc.perform(json(put("/api/productos/" + productId), registerAndGetToken("ROLE_SELLER"), update))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(put("/api/productos/" + productId), seller, update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jabón de avena y miel"))
                .andExpect(jsonPath("$.stock").value(5));
        mockMvc.perform(auth(delete("/api/productos/" + productId), seller))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/productos/" + productId)).andExpect(status().isNotFound());
    }

    @Test
    void deletingAStoreDeletesItsProducts() throws Exception {
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, true);
        Long productId = createProduct(seller, storeId, "Café de Chanchamayo", "Café en grano");

        mockMvc.perform(auth(delete("/api/comercios/" + storeId), seller))
                .andExpect(status().isNoContent());
        mockMvc.perform(auth(get("/api/productos/" + productId), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAnEcoLabelRemovesItFromProducts() throws Exception {
        String admin = adminToken();
        Long labelId = idOf(mockMvc.perform(json(post("/api/eco-etiquetas"), admin, """
                        {"name": "Comercio justo %s", "keywords": ["comercio justo"]}
                        """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        String seller = registerAndGetToken("ROLE_SELLER");
        Long storeId = createStore(seller, true);
        Long productId = idOf(mockMvc.perform(json(post("/api/productos"), seller,
                        productJson(storeId, "Cacao", "Cacao").replace("\"stock\": 40", "\"stock\": 40, \"ecoLabelIds\": [" + labelId + "]")))
                .andExpect(jsonPath("$.ecoLabelIds", hasItem(labelId.intValue())))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(auth(delete("/api/eco-etiquetas/" + labelId), admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/productos/" + productId))
                .andExpect(jsonPath("$.ecoLabelIds", not(hasItem(labelId.intValue()))));
    }
}
