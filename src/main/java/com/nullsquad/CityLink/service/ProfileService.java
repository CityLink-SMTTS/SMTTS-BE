package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.GreenScoreDto;
import com.nullsquad.CityLink.entity.GreenScore;
import com.nullsquad.CityLink.entity.User;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.GreenScoreRepository;
import com.nullsquad.CityLink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProfileService {

    @Autowired
    private GreenScoreRepository greenScoreRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated user's green score
     */
    public GreenScoreDto getMyGreenScore() {
        User currentUser = getCurrentUser();
        return getGreenScoreByUserId(currentUser.getUserId());
    }

    /**
     * Get green score by user ID
     */
    public GreenScoreDto getGreenScoreByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        GreenScore greenScore = greenScoreRepository.findByUser(user)
                .orElseGet(() -> createDefaultGreenScore(user));

        return convertToDto(greenScore);
    }

    /**
     * Create default green score for user if doesn't exist
     */
    @Transactional
    private GreenScore createDefaultGreenScore(User user) {
        GreenScore greenScore = new GreenScore();
        greenScore.setUser(user);
        greenScore.setTotalTrips(0);
        greenScore.setCarbonSavedKg(BigDecimal.ZERO);
        greenScore.setGreenScore(0);
        return greenScoreRepository.save(greenScore);
    }

    /**
     * Convert GreenScore entity to DTO
     */
    private GreenScoreDto convertToDto(GreenScore greenScore) {
        GreenScoreDto dto = new GreenScoreDto();
        dto.setUserId(greenScore.getUser().getUserId());
        dto.setFullName(greenScore.getUser().getFullName());
        dto.setTotalTrips(greenScore.getTotalTrips());
        dto.setCarbonSavedKg(greenScore.getCarbonSavedKg());
        dto.setGreenScore(greenScore.getGreenScore());
        dto.setLastUpdated(greenScore.getLastUpdated());
        return dto;
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
