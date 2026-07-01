package com.myapp.backend.infrastructure.persistence.repository;

import com.myapp.backend.infrastructure.persistence.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository extends JpaRepository<CourseEntity, Long> {}
