package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.GreenScoreDto;
import com.nullsquad.CityLink.dto.UserProfileDto;
import com.nullsquad.CityLink.service.ProfileService;
import com.nullsquad.CityLink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    // Get current user's complete profile
    @GetMapping
    public ResponseEntity<UserProfileDto> getMyProfile() {
        UserProfileDto profile = userService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    // Get current user's green score
    @GetMapping("/green-score")
    public ResponseEntity<GreenScoreDto> getMyGreenScore() {
        GreenScoreDto greenScore = profileService.getMyGreenScore();
        return ResponseEntity.ok(greenScore);
    }

    // Get green score by user ID (ADMIN or public)
    @GetMapping("/green-score/{userId}")
    public ResponseEntity<GreenScoreDto> getGreenScoreByUserId(@PathVariable Long userId) {
        GreenScoreDto greenScore = profileService.getGreenScoreByUserId(userId);
        return ResponseEntity.ok(greenScore);
    }
}
