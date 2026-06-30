package com.myapp.backend.infrastructure.rest.mapper;

import com.myapp.backend.domain.model.Address;
import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "auditData", ignore = true)
    User toDomain(com.micro.securityregister.model.UserRequest request);

    com.micro.securityregister.model.User toDto(User user);

    Address toDomain(com.micro.securityregister.model.Address address);
    com.micro.securityregister.model.Address toDto(Address address);

    AuditData toDomain(com.micro.securityregister.model.AuditData dto);
    com.micro.securityregister.model.AuditData toDto(AuditData auditData);
}
