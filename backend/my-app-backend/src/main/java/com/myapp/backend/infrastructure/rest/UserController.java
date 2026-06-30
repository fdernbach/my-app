package com.myapp.backend.infrastructure.rest;

import com.micro.securityregister.api.UsersApi;
import com.micro.securityregister.model.UserPage;
import com.micro.securityregister.model.UserRequest;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;
import com.myapp.backend.domain.port.in.UserUseCase;
import com.myapp.backend.infrastructure.rest.mapper.UserDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserController implements UsersApi {

    private final UserUseCase userUseCase;
    private final UserDtoMapper mapper;

    public UserController(UserUseCase userUseCase, UserDtoMapper mapper) {
        this.userUseCase = userUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<UserPage> listUsers(Integer page, Integer size) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        Page<User> domainPage = userUseCase.listUsers(p, s);

        UserPage dto = new UserPage();
        dto.setContent(domainPage.getContent().stream().map(mapper::toDto).toList());
        dto.setTotalElements(domainPage.getTotalElements());
        dto.setTotalPages(domainPage.getTotalPages());
        dto.setPageNumber(domainPage.getPageNumber());
        dto.setPageSize(domainPage.getPageSize());
        dto.setFirst(domainPage.isFirst());
        dto.setLast(domainPage.isLast());

        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<com.micro.securityregister.model.User> createUser(UserRequest userRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDto(userUseCase.createUser(mapper.toDomain(userRequest))));
    }

    @Override
    public ResponseEntity<com.micro.securityregister.model.User> getUserById(UUID id) {
        return ResponseEntity.ok(mapper.toDto(userUseCase.getUserById(id)));
    }

    @Override
    public ResponseEntity<com.micro.securityregister.model.User> updateUser(UUID id, UserRequest userRequest) {
        return ResponseEntity.ok(mapper.toDto(userUseCase.updateUser(id, mapper.toDomain(userRequest))));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
