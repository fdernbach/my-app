package com.myapp.backend.infrastructure.rest;

import com.micro.securityregister.api.CoursesApi;
import com.micro.securityregister.model.Course;
import com.micro.securityregister.model.CoursePage;
import com.micro.securityregister.model.CourseRequest;
import com.myapp.backend.domain.port.in.CourseUseCase;
import com.myapp.backend.infrastructure.rest.mapper.CourseDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseController implements CoursesApi {

    private final CourseUseCase courseUseCase;
    private final CourseDtoMapper mapper;

    public CourseController(CourseUseCase courseUseCase, CourseDtoMapper mapper) {
        this.courseUseCase = courseUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<CoursePage> listCourses(Integer page, Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        return ResponseEntity.ok(mapper.toPageDto(courseUseCase.listCourses(p, s)));
    }

    @Override
    public ResponseEntity<Course> createCourse(CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDto(courseUseCase.createCourse(mapper.toDomain(request))));
    }

    @Override
    public ResponseEntity<Course> getCourseById(Long id) {
        return ResponseEntity.ok(mapper.toDto(courseUseCase.getCourseById(id)));
    }

    @Override
    public ResponseEntity<Course> updateCourse(Long id, CourseRequest request) {
        return ResponseEntity.ok(mapper.toDto(courseUseCase.updateCourse(id, mapper.toDomain(request))));
    }

    @Override
    public ResponseEntity<Void> deleteCourse(Long id) {
        courseUseCase.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
