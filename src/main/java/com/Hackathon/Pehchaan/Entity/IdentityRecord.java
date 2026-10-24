
package com.Hackathon.Pehchaan.Entity;

import com.Hackathon.Pehchaan.Enums.RecordStatus;
import com.Hackathon.Pehchaan.Enums.ReportCategory;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "identity_records")
public class IdentityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String source;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RecordStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReportCategory category;

    @Column(length = 1000)
    private String detail;

    public IdentityRecord() {
    }

    public IdentityRecord(
            String name,
            String source,
            LocalDate date,
            RecordStatus status,
            ReportCategory category,
            String detail) {
        this.name = name;
        this.source = source;
        this.date = date;
        this.status = status;
        this.category = category;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
