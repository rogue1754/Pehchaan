
package com.Hackathon.Pehchaan.DTOs;

import jakarta.validation.constraints.*;

public record CreateProfileRequest(

        @NotBlank
        @Size(max = 100)
        String fullName,

        @Email
        @Size(max = 150)
        String email,

        @Pattern(regexp = "[0-9]{12}")
        String aadhaarNumber,

        @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]")
        String panNumber
) {}
