
package com.Hackathon.Pehchaan.Controller;

import com.Hackathon.Pehchaan.DTOs.CreateProfileRequest;
import com.Hackathon.Pehchaan.Entity.Profile;
import com.Hackathon.Pehchaan.Repository.ProfileRepository;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileRepository repository;

    public ProfileController(ProfileRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {

        if (request.aadhaarNumber() == null
                && request.panNumber() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide Aadhaar or PAN for the demo profile"
            );
        }

        if (request.aadhaarNumber() != null
                && repository.existsByAadhaarHash(
                request.aadhaarNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This Aadhaar is already linked to a profile"
            );
        }

        if (request.panNumber() != null
                && repository.existsByPanNumber(
                request.panNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This PAN is already linked to a profile"
            );
        }

        Profile profile = new Profile();
        profile.setFullName(request.fullName().trim());
        profile.setEmail(request.email());

        // Temporary demo implementation:
        // replace with a proper keyed hash before using real identifiers.
        profile.setAadhaarHash(request.aadhaarNumber());
        profile.setPanNumber(request.panNumber());

        try {
            Profile saved = repository.saveAndFlush(profile);

            return Map.of(
                    "id", saved.getId(),
                    "fullName", saved.getFullName(),
                    "email", saved.getEmail() == null
                            ? "" : saved.getEmail(),
                    "createdAt", saved.getCreatedAt(),
                    "message", "Demo profile created"
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An identifier is already linked to a profile"
            );
        }
    }

    @GetMapping
    public List<Map<String, Object>> getProfiles() {
        return repository.findAll().stream()
                .map(profile -> Map.<String, Object>of(
                        "id", profile.getId(),
                        "fullName", profile.getFullName(),
                        "email", profile.getEmail() == null
                                ? "" : profile.getEmail(),
                        "createdAt", profile.getCreatedAt()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProfile(@PathVariable Long id) {
        Profile profile = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile not found"
                ));

        return Map.of(
                "id", profile.getId(),
                "fullName", profile.getFullName(),
                "email", profile.getEmail() == null
                        ? "" : profile.getEmail(),
                "createdAt", profile.getCreatedAt()
        );
    }
}
