package com.myapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Embedded
    private AddressEmbeddable address;

    @Embedded
    private AuditDataEmbeddable auditData;

    @Version
    private Long version;

    public UserEntity() {}

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

    public AddressEmbeddable getAddress() { return address; }
    public void setAddress(AddressEmbeddable address) { this.address = address; }

    public AuditDataEmbeddable getAuditData() { return auditData; }
    public void setAuditData(AuditDataEmbeddable auditData) { this.auditData = auditData; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
