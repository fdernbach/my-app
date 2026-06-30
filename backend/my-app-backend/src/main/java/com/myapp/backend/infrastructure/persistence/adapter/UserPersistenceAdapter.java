package com.myapp.backend.infrastructure.persistence.adapter;

import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;
import com.myapp.backend.domain.port.out.UserRepository;
import com.myapp.backend.infrastructure.persistence.entity.UserEntity;
import com.myapp.backend.infrastructure.persistence.mapper.UserEntityMapper;
import com.myapp.backend.infrastructure.persistence.repository.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository, UserEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<User> findAll(int page, int size) {
        org.springframework.data.domain.Page<UserEntity> springPage =
                jpaRepository.findAll(PageRequest.of(page, size, Sort.by("lastName")));
        List<User> users = springPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new Page<>(users, springPage.getTotalElements(), springPage.getTotalPages(), page, size);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = jpaRepository.saveAndFlush(mapper.toEntity(user));
        // refresh reloads DB state into the managed entity: necessary on update because
        // merge() overwrites createdAt/createdBy with null (they are @Column(updatable=false)
        // so not sent in the UPDATE, but the in-memory entity loses them).
        entityManager.refresh(saved);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}