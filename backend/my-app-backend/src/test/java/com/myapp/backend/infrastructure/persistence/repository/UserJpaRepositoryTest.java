package com.myapp.backend.infrastructure.persistence.repository;

import com.myapp.backend.infrastructure.persistence.entity.AddressEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.AuditDataEmbeddable;
import com.myapp.backend.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@WithMockUser(username = "testuser")
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_persistsUserWithMandatoryFields() {
        UserEntity saved = repository.save(buildUser());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLastName()).isEqualTo("Dupont");
        assertThat(saved.getFirstName()).isEqualTo("Jean");
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1985, 6, 15));
    }

    @Test
    void save_setsVersionToZero_onFirstPersist() {
        UserEntity saved = repository.save(buildUser());

        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void save_persistsAuditData() {
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        repository.saveAndFlush(buildUser());
        entityManager.clear();

        UserEntity found = repository.findAll().getFirst();
        assertThat(found.getAuditData().getCreatedBy()).isEqualTo("testuser");
        assertThat(found.getAuditData().getUpdatedBy()).isEqualTo("testuser");
        assertThat(found.getAuditData().getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(found.getAuditData().getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void save_persistsAddress() {
        repository.saveAndFlush(buildUser());
        entityManager.clear();

        UserEntity found = repository.findAll().getFirst();

        assertThat(found.getAddress().getStreetNumber()).isEqualTo("10");
        assertThat(found.getAddress().getStreetName()).isEqualTo("Rue de Rivoli");
        assertThat(found.getAddress().getPostalCode()).isEqualTo("75001");
        assertThat(found.getAddress().getCity()).isEqualTo("Paris");
        assertThat(found.getAddress().getCountry()).isEqualTo("France");
    }

    @Test
    void findById_returnsUser_whenExists() {
        UserEntity saved = repository.save(buildUser());

        Optional<UserEntity> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getLastName()).isEqualTo("Dupont");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        Optional<UserEntity> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllPersistedUsers() {
        repository.save(buildUser());
        repository.save(buildUser(UUID.randomUUID(), "Martin", "Sophie"));

        List<UserEntity> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_removesUser() {
        UserEntity saved = repository.save(buildUser());

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void update_incrementsVersion() {
        UserEntity saved = repository.saveAndFlush(buildUser());
        assertThat(saved.getVersion()).isZero();

        saved.setLastName("NouveauNom");
        UserEntity updated = repository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void update_doesNotModifyCreatedAtAndCreatedBy() {
        // Création avec "testuser" (from class-level @WithMockUser)
        UserEntity entity = buildUser();
        repository.saveAndFlush(entity);
        entityManager.clear();

        // Passage à "editor" pour la mise à jour
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("editor", "password", Collections.emptyList())
        );

        UserEntity toUpdate = repository.findById(entity.getId()).orElseThrow();
        toUpdate.setLastName("NouveauNom");
        toUpdate.getAuditData().setCreatedBy("hacker"); // ignoré par @Column(updatable=false)
        repository.saveAndFlush(toUpdate);
        entityManager.clear();

        UserEntity found = repository.findById(entity.getId()).orElseThrow();
        assertThat(found.getLastName()).isEqualTo("NouveauNom");
        assertThat(found.getAuditData().getUpdatedBy()).isEqualTo("editor");
        assertThat(found.getAuditData().getCreatedBy()).isEqualTo("testuser"); // inchangé en DB
    }

    private UserEntity buildUser() {
        return buildUser(UUID.randomUUID(), "Dupont", "Jean");
    }

    private UserEntity buildUser(UUID id, String lastName, String firstName) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setLastName(lastName);
        entity.setFirstName(firstName);
        entity.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
        entity.setBirthDate(LocalDate.of(1985, 6, 15));

        AddressEmbeddable address = new AddressEmbeddable();
        address.setStreetNumber("10");
        address.setStreetName("Rue de Rivoli");
        address.setPostalCode("75001");
        address.setCity("Paris");
        address.setCountry("France");
        entity.setAddress(address);

        entity.setAuditData(new AuditDataEmbeddable());

        return entity;
    }
}
