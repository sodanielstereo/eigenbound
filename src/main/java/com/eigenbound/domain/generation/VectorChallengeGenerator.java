package com.eigenbound.domain.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

/**
 * Generates deterministic vector challenges with verified solutions.
 */
public final class VectorChallengeGenerator {

    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 5;

    /**
     * Generates a vector challenge from a seed and difficulty.
     *
     * The same seed and difficulty always produce the same challenge.
     *
     * @param seed       deterministic generation seed
     * @param difficulty difficulty from 1 to 5
     * @return challenge together with its verified solution
     */
    public GeneratedVectorChallenge generate(
            long seed,
            int difficulty) {
        validateDifficulty(difficulty);

        Random random = new Random(seed);

        int moveCount = moveCountFor(difficulty);
        int maxSteps = difficulty + 1;
        int coordinateLimit = difficulty + 1;

        Vector2 start = randomVector(
                random,
                coordinateLimit,
                true);

        List<Vector2> availableMoves = generateAvailableMoves(
                random,
                moveCount,
                coordinateLimit);

        List<Vector2> solution = generateSolution(
                random,
                availableMoves,
                maxSteps);

        Vector2 displacement = sum(solution);

        /*
         * A random sequence could cancel itself and return to the starting
         * position. In that case, repeating one non-zero movement guarantees
         * a non-zero displacement while preserving determinism.
         */
        if (displacement.isZero()) {
            solution = repeatedSolution(
                    availableMoves.get(0),
                    maxSteps);
            displacement = sum(solution);
        }

        Vector2 target = start.add(displacement);

        VectorChallenge challenge = new VectorChallenge(
                start,
                target,
                availableMoves,
                maxSteps);

        return new GeneratedVectorChallenge(
                challenge,
                solution,
                seed);
    }

    private List<Vector2> generateAvailableMoves(
            Random random,
            int moveCount,
            int coordinateLimit) {
        List<Vector2> moves = new ArrayList<>();

        while (moves.size() < moveCount) {
            Vector2 candidate = randomVector(
                    random,
                    coordinateLimit,
                    false);

            if (!moves.contains(candidate)) {
                moves.add(candidate);
            }
        }

        return List.copyOf(moves);
    }

    private List<Vector2> generateSolution(
            Random random,
            List<Vector2> availableMoves,
            int maxSteps) {
        List<Vector2> solution = new ArrayList<>();

        for (int step = 0; step < maxSteps; step++) {
            int selectedIndex = random.nextInt(
                    availableMoves.size());

            solution.add(availableMoves.get(selectedIndex));
        }

        return List.copyOf(solution);
    }

    private List<Vector2> repeatedSolution(
            Vector2 movement,
            int amount) {
        List<Vector2> solution = new ArrayList<>();

        for (int index = 0; index < amount; index++) {
            solution.add(movement);
        }

        return List.copyOf(solution);
    }

    private Vector2 sum(List<Vector2> vectors) {
        Vector2 result = new Vector2(0, 0);

        for (Vector2 vector : vectors) {
            result = result.add(vector);
        }

        return result;
    }

    private Vector2 randomVector(
            Random random,
            int coordinateLimit,
            boolean allowZero) {
        Vector2 vector;

        do {
            int x = randomCoordinate(
                    random,
                    coordinateLimit);
            int y = randomCoordinate(
                    random,
                    coordinateLimit);

            vector = new Vector2(x, y);
        } while (!allowZero && vector.isZero());

        return vector;
    }

    private int randomCoordinate(
            Random random,
            int coordinateLimit) {
        int amountOfValues = coordinateLimit * 2 + 1;

        return random.nextInt(amountOfValues)
                - coordinateLimit;
    }

    private int moveCountFor(int difficulty) {
        return switch (difficulty) {
            case 1, 2 -> 2;
            case 3, 4 -> 3;
            case 5 -> 4;
            default -> throw new IllegalArgumentException(
                    "Unsupported difficulty: " + difficulty);
        };
    }

    private void validateDifficulty(int difficulty) {
        if (difficulty < MIN_DIFFICULTY
                || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException(
                    "Difficulty must be between "
                            + MIN_DIFFICULTY
                            + " and "
                            + MAX_DIFFICULTY);
        }
    }
}