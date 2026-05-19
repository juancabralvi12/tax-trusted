package com.example.taxtrusted.dto;

import com.example.taxtrusted.model.TaxNeed;
import com.example.taxtrusted.model.Timeline;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Set;

public record CreateLeadRequest(
    @Pattern(regexp = "^[0-9]{5}$", message = "ZIP code must be 5 digits")
    String zipCode,
    @NotEmpty
    Set<TaxNeed> needs,
    @NotNull
    Timeline timeline,
    String firstName,
    String lastName,
    @Email
    String email,
    String phone
) {}

