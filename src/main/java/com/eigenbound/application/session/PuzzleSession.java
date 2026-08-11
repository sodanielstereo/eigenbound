package com.eigenbound.application.session;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import com.eigenbound.domain.puzzle.MiniPuzzle;
import com.eigenbound.domain.puzzle.PuzzleOption;
import com.eigenbound.domain.puzzle.PuzzleResult;

/**
 * Maintains the state of one mini-puzzle attempt.
 *
 * <p>
 * A session starts active and can produce exactly one terminal result. Once
 * solved, answered incorrectly, timed out or cancelled, it cannot be changed.
 * The caller supplies elapsed time so this class remains independent from
 * JavaFX timers and the system clock.
 * </p>
 */
public final class PuzzleSession {

    private final MiniPuzzle puzzle;
    private PuzzleResult result;

    /**
     * Creates an active session for a mini-puzzle.
     *
     * @param puzzle puzzle played during this session
     */
    public PuzzleSession(
            MiniPuzzle puzzle) {
        this.puzzle = Objects.requireNonNull(
                puzzle,
                "Mini-puzzle cannot be null");
    }

    /**
     * Returns the puzzle associated with this session.
     *
     * @return current mini-puzzle
     */
    public MiniPuzzle puzzle() {
        return puzzle;
    }

    /**
     * Returns the terminal result when the session has finished.
     *
     * @return the result, or an empty value while the session is active
     */
    public Optional<PuzzleResult> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Indicates whether this session already reached a terminal state.
     *
     * @return {@code true} when the session can no longer change
     */
    public boolean isFinished() {
        return result != null;
    }

    /**
     * Evaluates an answer when it arrives before the puzzle deadline.
     *
     * <p>
     * An answer submitted exactly at or after the time limit becomes a timed
     * out result and the selected option is not evaluated.
     * </p>
     *
     * @param optionId    selected puzzle option identifier
     * @param elapsedTime time elapsed since the puzzle started
     * @return the terminal result produced by the answer
     */
    public PuzzleResult submit(
            String optionId,
            Duration elapsedTime) {
        requireActive();

        Objects.requireNonNull(
                optionId,
                "Selected option ID cannot be null");
        validateElapsedTime(elapsedTime);

        String normalizedOptionId = optionId.trim();

        if (normalizedOptionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Selected option ID cannot be blank");
        }

        if (!containsOption(normalizedOptionId)) {
            throw new IllegalArgumentException(
                    "Selected option does not belong to this puzzle");
        }

        if (hasReachedTimeLimit(elapsedTime)) {
            return finish(
                    PuzzleResult.timedOut(
                            elapsedTime,
                            puzzle.explanation()));
        }

        if (puzzle.isCorrectOption(normalizedOptionId)) {
            return finish(
                    PuzzleResult.solved(
                            normalizedOptionId,
                            elapsedTime,
                            puzzle.explanation()));
        }

        return finish(
                PuzzleResult.incorrect(
                        normalizedOptionId,
                        elapsedTime,
                        puzzle.explanation()));
    }

    /**
     * Ends the session after its time limit has been reached.
     *
     * @param elapsedTime time elapsed since the puzzle started
     * @return the timed-out result
     */
    public PuzzleResult timeOut(
            Duration elapsedTime) {
        requireActive();
        validateElapsedTime(elapsedTime);

        if (!hasReachedTimeLimit(elapsedTime)) {
            throw new IllegalArgumentException(
                    "Puzzle cannot time out before its time limit");
        }

        return finish(
                PuzzleResult.timedOut(
                        elapsedTime,
                        puzzle.explanation()));
    }

    /**
     * Cancels the active puzzle without evaluating an answer.
     *
     * @param elapsedTime time elapsed since the puzzle started
     * @return the cancelled result
     */
    public PuzzleResult cancel(
            Duration elapsedTime) {
        requireActive();
        validateElapsedTime(elapsedTime);

        return finish(
                PuzzleResult.cancelled(
                        elapsedTime,
                        puzzle.explanation()));
    }

    /**
     * Stores the only terminal result that this session can produce.
     */
    private PuzzleResult finish(
            PuzzleResult terminalResult) {
        result = terminalResult;
        return result;
    }

    /**
     * Rejects transitions after the session has finished.
     */
    private void requireActive() {
        if (isFinished()) {
            throw new IllegalStateException(
                    "Mini-puzzle session has already finished");
        }
    }

    /**
     * Validates elapsed time supplied by the presentation timer.
     */
    private void validateElapsedTime(
            Duration elapsedTime) {
        Objects.requireNonNull(
                elapsedTime,
                "Elapsed time cannot be null");

        if (elapsedTime.isNegative()) {
            throw new IllegalArgumentException(
                    "Elapsed time cannot be negative");
        }
    }

    /**
     * Determines whether the puzzle deadline has been reached.
     */
    private boolean hasReachedTimeLimit(
            Duration elapsedTime) {
        return elapsedTime.compareTo(puzzle.timeLimit()) >= 0;
    }

    /**
     * Determines whether an identifier belongs to one of the puzzle options.
     */
    private boolean containsOption(
            String optionId) {
        for (PuzzleOption option : puzzle.options()) {
            if (option.id().equals(optionId)) {
                return true;
            }
        }

        return false;
    }
}