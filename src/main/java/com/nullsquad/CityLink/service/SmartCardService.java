package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.SmartCardDto;
import com.nullsquad.CityLink.entity.GreenScore;
import com.nullsquad.CityLink.entity.SmartCard;
import com.nullsquad.CityLink.entity.User;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.GreenScoreRepository;
import com.nullsquad.CityLink.repository.SmartCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartCardService {

    @Autowired
    private SmartCardRepository smartCardRepository;

    @Autowired
    private GreenScoreRepository greenScoreRepository;

    @Autowired
    private UserService userService;

    // Link a smart card to current user
    @Transactional
    public SmartCardDto linkCardToUser(String cardUid) {
        User currentUser = userService.getCurrentUser();

        // Check if card already exists
        if (smartCardRepository.existsByCardUid(cardUid)) {
            throw new RuntimeException("Card already linked to a user");
        }

        SmartCard smartCard = new SmartCard();
        smartCard.setCardUid(cardUid);
        smartCard.setUser(currentUser);
        smartCard.setBalance(BigDecimal.ZERO);
        smartCard.setStatus(SmartCard.CardStatus.ACTIVE);

        SmartCard savedCard = smartCardRepository.save(smartCard);

        // Initialize green score for new user if not exists
        if (greenScoreRepository.findByUser(currentUser).isEmpty()) {
            GreenScore greenScore = new GreenScore();
            greenScore.setUser(currentUser);
            greenScore.setTotalTrips(0);
            greenScore.setCarbonSavedKg(BigDecimal.ZERO);
            greenScore.setGreenScore(0);
            greenScoreRepository.save(greenScore);
        }

        return mapToDto(savedCard);
    }

    // Get all cards for current user
    public List<SmartCardDto> getMyCards() {
        User currentUser = userService.getCurrentUser();
        List<SmartCard> cards = smartCardRepository.findByUser(currentUser);
        return cards.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get card by ID
    public SmartCardDto getCardById(Long cardId) {
        SmartCard card = smartCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        // Verify ownership
        User currentUser = userService.getCurrentUser();
        if (!card.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized access to card");
        }

        return mapToDto(card);
    }

    // Freeze/Unfreeze card
    @Transactional
    public SmartCardDto updateCardStatus(Long cardId, boolean freeze) {
        SmartCard card = smartCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        // Verify ownership
        User currentUser = userService.getCurrentUser();
        if (!card.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized access to card");
        }

        if (freeze) {
            card.setStatus(SmartCard.CardStatus.FROZEN);
        } else {
            // Only unfreeze if not blocked
            if (card.getStatus() == SmartCard.CardStatus.FROZEN) {
                card.setStatus(SmartCard.CardStatus.ACTIVE);
            } else if (card.getStatus() == SmartCard.CardStatus.BLOCKED) {
                throw new RuntimeException("Cannot unfreeze blocked card. Contact admin.");
            }
        }

        SmartCard updatedCard = smartCardRepository.save(card);
        return mapToDto(updatedCard);
    }

    // Block card (ADMIN only)
    @Transactional
    public SmartCardDto blockCard(Long cardId) {
        SmartCard card = smartCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        card.setStatus(SmartCard.CardStatus.BLOCKED);
        SmartCard updatedCard = smartCardRepository.save(card);
        return mapToDto(updatedCard);
    }

    // Unblock card (ADMIN only)
    @Transactional
    public SmartCardDto unblockCard(Long cardId) {
        SmartCard card = smartCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        card.setStatus(SmartCard.CardStatus.ACTIVE);
        SmartCard updatedCard = smartCardRepository.save(card);
        return mapToDto(updatedCard);
    }

    // Get card balance
    public BigDecimal getCardBalance(Long cardId) {
        SmartCard card = smartCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        // Verify ownership
        User currentUser = userService.getCurrentUser();
        if (!card.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized access to card");
        }

        return card.getBalance();
    }

    // Helper method to map SmartCard to SmartCardDto
    private SmartCardDto mapToDto(SmartCard smartCard) {
        SmartCardDto dto = new SmartCardDto();
        dto.setCardId(smartCard.getId());
        dto.setCardUid(smartCard.getCardUid());
        dto.setBalance(smartCard.getBalance());
        dto.setStatus(smartCard.getStatus().name());
        dto.setIssuedAt(smartCard.getIssuedAt());
        return dto;
    }
}
