package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.*;
import com.nullsquad.CityLink.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserRewardDTO> getUserReward(@PathVariable Long userId) {
        return ResponseEntity.ok(rewardService.getUserReward(userId));
    }

    @GetMapping("/user/{userId}/green-score")
    public ResponseEntity<GreenScoreDTO> getGreenScore(@PathVariable Long userId) {
        return ResponseEntity.ok(rewardService.getGreenScore(userId));
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<RewardTransactionDTO>> getUserTransactionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(rewardService.getUserTransactionHistory(userId, limit));
    }

    @PostMapping("/user/{userId}/redeem")
    public ResponseEntity<UserRewardDTO> redeemPoints(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        int points = (Integer) body.get("points");
        String description = (String) body.get("description");
        return ResponseEntity.ok(rewardService.redeemPoints(userId, points, description));
    }

    @GetMapping("/leaderboard/green-score")
    public ResponseEntity<List<UserRewardDTO>> getGreenScoreLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rewardService.getGreenScoreLeaderboard(limit));
    }

    @GetMapping("/leaderboard/points")
    public ResponseEntity<List<UserRewardDTO>> getPointsLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rewardService.getPointsLeaderboard(limit));
    }

    @GetMapping("/leaderboard/eco-travelers")
    public ResponseEntity<List<UserRewardDTO>> getEcoTravelersLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(rewardService.getEcoTravelersLeaderboard(limit));
    }

    @GetMapping("/sustainability/global")
    public ResponseEntity<Map<String, Object>> getGlobalSustainabilityStats() {
        return ResponseEntity.ok(rewardService.getGlobalSustainabilityStats());
    }

    @PostMapping("/admin/user/{userId}/bonus")
    public ResponseEntity<Void> awardBonusPoints(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        int points = (Integer) body.get("points");
        String reason = (String) body.get("reason");
        rewardService.awardBonusPoints(userId, points, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/{userId}/adjust")
    public ResponseEntity<UserRewardDTO> adjustPoints(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        int adjustment = (Integer) body.get("adjustment");
        String reason = (String) body.get("reason");
        return ResponseEntity.ok(rewardService.adjustPoints(userId, adjustment, reason));
    }
}
