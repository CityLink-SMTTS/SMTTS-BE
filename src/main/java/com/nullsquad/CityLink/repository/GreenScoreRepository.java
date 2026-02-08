package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.GreenScore;
import com.nullsquad.CityLink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GreenScoreRepository extends JpaRepository<GreenScore, Long> {
    Optional<GreenScore> findByUser(User user);
    Optional<GreenScore> findByUserUserId(Long userId);
}
