package com.nullsquad.CityLink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkSmartCardRequest {
    @NotBlank(message = "Card UID is required")
    private String cardUid;
}
