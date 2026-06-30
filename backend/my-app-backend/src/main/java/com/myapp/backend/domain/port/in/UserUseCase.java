package com.myapp.backend.domain.port.in;

import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;

import java.util.UUID;

public interface UserUseCase {
    Page<User> listUsers(int page, int size);
    User createUser(User user);
    User getUserById(UUID id);
    User updateUser(UUID id, User user);
    void deleteUser(UUID id);
}