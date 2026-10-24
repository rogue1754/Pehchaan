
package com.Hackathon.Pehchaan.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_aadhaar",
                        columnNames = "aadhaar_hash"
                ),
                @UniqueConstraint(
                        name = "uk_profile_pan",
                        columnNames = "pan_number"
                )
        }
)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String email;

    // Demo-safe identifier fields.
    // Store dummy values only until secure hashing is implemented.
    @Column(name = "aadhaar_hash", unique = true, length = 64)
    private String aadhaarHash;

    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Profile() {}

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAadhaarHash() {
        return aadhaarHash;
    }

    public void setAadhaarHash(String aadhaarHash) {
        this.aadhaarHash = aadhaarHash;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
