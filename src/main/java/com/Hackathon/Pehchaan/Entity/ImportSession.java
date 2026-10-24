
package com.Hackathon.Pehchaan.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_sessions")
public class ImportSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status = "PENDING";

    private String fileName;

    private String fileType;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ImportSession() {
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
