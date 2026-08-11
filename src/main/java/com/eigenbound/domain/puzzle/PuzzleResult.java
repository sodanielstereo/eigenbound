package com.eigenbound.domain.puzzle;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the immutable terminal result of one mini-puzzle attempt.
 *
 * <p>
 * Solved and incorrect results contain the selected option. Timed-out and
 * cancelled results do not, because no answer was evaluated in those cases.
 * </p>
 *
 * @param status           terminal outcome of the attempt
 * @param selectedOptionId selected option, when an answer was evaluated
 * @param elapsedTime      time spent before reaching the outcome
 * @param explanation      feedback presented after the attempt
 */
public record PuzzleResult(
        PuzzleStatus status,
        Optional<String> selectedOptionId,
        Duration elapsedTime,
        String explanation) {

    /**
     * Normalizes result data and validates every state combination.
     */
    public PuzzleResult {
        Objects.requireNonNull(
                status,
                "Puzzle status cannot be null");
        Objects.requireNonNull(
                selectedOptionId,
                "Selected option cannot be null");
        Objects.requireNonNull(
                elapsedTime,
                "Elapsed time cannot be null");
        Objects.requireNonNull(
                explanation,
                "Puzzle explanation cannot be null");

        selectedOptionId = selectedOptionId.map(String::trim);
        explanation = explanation.trim();

        if (selectedOptionId.isPresent()
                && selectedOptionId.orElseThrow().isBlank()) {
            throw new IllegalArgumentException(
                    "Selected option ID cannot be blank");
        }

        if (elapsedTime.isNegative()) {
            throw new IllegalArgumentException(
                    "Elapsed time cannot be negative");
        }

        if (explanation.isBlank()) {
            throw new IllegalArgumentException(
                    "Puzzle explanation cannot be blank");
        }

        if (status.requiresSelectedOption()
                && selectedOptionId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Evaluated results require a selected option");
        }

        if (!status.requiresSelectedOption()
                && selectedOptionId.isPresent()) {
            throw new IllegalArgumentException(
                    "Unevaluated results cannot contain a selected option");
        }
    }

    /**
     * Creates a successful result.
     *
     * @param selectedOptionId correct option selected by the player
     * @param elapsedTime      time spent before answering
     * @param explanation      educational feedback for the answer
     * @return a solved puzzle result
     */
    public static PuzzleResult solved(
            String selectedOptionId,
            Duration elapsedTime,
            String explanation) {
        return evaluated(
                PuzzleStatus.SOLVED,
                selectedOptionId,
                elapsedTime,
                explanation);
    }

    /**
     * Creates a result for an incorrect answer.
     *
     * @param selectedOptionId incorrect option selected by the player
     * @param elapsedTime      time spent before answering
     * @param explanation      educational feedback for the answer
     * @return an incorrect puzzle result
     */
    public static PuzzleResult incorrect(
            String selectedOptionId,
            Duration elapsedTime,
            String explanation) {
        return evaluated(
                PuzzleStatus.INCORRECT,
                selectedOptionId,
                elapsedTime,
                explanation);
    }

    /**
     * Creates a result for an expired time limit.
     *
     * @param elapsedTime time spent before expiration
     * @param explanation feedback presented after expiration
     * @return a timed-out puzzle result
     */
    public static PuzzleResult timedOut(
            Duration elapsedTime,
            String explanation) {
        return unevaluated(
                PuzzleStatus.TIMED_OUT,
                elapsedTime,
                explanation);
    }

    /**
     * Creates a result for a cancelled attempt.
     *
     * @param elapsedTime time spent before cancellation
     * @param explanation feedback presented after cancellation
     * @return a cancelled puzzle result
     */
    public static PuzzleResult cancelled(
            Duration elapsedTime,
            String explanation) {
        return unevaluated(
                PuzzleStatus.CANCELLED,
                elapsedTime,
                explanation);
    }

    /**
     * Indicates whether the attempt solved the puzzle.
     *
     * @return {@code true} only for a solved result
     */
    public boolean isSuccessful() {
        return status == PuzzleStatus.SOLVED;
    }

    /**
     * Creates a result that evaluated a selected answer.
     */
    private static PuzzleResult evaluated(
            PuzzleStatus status,
            String selectedOptionId,
            Duration elapsedTime,
            String explanation) {
        Objects.requireNonNull(
                selectedOptionId,
                "Selected option ID cannot be null");

        return new PuzzleResult(
                status,
                Optional.of(selectedOptionId),
                elapsedTime,
                explanation);
    }

    /**
     * Creates a result that did not evaluate an answer.
     */
    private static PuzzleResult unevaluated(
            PuzzleStatus status,
            Duration elapsedTime,
            String explanation) {
        return new PuzzleResult(
                status,
                Optional.empty(),
                elapsedTime,
                explanation);
    }
}