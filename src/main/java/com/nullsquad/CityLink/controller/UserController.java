package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.ApiResponse;
import com.nullsquad.CityLink.dto.UpdateProfileRequest;
import com.nullsquad.CityLink.dto.UserProfileDto;
import com.nullsquad.CityLink.entity.UserStatus;
import com.nullsquad.CityLink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Get all users (ADMIN only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", userService.getAllUsers()));
    }

    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        UserProfileDto profile = userService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    // Get user profile by ID (ADMIN only)
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    // Update current user profile
    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updatedProfile = userService.updateMyProfile(request);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedProfile));
    }

    // Suspend user (ADMIN only)
    @PostMapping("/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> suspendUser(@PathVariable Long userId) {
        userService.suspendUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User suspended successfully"));
    }

    // Activate user (ADMIN only)
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User activated successfully"));
    }
}

