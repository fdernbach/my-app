package com.myapp.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_json", columnDefinition = "jsonb")
    private String documentJson;

    @Embedded
    private AuditDataEmbeddable auditData;

    public CourseEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }

    public AuditDataEmbeddable getAuditData() { return auditData; }
    public void setAuditData(AuditDataEmbeddable auditData) { this.auditData = auditData; }
}
