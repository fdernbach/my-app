package com.myapp.backend.domain.model;

public class Course {

    private Long id;
    private String title;
    private String author;
    private String documentJson;
    private AuditData auditData;

    public Course() {}

    public Course(Long id, String title, String author, String documentJson, AuditData auditData) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.documentJson = documentJson;
        this.auditData = auditData;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }

    public AuditData getAuditData() { return auditData; }
    public void setAuditData(AuditData auditData) { this.auditData = auditData; }
}
