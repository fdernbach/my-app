package com.myapp.backend.infrastructure.persistence.repository;

import com.myapp.backend.infrastructure.persistence.entity.AuditDataEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.CourseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@WithMockUser(username = "testuser")
class CourseJpaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CourseJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_persistsCourseWithMandatoryFields() {
        CourseEntity saved = repository.save(buildCourse("Spring Boot", "jdupont"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Spring Boot");
        assertThat(saved.getAuthor()).isEqualTo("jdupont");
    }

    @Test
    void save_persistsAuditData() {
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        repository.saveAndFlush(buildCourse("Spring Boot", "jdupont"));
        entityManager.clear();

        CourseEntity found = repository.findAll().getFirst();
        assertThat(found.getAuditData().getCreatedBy()).isEqualTo("testuser");
        assertThat(found.getAuditData().getUpdatedBy()).isEqualTo("testuser");
        assertThat(found.getAuditData().getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(found.getAuditData().getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void save_persistsDocumentJson() {
        CourseEntity entity = buildCourse("Spring Boot", "jdupont");
        entity.setDocumentJson("{\"chapters\": 10}");
        repository.saveAndFlush(entity);
        entityManager.clear();

        CourseEntity found = repository.findAll().getFirst();
        assertThat(found.getDocumentJson()).contains("chapters");
    }

    @Test
    void findById_returnsCourse_whenExists() {
        CourseEntity saved = repository.save(buildCourse("Spring Boot", "jdupont"));

        Optional<CourseEntity> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        Optional<CourseEntity> result = repository.findById(9999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllPersistedCourses() {
        repository.save(buildCourse("Spring Boot", "jdupont"));
        repository.save(buildCourse("Angular 18", "smartin"));

        List<CourseEntity> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_removesCourse() {
        CourseEntity saved = repository.save(buildCourse("Spring Boot", "jdupont"));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    private CourseEntity buildCourse(String title, String author) {
        CourseEntity entity = new CourseEntity();
        entity.setTitle(title);
        entity.setAuthor(author);
        entity.setAuditData(new AuditDataEmbeddable());
        return entity;
    }
}
