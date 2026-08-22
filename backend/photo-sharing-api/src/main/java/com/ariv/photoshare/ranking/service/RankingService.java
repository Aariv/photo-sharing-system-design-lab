package com.ariv.photoshare.ranking.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class RankingService {

    public double calculateScore(
            Instant createdAt,
            long likes,
            long comments) {

        return recencyScore(createdAt)
                + likesScore(likes)
                + commentsScore(comments);
    }

    private double recencyScore(
            Instant createdAt) {

        long minutes =
                Duration.between(
                        createdAt,
                        Instant.now())
                .toMinutes();

        return Math.max(
                1,
                1000 - minutes);
    }

    private double likesScore(
            long likes) {

        return likes * 2.0;
    }

    private double commentsScore(
            long comments) {

        return comments * 3.0;
    }
}