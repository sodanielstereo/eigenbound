package com.eigenbound.domain.challenge;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.math.Vector2;

/**
 * Challenge in which a target must be reached by combining available vectors.
 *
 * @param start          initial position
 * @param target         position that must be reached
 * @param availableMoves vectors that the player may use
 * @param maxSteps       maximum number of movements allowed
 */
public record VectorChallenge(
        Vector2 start,
        Vector2 target,
        List<Vector2> availableMoves,
        int maxSteps) {

    private static final double EPSILON = 1e-9;

    public VectorChallenge {
        Objects.requireNonNull(start, "Start cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        Objects.requireNonNull(
                availableMoves,
                "Available moves cannot be null");

        if (availableMoves.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one movement must be available");
        }

        if (maxSteps <= 0) {
            throw new IllegalArgumentException(
                    "Maximum steps must be greater than zero");
        }

        availableMoves = List.copyOf(availableMoves);
    }

    /**
     * Evaluates a sequence of movements.
     *
     * @param moves movements selected by the player
     * @return result of the attempt
     */
    public ChallengeResult evaluate(List<Vector2> moves) {
        Objects.requireNonNull(moves, "Moves cannot be null");

        if (moves.size() > maxSteps) {
            return new ChallengeResult(
                    ChallengeStatus.STEP_LIMIT_EXCEEDED,
                    start,
                    0);
        }

        Vector2 currentPosition = start;
        int stepsUsed = 0;

        for (Vector2 move : moves) {
            if (move == null || !availableMoves.contains(move)) {
                return new ChallengeResult(
                        ChallengeStatus.INVALID_MOVE,
                        currentPosition,
                        stepsUsed);
            }

            currentPosition = currentPosition.add(move);
            stepsUsed++;
        }

        ChallengeStatus status = approximatelyEquals(
                currentPosition,
                target)
                        ? ChallengeStatus.SOLVED
                        : ChallengeStatus.INCOMPLETE;

        return new ChallengeResult(
                status,
                currentPosition,
                stepsUsed);
    }

    private boolean approximatelyEquals(Vector2 first, Vector2 second) {
        return first.subtract(second).magnitude() <= EPSILON;
    }
}