Feature: User management

  Scenario: Create a new user
    Given no user exists with username "jdupont"
    When I create a user with username "jdupont" and email "j.dupont@example.com"
    Then a user exists with username "jdupont"

  Scenario: Find a user by id
    Given a user exists with username "mmartin" and email "m.martin@example.com"
    When I look up that user by id
    Then the user found has username "mmartin"

  Scenario: Delete an existing user
    Given a user exists with username "todelete" and email "del@example.com"
    When I delete that user
    Then no user with username "todelete" should exist
