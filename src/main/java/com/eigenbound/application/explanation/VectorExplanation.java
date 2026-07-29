package com.eigenbound.application.explanation;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.math.Vector2;

/**
 * Contains a complete educational explanation of a challenge attempt.
 *
 * @param steps         individual vector additions
 * @param finalPosition position reached by the player
 * @param status        result of the challenge attempt
 * @param summary       educational summary
 */
public record VectorExplanation(
        List<VectorAdditionStep> steps,
        Vector2 finalPosition,
        ChallengeStatus status,
        String summary) {

    public VectorExplanation {
        Objects.requireNonNull(
                steps,
                "Explanation steps cannot be null");
        Objects.requireNonNull(
                finalPosition,
                "Final position cannot be null");
        Objects.requireNonNull(
                status,
                "Challenge status cannot be null");
        Objects.requireNonNull(
                summary,
                "Summary cannot be null");

        if (summary.isBlank()) {
            throw new IllegalArgumentException(
                    "Summary cannot be blank");
        }

        steps = List.copyOf(steps);
    }
}