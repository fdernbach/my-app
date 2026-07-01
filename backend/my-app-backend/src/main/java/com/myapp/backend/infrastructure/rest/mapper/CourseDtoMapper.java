package com.myapp.backend.infrastructure.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.Course;
import com.myapp.backend.domain.model.Page;
import com.micro.securityregister.model.CoursePage;
import com.micro.securityregister.model.CourseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class CourseDtoMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "auditData", ignore = true)
    @Mapping(target = "documentJson", source = "documentJson", qualifiedByName = "objectToJson")
    public abstract Course toDomain(CourseRequest request);

    @Mapping(target = "documentJson", source = "documentJson", qualifiedByName = "jsonToObject")
    public abstract com.micro.securityregister.model.Course toDto(Course course);

    public abstract AuditData toDomain(com.micro.securityregister.model.AuditData dto);
    public abstract com.micro.securityregister.model.AuditData toDto(AuditData auditData);

    public CoursePage toPageDto(Page<Course> page) {
        CoursePage dto = new CoursePage();
        dto.setContent(page.getContent().stream().map(this::toDto).toList());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setPageNumber(page.getPageNumber());
        dto.setPageSize(page.getPageSize());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        return dto;
    }

    @Named("objectToJson")
    protected String objectToJson(Map<String, Object> obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("documentJson is not valid JSON", e);
        }
    }

    @Named("jsonToObject")
    @SuppressWarnings("unchecked")
    protected Map<String, Object> jsonToObject(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("documentJson is not valid JSON", e);
        }
    }
}
