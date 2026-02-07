package com.nullsquad.CityLink.security;

import com.nullsquad.CityLink.entity.User;
import com.nullsquad.CityLink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Simple implementation, should return UserDetails object
        // Assuming User entity has username field
        // We need to return a UserDetails implementation.
        // For skeleton:
        return new org.springframework.security.core.userdetails.User(username, "password", new ArrayList<>());
    }
}
