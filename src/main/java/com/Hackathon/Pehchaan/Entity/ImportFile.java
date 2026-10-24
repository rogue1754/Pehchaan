
package com.Hackathon.Pehchaan.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_files")
public class ImportFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_session_id", nullable = false)
    private ImportSession importSession;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    private Long fileSize;

    @Lob
    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private String status = "UPLOADED";

    private LocalDateTime uploadedAt = LocalDateTime.now();

    public ImportFile() {
    }

    public Long getId() {
        return id;
    }

    public ImportSession getImportSession() {
        return importSession;
    }

    public void setImportSession(ImportSession importSession) {
        this.importSession = importSession;
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

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    @JsonIgnore
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
