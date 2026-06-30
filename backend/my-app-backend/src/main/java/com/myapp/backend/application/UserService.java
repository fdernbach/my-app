package com.myapp.backend.application;

import com.myapp.backend.domain.exception.UserNotFoundException;
import com.myapp.backend.domain.model.AuditData;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;
import com.myapp.backend.domain.port.in.UserUseCase;
import com.myapp.backend.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<User> listUsers(int page, int size) {
        return userRepository.findAll(page, size);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(new User(
                UUID.randomUUID(),
                user.getLastName(),
                user.getFirstName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAddress(),
                new AuditData()
        ));
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public User updateUser(UUID id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        AuditData auditData = new AuditData();
        auditData.setVersion(existing.getAuditData().getVersion());
        return userRepository.save(new User(
                id,
                user.getLastName(),
                user.getFirstName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAddress(),
                auditData
        ));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
    }
}
