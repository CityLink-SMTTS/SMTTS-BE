package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.SmartCard;
import com.nullsquad.CityLink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmartCardRepository extends JpaRepository<SmartCard, Long> {
    Optional<SmartCard> findByCardUid(String cardUid);
    List<SmartCard> findByUser(User user);
    List<SmartCard> findByUserUserId(Long userId);
    Optional<SmartCard> findByCardUidAndStatus(String cardUid, SmartCard.CardStatus status);
    boolean existsByCardUid(String cardUid);
}
