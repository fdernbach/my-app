package com.myapp.backend.bdd.steps;

import com.myapp.backend.domain.model.User;
import com.myapp.backend.domain.port.in.UserUseCase;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserSteps {

    @Autowired
    private UserUseCase userUseCase;

    private User lastCreatedUser;

    // ── Givens ────────────────────────────────────────────────────────────────

    @Given("no user exists with username {string}")
    public void noUserExistsWithUsername(String username) {
        userUseCase.listUsers(0, 100).getContent().stream()
                .filter(u -> u.getUserName().equals(username))
                .map(User::getId)
                .forEach(userUseCase::deleteUser);
    }

    @Given("a user exists with username {string} and email {string}")
    public void aUserExistsWithUsernameAndEmail(String username, String email) {
        User user = buildUser(username, email);
        lastCreatedUser = userUseCase.createUser(user);
    }

    // ── Whens ─────────────────────────────────────────────────────────────────

    @When("I create a user with username {string} and email {string}")
    public void iCreateAUserWithUsernameAndEmail(String username, String email) {
        lastCreatedUser = userUseCase.createUser(buildUser(username, email));
    }

    @When("I look up that user by id")
    public void iLookUpThatUserById() {
        lastCreatedUser = userUseCase.getUserById(lastCreatedUser.getId());
    }

    @When("I delete that user")
    public void iDeleteThatUser() {
        UUID id = lastCreatedUser.getId();
        userUseCase.deleteUser(id);
        lastCreatedUser = null;
    }

    // ── Thens ─────────────────────────────────────────────────────────────────

    @Then("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        assertThat(lastCreatedUser).isNotNull();
        assertThat(lastCreatedUser.getUserName()).isEqualTo(username);
        assertThat(lastCreatedUser.getId()).isNotNull();
    }

    @Then("the user found has username {string}")
    public void theUserFoundHasUsername(String username) {
        assertThat(lastCreatedUser).isNotNull();
        assertThat(lastCreatedUser.getUserName()).isEqualTo(username);
    }

    @Then("no user with username {string} should exist")
    public void noUserWithUsernameShouldExist(String username) {
        boolean exists = userUseCase.listUsers(0, 100).getContent().stream()
                .anyMatch(u -> u.getUserName().equals(username));
        assertThat(exists).isFalse();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUserName(username);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        return user;
    }
}
