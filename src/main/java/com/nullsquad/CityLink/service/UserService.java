package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.UpdateProfileRequest;
import com.nullsquad.CityLink.dto.UserProfileDto;
import com.nullsquad.CityLink.dto.GreenScoreDto;
import com.nullsquad.CityLink.dto.SmartCardDto;
import com.nullsquad.CityLink.entity.User;
import com.nullsquad.CityLink.entity.UserStatus;
import com.nullsquad.CityLink.entity.GreenScore;
import com.nullsquad.CityLink.entity.SmartCard;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.UserRepository;
import com.nullsquad.CityLink.repository.GreenScoreRepository;
import com.nullsquad.CityLink.repository.SmartCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GreenScoreRepository greenScoreRepository;

    @Autowired
    private SmartCardRepository smartCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all users (ADMIN only)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get current authenticated user's profile
     */
    public UserProfileDto getMyProfile() {
        User currentUser = getCurrentUser();
        return convertToProfileDto(currentUser);
    }

    /**
     * Get user profile by ID
     */
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return convertToProfileDto(user);
    }

    /**
     * Update current user's profile
     */
    @Transactional
    public UserProfileDto updateMyProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            // Check if phone is already taken by another user
            if (userRepository.existsByPhone(request.getPhone()) &&
                !request.getPhone().equals(user.getPhone())) {
                throw new RuntimeException("Phone number already in use");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmail(request.getEmail()) &&
                !request.getEmail().equals(user.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return convertToProfileDto(updatedUser);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Suspend user account (ADMIN only)
     */
    @Transactional
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    /**
     * Activate user account (ADMIN only)
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * Convert User entity to UserProfileDto
     */
    private UserProfileDto convertToProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());

        // Get green score
        greenScoreRepository.findByUser(user).ifPresent(greenScore -> {
            GreenScoreDto greenScoreDto = new GreenScoreDto();
            greenScoreDto.setUserId(user.getUserId());
            greenScoreDto.setFullName(user.getFullName());
            greenScoreDto.setTotalTrips(greenScore.getTotalTrips());
            greenScoreDto.setCarbonSavedKg(greenScore.getCarbonSavedKg());
            greenScoreDto.setGreenScore(greenScore.getGreenScore());
            greenScoreDto.setLastUpdated(greenScore.getLastUpdated());
            dto.setGreenScore(greenScoreDto);
        });

        // Get smart cards
        List<SmartCard> cards = smartCardRepository.findByUser(user);
        List<SmartCardDto> cardDtos = cards.stream().map(this::convertToSmartCardDto).collect(Collectors.toList());
        dto.setSmartCards(cardDtos);

        return dto;
    }

    /**
     * Convert SmartCard entity to SmartCardDto
     */
    private SmartCardDto convertToSmartCardDto(SmartCard card) {
        SmartCardDto dto = new SmartCardDto();
        dto.setCardId(card.getId());
        dto.setCardUid(card.getCardUid());
        dto.setBalance(card.getBalance());
        dto.setStatus(card.getStatus().name());
        dto.setIssuedAt(card.getIssuedAt());
        return dto;
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
