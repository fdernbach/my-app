package com.myapp.backend.domain.port.in;

import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;

public interface CourseUseCase {
    Page<Course> listCourses(int page, int size);
    Course createCourse(Course course);
    Course getCourseById(Long id);
    Course updateCourse(Long id, Course course);
    void deleteCourse(Long id);
}
