package com.myapp.backend.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@WithMockUser(username = "admin")
class CourseControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String COURSE_JSON = """
            {
              "title": "Introduction à Spring Boot",
              "author": "jdupont",
              "documentJson": { "chapters": 10, "language": "fr" }
            }
            """;

    // ── POST /courses ─────────────────────────────────────────────────────────

    @Test
    void createCourse_returns201_withPopulatedBody() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Introduction à Spring Boot"))
                .andExpect(jsonPath("$.author").value("jdupont"))
                .andExpect(jsonPath("$.documentJson.chapters").value(10))
                .andExpect(jsonPath("$.auditData.createdBy").value("admin"))
                .andExpect(jsonPath("$.auditData.updatedBy").value("admin"))
                .andExpect(jsonPath("$.auditData.createdAt").isNotEmpty());
    }

    @Test
    void createCourse_returns400_withProblemDetail_whenTitleMissing() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "author": "jdupont" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-error"))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void createCourse_returns400_withProblemDetail_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── GET /courses ──────────────────────────────────────────────────────────

    @Test
    void listCourses_returns200_emptyPage_initially() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void listCourses_returns200_withCreatedCourse() throws Exception {
        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(COURSE_JSON));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Introduction à Spring Boot"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listCourses_returnsPaginatedResults() throws Exception {
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON).content(COURSE_JSON));
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON).content("""
                { "title": "Angular 18", "author": "smartin" }
                """));

        mockMvc.perform(get("/courses").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    // ── GET /courses/{id} ─────────────────────────────────────────────────────

    @Test
    void getCourseById_returns200_whenExists() throws Exception {
        long id = extractId(mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(COURSE_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/courses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Introduction à Spring Boot"));
    }

    @Test
    void getCourseById_returns404_withProblemDetail_whenNotFound() throws Exception {
        mockMvc.perform(get("/courses/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem:course-not-found"))
                .andExpect(jsonPath("$.title").value("Course Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").isNotEmpty());
    }

    // ── PUT /courses/{id} ─────────────────────────────────────────────────────

    @Test
    void updateCourse_returns200_withUpdatedFields() throws Exception {
        long id = extractId(mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(COURSE_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Spring Boot 3 Avancé", "author": "smartin" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot 3 Avancé"))
                .andExpect(jsonPath("$.author").value("smartin"))
                .andExpect(jsonPath("$.auditData.createdBy").value("admin"))
                .andExpect(jsonPath("$.auditData.updatedBy").value("admin"));
    }

    @Test
    void updateCourse_returns404_whenNotFound() throws Exception {
        mockMvc.perform(put("/courses/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /courses/{id} ──────────────────────────────────────────────────

    @Test
    void deleteCourse_returns204_andCourseIsGone() throws Exception {
        long id = extractId(mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(COURSE_JSON))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(delete("/courses/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/courses/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCourse_returns404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/courses/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    // ── Sécurité ──────────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    void anyRequest_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long extractId(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
