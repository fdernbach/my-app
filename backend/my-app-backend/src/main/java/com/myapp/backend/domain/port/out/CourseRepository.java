package com.myapp.backend.domain.port.out;

import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;

import java.util.Optional;

public interface CourseRepository {
    Page<Course> findAll(int page, int size);
    Optional<Course> findById(Long id);
    Course save(Course course);
    void deleteById(Long id);
}
