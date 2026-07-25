package com.eigenbound.domain.generation;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

/**
 * Procedurally generated challenge with its verified solution and seed.
 *
 * @param challenge generated vector challenge
 * @param solution  sequence guaranteed to solve the challenge
 * @param seed      seed used during generation
 */
public record GeneratedVectorChallenge(
        VectorChallenge challenge,
        List<Vector2> solution,
        long seed) {

    public GeneratedVectorChallenge {
        Objects.requireNonNull(
                challenge,
                "Challenge cannot be null");
        Objects.requireNonNull(
                solution,
                "Solution cannot be null");

        solution = List.copyOf(solution);

        if (solution.isEmpty()) {
            throw new IllegalArgumentException(
                    "Solution cannot be empty");
        }

        ChallengeStatus status = challenge
                .evaluate(solution)
                .status();

        if (status != ChallengeStatus.SOLVED) {
            throw new IllegalArgumentException(
                    "Generated solution must solve the challenge");
        }
    }
}