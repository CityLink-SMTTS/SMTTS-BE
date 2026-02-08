package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.Role;
import com.nullsquad.CityLink.entity.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private GreenScoreDto greenScore;
    private List<SmartCardDto> smartCards;
}
