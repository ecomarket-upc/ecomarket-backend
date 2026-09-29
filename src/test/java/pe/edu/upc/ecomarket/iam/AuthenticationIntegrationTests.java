package pe.edu.upc.ecomarket.iam;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pe.edu.upc.ecomarket.shared.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationIntegrationTests extends IntegrationTestSupport {

    @Test
    void registerReturnsTokenAndNeverThePassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(uniqueEmail("lucia"), "ROLE_CONSUMER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ROLE_CONSUMER"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void registerRejectsDuplicatedEmail() throws Exception {
        String email = uniqueEmail("duplicado");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(email, "ROLE_CONSUMER")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(email, "ROLE_SELLER")))
                .andExpect(status().isConflict());
    }

    @Test
    void registerRejectsAdminRole() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(uniqueEmail("hacker"), "ROLE_ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerValidatesFields() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "", "lastName": "Quispe", "email": "no-es-correo", "password": "corta", "role": "ROLE_CONSUMER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Incorrecta1"}
                                """.formatted(ADMIN_EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos"));
    }

    @Test
    void meRequiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsTheAuthenticatedUser() throws Exception {
        String token = registerAndGetToken("ROLE_SELLER");
        mockMvc.perform(auth(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_SELLER"));
    }

    @Test
    void onlyAdminCanListUsers() throws Exception {
        mockMvc.perform(auth(get("/api/usuarios"), registerAndGetToken("ROLE_CONSUMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(auth(get("/api/usuarios"), adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotEditAnotherProfile() throws Exception {
        String admin = adminToken();
        Long adminId = idOf(mockMvc.perform(auth(get("/api/auth/me"), admin))
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(json(put("/api/usuarios/" + adminId), registerAndGetToken("ROLE_CONSUMER"), """
                        {"firstName": "Otro", "lastName": "Nombre"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivatedUserCannotSignIn() throws Exception {
        String email = uniqueEmail("inactivo");
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson(email, "ROLE_CONSUMER")))
                .andReturn().getResponse().getContentAsString();
        Long userId = ((Number) JsonPath.read(body, "$.user.id")).longValue();

        mockMvc.perform(auth(delete("/api/usuarios/" + userId), adminToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("La cuenta está desactivada"));
    }
}
