package com.myapp.backend.infrastructure.persistence.mapper;

import com.myapp.backend.domain.model.Address;
import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.User;
import com.myapp.backend.infrastructure.persistence.entity.AddressEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.AuditDataEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    @Mapping(target = "version", source = "auditData.version")
    UserEntity toEntity(User user);

    // MapStruct uses toDomain(AuditDataEmbeddable) for entity.auditData → user.auditData,
    // then @AfterMapping sets version from entity.version (@Version field).
    User toDomain(UserEntity entity);

    @AfterMapping
    default void setAuditDataVersion(@MappingTarget User user, UserEntity entity) {
        AuditData auditData = user.getAuditData();
        if (auditData == null) {
            auditData = new AuditData();
            user.setAuditData(auditData);
        }
        auditData.setVersion(entity.getVersion());
    }

    AddressEmbeddable toEmbeddable(Address address);
    Address toDomain(AddressEmbeddable embeddable);

    AuditDataEmbeddable toEmbeddable(AuditData auditData);
    AuditData toDomain(AuditDataEmbeddable embeddable);
}
