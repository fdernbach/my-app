package com.myapp.backend.application;

import com.myapp.backend.domain.exception.CourseNotFoundException;
import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.port.in.CourseUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
@WithMockUser(username = "testuser")
class CourseServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private CourseUseCase courseService;

    @Test
    void createCourse_assignsId() {
        Course created = courseService.createCourse(buildRequest("Spring Boot", "jdupont"));

        assertThat(created.getId()).isNotNull();
    }

    @Test
    void createCourse_populatesAuditFields() {
        Course created = courseService.createCourse(buildRequest("Spring Boot", "jdupont"));

        assertThat(created.getAuditData()).isNotNull();
        assertThat(created.getAuditData().getCreatedAt()).isNotNull();
        assertThat(created.getAuditData().getCreatedBy()).isEqualTo("testuser");
        assertThat(created.getAuditData().getUpdatedAt()).isNotNull();
        assertThat(created.getAuditData().getUpdatedBy()).isEqualTo("testuser");
    }

    @Test
    void getCourseById_returnsCourse_whenExists() {
        Course created = courseService.createCourse(buildRequest("Spring Boot", "jdupont"));

        Course found = courseService.getCourseById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getTitle()).isEqualTo("Spring Boot");
        assertThat(found.getAuthor()).isEqualTo("jdupont");
    }

    @Test
    void getCourseById_throwsCourseNotFoundException_whenNotFound() {
        assertThatThrownBy(() -> courseService.getCourseById(9999L))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void listCourses_returnsAllCreatedCourses() {
        courseService.createCourse(buildRequest("Spring Boot", "jdupont"));
        courseService.createCourse(buildRequest("Angular 18", "smartin"));

        Page<Course> page = courseService.listCourses(0, 10);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void listCourses_respectsPageSizeAndNumber() {
        courseService.createCourse(buildRequest("Angular 18", "jdupont"));
        courseService.createCourse(buildRequest("Spring Boot", "jdupont"));
        courseService.createCourse(buildRequest("Vue.js", "smartin"));

        Page<Course> firstPage = courseService.listCourses(0, 2);
        Page<Course> secondPage = courseService.listCourses(1, 2);

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.isLast()).isTrue();
    }

    @Test
    void updateCourse_updatesTitleAndAuthor() {
        Course created = courseService.createCourse(buildRequest("Spring Boot", "jdupont"));

        Course updated = courseService.updateCourse(created.getId(), buildRequest("Spring Boot 3", "smartin"));

        assertThat(updated.getTitle()).isEqualTo("Spring Boot 3");
        assertThat(updated.getAuthor()).isEqualTo("smartin");
    }

    @Test
    void updateCourse_throwsCourseNotFoundException_whenNotFound() {
        assertThatThrownBy(() -> courseService.updateCourse(9999L, buildRequest("Title", "author")))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void deleteCourse_removesCourse() {
        Course created = courseService.createCourse(buildRequest("Spring Boot", "jdupont"));

        courseService.deleteCourse(created.getId());

        assertThatThrownBy(() -> courseService.getCourseById(created.getId()))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void deleteCourse_throwsCourseNotFoundException_whenNotFound() {
        assertThatThrownBy(() -> courseService.deleteCourse(9999L))
                .isInstanceOf(CourseNotFoundException.class);
    }

    private Course buildRequest(String title, String author) {
        Course course = new Course();
        course.setTitle(title);
        course.setAuthor(author);
        return course;
    }
}
