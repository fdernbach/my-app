package com.myapp.backend.infrastructure.persistence.adapter;

import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.port.out.CourseRepository;
import com.myapp.backend.infrastructure.persistence.entity.CourseEntity;
import com.myapp.backend.infrastructure.persistence.mapper.CourseEntityMapper;
import com.myapp.backend.infrastructure.persistence.repository.CourseJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CoursePersistenceAdapter implements CourseRepository {

    private final CourseJpaRepository jpaRepository;
    private final CourseEntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public CoursePersistenceAdapter(CourseJpaRepository jpaRepository, CourseEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<Course> findAll(int page, int size) {
        org.springframework.data.domain.Page<CourseEntity> springPage =
                jpaRepository.findAll(PageRequest.of(page, size, Sort.by("title")));
        List<Course> courses = springPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new Page<>(courses, springPage.getTotalElements(), springPage.getTotalPages(), page, size);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Course save(Course course) {
        CourseEntity saved = jpaRepository.saveAndFlush(mapper.toEntity(course));
        entityManager.refresh(saved);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
