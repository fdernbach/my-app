package com.myapp.backend.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@WithMockUser(username = "admin")
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_JSON = """
            {
              "userName": "jdupont",
              "lastName": "Dupont",
              "firstName": "Jean",
              "email": "jean.dupont@example.com",
              "birthDate": "1985-06-15",
              "address": {
                "streetNumber": "10",
                "streetName": "Rue de Rivoli",
                "postalCode": "75001",
                "city": "Paris",
                "country": "France"
              }
            }
            """;

    // ── POST /users ──────────────────────────────────────────────────────────

    @Test
    void createUser_returns201_withPopulatedBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.birthDate").value("1985-06-15"))
                .andExpect(jsonPath("$.address.city").value("Paris"))
                .andExpect(jsonPath("$.auditData.createdBy").value("admin"))
                .andExpect(jsonPath("$.auditData.updatedBy").value("admin"))
                .andExpect(jsonPath("$.auditData.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.auditData.version").value(0));
    }

    @Test
    void createUser_returns400_withProblemDetail_whenLastNameMissing() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userName": "jdupont", "firstName": "Jean", "email": "jean@example.com" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-error"))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("lastName"));
    }

    @Test
    void createUser_returns400_withProblemDetail_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── GET /users ───────────────────────────────────────────────────────────

    @Test
    void listUsers_returns200_emptyPage_initially() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void listUsers_returns200_withCreatedUser() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listUsers_returnsPaginatedResults() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(USER_JSON));
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("""
                { "userName": "smartin", "lastName": "Martin", "firstName": "Sophie", "email": "sophie.martin@example.com" }
                """));

        mockMvc.perform(get("/users").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    // ── GET /users/{id} ──────────────────────────────────────────────────────

    @Test
    void getUserById_returns200_whenExists() throws Exception {
        String id = extractId(mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.lastName").value("Dupont"));
    }

    @Test
    void getUserById_returns404_withProblemDetail_whenNotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem:user-not-found"))
                .andExpect(jsonPath("$.title").value("User Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").isNotEmpty());
    }

    // ── PUT /users/{id} ──────────────────────────────────────────────────────

    @Test
    void updateUser_returns200_withUpdatedFieldsAndIncrementedVersion() throws Exception {
        String id = extractId(mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "userName": "jdupont", "lastName": "Durand", "firstName": "Pierre", "email": "pierre.durand@example.com" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Durand"))
                .andExpect(jsonPath("$.firstName").value("Pierre"))
                .andExpect(jsonPath("$.auditData.version").value(1))
                .andExpect(jsonPath("$.auditData.createdBy").value("admin"))
                .andExpect(jsonPath("$.auditData.updatedBy").value("admin"));
    }

    @Test
    void updateUser_returns404_whenNotFound() throws Exception {
        mockMvc.perform(put("/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USER_JSON))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /users/{id} ───────────────────────────────────────────────────

    @Test
    void deleteUser_returns204_andUserIsGone() throws Exception {
        String id = extractId(mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returns404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── Sécurité ─────────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    void anyRequest_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractId(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("id").asText();
    }
}
