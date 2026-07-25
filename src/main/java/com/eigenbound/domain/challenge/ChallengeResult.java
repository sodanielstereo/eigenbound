package com.eigenbound.domain.challenge;

import java.util.Objects;

import com.eigenbound.domain.math.Vector2;

/**
 * Result produced after evaluating a sequence of vector movements.
 *
 * @param status        final status of the attempt
 * @param finalPosition position reached by the player
 * @param stepsUsed     number of valid movements performed
 */
public record ChallengeResult(
        ChallengeStatus status,
        Vector2 finalPosition,
        int stepsUsed) {

    public ChallengeResult {
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(
                finalPosition,
                "Final position cannot be null");

        if (stepsUsed < 0) {
            throw new IllegalArgumentException(
                    "Steps used cannot be negative");
        }
    }
}