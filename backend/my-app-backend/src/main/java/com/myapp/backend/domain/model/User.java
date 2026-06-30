package com.myapp.backend.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class User {

    private UUID id;
    private String lastName;
    private String firstName;
    private String email;
    private LocalDate birthDate;
    private Address address;
    private AuditData auditData;

    public User() {}

    public User(UUID id, String lastName, String firstName, String email, LocalDate birthDate,
                Address address, AuditData auditData) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.auditData = auditData;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public AuditData getAuditData() { return auditData; }
    public void setAuditData(AuditData auditData) { this.auditData = auditData; }
}
