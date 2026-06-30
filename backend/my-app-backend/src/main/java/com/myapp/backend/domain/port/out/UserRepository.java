package com.myapp.backend.domain.port.out;

import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Page<User> findAll(int page, int size);
    Optional<User> findById(UUID id);
    boolean existsByUserName(String userName);
    User save(User user);
    void deleteById(UUID id);
}