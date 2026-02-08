package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.ApiResponse;
import com.nullsquad.CityLink.dto.CardStatusRequest;
import com.nullsquad.CityLink.dto.LinkSmartCardRequest;
import com.nullsquad.CityLink.dto.SmartCardDto;
import com.nullsquad.CityLink.service.SmartCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/smartcards")
public class SmartCardController {

    @Autowired
    private SmartCardService smartCardService;

    // Link a new smart card to current user
    @PostMapping("/link")
    public ResponseEntity<ApiResponse> linkCard(@Valid @RequestBody LinkSmartCardRequest request) {
        SmartCardDto cardDto = smartCardService.linkCardToUser(request.getCardUid());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Smart card linked successfully", cardDto));
    }

    // Get all smart cards for current user
    @GetMapping
    public ResponseEntity<List<SmartCardDto>> getAllMyCards() {
        List<SmartCardDto> cards = smartCardService.getMyCards();
        return ResponseEntity.ok(cards);
    }

    // Alternative endpoint - Get all smart cards for current user
    @GetMapping("/my-cards")
    public ResponseEntity<List<SmartCardDto>> getMyCards() {
        List<SmartCardDto> cards = smartCardService.getMyCards();
        return ResponseEntity.ok(cards);
    }

    // Get specific card by ID
    @GetMapping("/{cardId}")
    public ResponseEntity<SmartCardDto> getCardById(@PathVariable Long cardId) {
        SmartCardDto card = smartCardService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }

    // Get card balance
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<ApiResponse> getCardBalance(@PathVariable Long cardId) {
        BigDecimal balance = smartCardService.getCardBalance(cardId);
        return ResponseEntity.ok(new ApiResponse(true, "Balance retrieved successfully", balance));
    }

    // Freeze or unfreeze card
    @PutMapping("/{cardId}/status")
    public ResponseEntity<ApiResponse> updateCardStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody CardStatusRequest request) {
        SmartCardDto updatedCard = smartCardService.updateCardStatus(cardId, request.getFreeze());
        String message = request.getFreeze() ? "Card frozen successfully" : "Card unfrozen successfully";
        return ResponseEntity.ok(new ApiResponse(true, message, updatedCard));
    }

    // Freeze card
    @PutMapping("/{cardId}/freeze")
    public ResponseEntity<ApiResponse> freezeCard(@PathVariable Long cardId) {
        SmartCardDto updatedCard = smartCardService.updateCardStatus(cardId, true);
        return ResponseEntity.ok(new ApiResponse(true, "Card frozen successfully", updatedCard));
    }

    // Unfreeze card
    @PutMapping("/{cardId}/unfreeze")
    public ResponseEntity<ApiResponse> unfreezeCard(@PathVariable Long cardId) {
        SmartCardDto updatedCard = smartCardService.updateCardStatus(cardId, false);
        return ResponseEntity.ok(new ApiResponse(true, "Card unfrozen successfully", updatedCard));
    }

    // Block card (ADMIN only)
    @PostMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> blockCard(@PathVariable Long cardId) {
        SmartCardDto blockedCard = smartCardService.blockCard(cardId);
        return ResponseEntity.ok(new ApiResponse(true, "Card blocked successfully", blockedCard));
    }

    // Unblock card (ADMIN only)
    @PostMapping("/{cardId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> unblockCard(@PathVariable Long cardId) {
        SmartCardDto unblockedCard = smartCardService.unblockCard(cardId);
        return ResponseEntity.ok(new ApiResponse(true, "Card unblocked successfully", unblockedCard));
    }
}
