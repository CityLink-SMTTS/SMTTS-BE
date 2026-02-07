package com.nullsquad.CityLink.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    // Methods for generating and validating tokens
    public String generateToken(String username) {
        return "token";
    }

    public boolean validateToken(String token) {
        return true;
    }

    public String getUsernameFromToken(String token) {
        return "username";
    }
}
