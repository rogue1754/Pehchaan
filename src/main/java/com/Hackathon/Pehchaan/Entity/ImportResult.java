
package com.Hackathon.Pehchaan.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "import_results")
public class ImportResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_session_id", nullable = false)
    private ImportSession importSession;

    private String name;
    private String source;
    private String date;
    private String status;
    private String category;
    private String detail;

    @Column(nullable = false)
    private boolean confirmed = false;

    public ImportResult() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
