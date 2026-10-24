
package com.Hackathon.Pehchaan.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "guidance_progress",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"guidance_type", "step_key"}
        )
)
public class GuidanceProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guidance_type", nullable = false)
    private String guidanceType;

    @Column(name = "step_key", nullable = false)
    private String stepKey;

    @Column(nullable = false)
    private boolean completed;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public GuidanceProgress() {
    }

    public Long getId() {
        return id;
    }

    public String getGuidanceType() {
        return guidanceType;
    }

    public void setGuidanceType(String guidanceType) {
        this.guidanceType = guidanceType;
    }

    public String getStepKey() {
        return stepKey;
    }

    public void setStepKey(String stepKey) {
        this.stepKey = stepKey;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}
