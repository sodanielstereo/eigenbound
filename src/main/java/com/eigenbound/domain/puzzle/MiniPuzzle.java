package com.eigenbound.domain.puzzle;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents one immutable multiple-choice mini-puzzle.
 *
 * <p>
 * A puzzle contains only domain data and validation. Timing and player
 * attempts will be managed later by a separate puzzle session.
 * </p>
 *
 * @param id              stable puzzle identifier
 * @param topic           academic topic practiced by the puzzle
 * @param prompt          question presented to the player
 * @param options         selectable answer options
 * @param correctOptionId identifier of the correct option
 * @param explanation     educational explanation shown after evaluation
 * @param difficulty      puzzle difficulty from one to five
 * @param timeLimit       maximum duration allowed for the puzzle
 */
public record MiniPuzzle(
        String id,
        PuzzleTopic topic,
        String prompt,
        List<PuzzleOption> options,
        String correctOptionId,
        String explanation,
        int difficulty,
        Duration timeLimit) {

    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 5;
    private static final int MIN_OPTION_COUNT = 2;

    /**
     * Normalizes puzzle text, creates an immutable option list and validates
     * every domain invariant.
     */
    public MiniPuzzle {
        Objects.requireNonNull(
                id,
                "Puzzle ID cannot be null");
        Objects.requireNonNull(
                topic,
                "Puzzle topic cannot be null");
        Objects.requireNonNull(
                prompt,
                "Puzzle prompt cannot be null");
        Objects.requireNonNull(
                options,
                "Puzzle options cannot be null");
        Objects.requireNonNull(
                correctOptionId,
                "Correct option ID cannot be null");
        Objects.requireNonNull(
                explanation,
                "Puzzle explanation cannot be null");
        Objects.requireNonNull(
                timeLimit,
                "Puzzle time limit cannot be null");

        id = id.trim();
        prompt = prompt.trim();
        correctOptionId = correctOptionId.trim();
        explanation = explanation.trim();
        options = List.copyOf(options);

        requireNotBlank(
                id,
                "Puzzle ID cannot be blank");
        requireNotBlank(
                prompt,
                "Puzzle prompt cannot be blank");
        requireNotBlank(
                correctOptionId,
                "Correct option ID cannot be blank");
        requireNotBlank(
                explanation,
                "Puzzle explanation cannot be blank");

        if (options.size() < MIN_OPTION_COUNT) {
            throw new IllegalArgumentException(
                    "Puzzle must contain at least two options");
        }

        validateUniqueOptionIds(options);

        if (!containsOptionId(
                options,
                correctOptionId)) {
            throw new IllegalArgumentException(
                    "Correct option ID must reference an existing option");
        }

        if (difficulty < MIN_DIFFICULTY
                || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException(
                    "Puzzle difficulty must be between one and five");
        }

        if (timeLimit.isZero()
                || timeLimit.isNegative()) {
            throw new IllegalArgumentException(
                    "Puzzle time limit must be positive");
        }
    }

    /**
     * Determines whether an option is the correct puzzle answer.
     *
     * @param optionId selected option identifier
     * @return {@code true} when the selected option is correct
     */
    public boolean isCorrectOption(
            String optionId) {
        Objects.requireNonNull(
                optionId,
                "Selected option ID cannot be null");

        return correctOptionId.equals(
                optionId.trim());
    }

    /**
     * Rejects repeated identifiers because answer evaluation depends on them.
     */
    private static void validateUniqueOptionIds(
            List<PuzzleOption> options) {
        Set<String> optionIds = new HashSet<>();

        for (PuzzleOption option : options) {
            if (!optionIds.add(option.id())) {
                throw new IllegalArgumentException(
                        "Puzzle option IDs must be unique");
            }
        }
    }

    /**
     * Determines whether the option list contains a specific identifier.
     */
    private static boolean containsOptionId(
            List<PuzzleOption> options,
            String optionId) {
        for (PuzzleOption option : options) {
            if (option.id().equals(optionId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Rejects text that contains only whitespace.
     */
    private static void requireNotBlank(
            String value,
            String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}