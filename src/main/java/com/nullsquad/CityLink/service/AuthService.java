package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.LoginRequest;
import com.nullsquad.CityLink.dto.SignupRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String login(LoginRequest loginRequest) {
        return "token";
    }

    public void signup(SignupRequest signupRequest) {
    }
}
