package com.nullsquad.CityLink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username (email or phone) is required")
    private String username; // Can be email or phone

    @NotBlank(message = "Password is required")
    private String password;
}
