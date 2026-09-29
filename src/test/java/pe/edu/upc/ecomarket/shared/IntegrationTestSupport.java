package pe.edu.upc.ecomarket.shared;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for API tests: boots the whole application on H2 and offers helpers to
 * register users and send authenticated JSON requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    protected static final String ADMIN_EMAIL = "admin@test.pe";
    protected static final String ADMIN_PASSWORD = "Admin12345!";
    protected static final String PASSWORD = "Clave2026";

    @Autowired
    protected MockMvc mockMvc;

    /** Unique email so tests do not collide in the shared in-memory database. */
    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.pe";
    }

    protected static String signUpJson(String email, String role) {
        return """
                {"firstName": "Lucía", "lastName": "Quispe", "email": "%s", "password": "%s", "role": "%s"}
                """.formatted(email, PASSWORD, role);
    }

    /** Registers a new user with the given role and returns its token. */
    protected String registerAndGetToken(String role) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(uniqueEmail(role.toLowerCase()), role)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected String adminToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    /** Adds the bearer token and a JSON body to a request. */
    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String token, String body) {
        request.contentType(MediaType.APPLICATION_JSON).content(body);
        return auth(request, token);
    }

    protected static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    protected static Long idOf(String responseBody) {
        return ((Number) JsonPath.read(responseBody, "$.id")).longValue();
    }
}
