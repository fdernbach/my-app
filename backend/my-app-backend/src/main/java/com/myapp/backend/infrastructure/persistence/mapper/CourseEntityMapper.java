package com.myapp.backend.infrastructure.persistence.mapper;

import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.Course;
import com.myapp.backend.infrastructure.persistence.entity.AuditDataEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.CourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseEntityMapper {

    @Mapping(target = "auditData", source = "auditData")
    Course toDomain(CourseEntity entity);

    @Mapping(target = "auditData", source = "auditData")
    CourseEntity toEntity(Course course);

    AuditData toDomain(AuditDataEmbeddable embeddable);
    AuditDataEmbeddable toEmbeddable(AuditData auditData);
}
