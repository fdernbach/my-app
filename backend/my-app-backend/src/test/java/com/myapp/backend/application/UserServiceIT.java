package com.myapp.backend.application;

import com.myapp.backend.domain.exception.UserNotFoundException;
import com.myapp.backend.domain.model.Address;
import com.myapp.backend.domain.model.Page;
import com.myapp.backend.domain.model.User;
import com.myapp.backend.domain.port.in.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
@WithMockUser(username = "testuser")
class UserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserUseCase userService;

    @Test
    void createUser_assignsId() {
        User created = userService.createUser(buildRequest());

        assertThat(created.getId()).isNotNull();
    }

    @Test
    void createUser_populatesAuditFields() {
        User created = userService.createUser(buildRequest());

        assertThat(created.getAuditData()).isNotNull();
        assertThat(created.getAuditData().getCreatedAt()).isNotNull();
        assertThat(created.getAuditData().getCreatedBy()).isEqualTo("testuser");
        assertThat(created.getAuditData().getUpdatedAt()).isNotNull();
        assertThat(created.getAuditData().getUpdatedBy()).isEqualTo("testuser");
    }

    @Test
    void createUser_setsVersionToZero() {
        User created = userService.createUser(buildRequest());

        assertThat(created.getAuditData().getVersion()).isZero();
    }

    @Test
    void getUserById_returnsUser_whenExists() {
        User created = userService.createUser(buildRequest());

        User found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getLastName()).isEqualTo("Dupont");
        assertThat(found.getFirstName()).isEqualTo("Jean");
    }

    @Test
    void getUserById_throwsUserNotFoundException_whenNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.getUserById(unknownId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUsers_returnsAllCreatedUsers() {
        userService.createUser(buildRequest("Dupont", "Jean"));
        userService.createUser(buildRequest("Martin", "Sophie"));

        Page<User> page = userService.listUsers(0, 10);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void listUsers_respectsPageSizeAndNumber() {
        userService.createUser(buildRequest("Dupont", "Jean"));
        userService.createUser(buildRequest("Martin", "Sophie"));
        userService.createUser(buildRequest("Durand", "Pierre"));

        Page<User> firstPage = userService.listUsers(0, 2);
        Page<User> secondPage = userService.listUsers(1, 2);

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.isLast()).isTrue();
    }

    @Test
    void updateUser_updatesLastNameAndFirstName() {
        User created = userService.createUser(buildRequest("Dupont", "Jean"));
        User updateRequest = buildRequest("Durand", "Pierre");

        User updated = userService.updateUser(created.getId(), updateRequest);

        assertThat(updated.getLastName()).isEqualTo("Durand");
        assertThat(updated.getFirstName()).isEqualTo("Pierre");
    }

    @Test
    @WithMockUser(username = "creator")
    void updateUser_preservesCreatedAudit() {
        User created = userService.createUser(buildRequest());
        // le updateUser se fait avec un autre utilisateur
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "editor", "password", java.util.Collections.emptyList()));

        User updated = userService.updateUser(created.getId(), buildRequest());

        assertThat(updated.getAuditData().getCreatedBy()).isEqualTo("creator");
        assertThat(updated.getAuditData().getCreatedAt()).isEqualTo(created.getAuditData().getCreatedAt());
    }

    @Test
    @WithMockUser(username = "creator")
    void updateUser_setsUpdatedByToCurrentUser() {
        User created = userService.createUser(buildRequest());
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "editor", "password", java.util.Collections.emptyList()));

        User updated = userService.updateUser(created.getId(), buildRequest());

        assertThat(updated.getAuditData().getUpdatedBy()).isEqualTo("editor");
        assertThat(updated.getAuditData().getUpdatedAt()).isNotNull();
    }

    @Test
    void updateUser_incrementsVersion() {
        User created = userService.createUser(buildRequest());

        User updated = userService.updateUser(created.getId(), buildRequest("Durand", "Pierre"));

        assertThat(updated.getAuditData().getVersion()).isEqualTo(1L);
    }

    @Test
    void updateUser_throwsUserNotFoundException_whenNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.updateUser(unknownId, buildRequest()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_removesUser() {
        User created = userService.createUser(buildRequest());

        userService.deleteUser(created.getId());

        assertThatThrownBy(() -> userService.getUserById(created.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_throwsUserNotFoundException_whenNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.deleteUser(unknownId))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User buildRequest() {
        return buildRequest("Dupont", "Jean");
    }

    private User buildRequest(String lastName, String firstName) {
        User user = new User();
        user.setUserName(String.valueOf(firstName.toLowerCase().charAt(0)) + lastName.toLowerCase());
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
        user.setBirthDate(LocalDate.of(1985, 6, 15));

        Address address = new Address();
        address.setStreetNumber("10");
        address.setStreetName("Rue de Rivoli");
        address.setPostalCode("75001");
        address.setCity("Paris");
        address.setCountry("France");
        user.setAddress(address);

        return user;
    }
}
