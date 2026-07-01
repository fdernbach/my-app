package com.myapp.backend.application;

import com.myapp.backend.domain.exception.CourseNotFoundException;
import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.port.in.CourseUseCase;
import com.myapp.backend.domain.port.out.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Page<Course> listCourses(int page, int size) {
        return courseRepository.findAll(page, size);
    }

    @Override
    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(new Course(
                null,
                course.getTitle(),
                course.getAuthor(),
                course.getDocumentJson(),
                new AuditData()
        ));
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course course) {
        getCourseById(id);
        return courseRepository.save(new Course(
                id,
                course.getTitle(),
                course.getAuthor(),
                course.getDocumentJson(),
                new AuditData()
        ));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        getCourseById(id);
        courseRepository.deleteById(id);
    }
}
