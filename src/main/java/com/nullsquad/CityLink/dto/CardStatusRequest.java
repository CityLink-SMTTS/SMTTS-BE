package com.nullsquad.CityLink.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardStatusRequest {
    @NotNull(message = "Freeze status is required")
    private Boolean freeze;
}
