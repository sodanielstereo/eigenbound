package com.eigenbound.domain.puzzle;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PuzzleResultTest {

    private static final Duration ELAPSED_TIME = Duration.ofSeconds(8);
    private static final String EXPLANATION = "A shift of three reveals VECTOR.";

    @Test
    void shouldCreateSolvedResult() {
        PuzzleResult result = PuzzleResult.solved(
                "A",
                ELAPSED_TIME,
                EXPLANATION);

        assertEquals(PuzzleStatus.SOLVED, result.status());
        assertEquals(Optional.of("A"), result.selectedOptionId());
        assertEquals(ELAPSED_TIME, result.elapsedTime());
        assertEquals(EXPLANATION, result.explanation());
        assertTrue(result.isSuccessful());
    }

    @Test
    void shouldCreateIncorrectResult() {
        PuzzleResult result = PuzzleResult.incorrect(
                "B",
                ELAPSED_TIME,
                EXPLANATION);

        assertEquals(PuzzleStatus.INCORRECT, result.status());
        assertEquals(Optional.of("B"), result.selectedOptionId());
        assertFalse(result.isSuccessful());
    }

    @Test
    void shouldCreateTimedOutResultWithoutSelection() {
        PuzzleResult result = PuzzleResult.timedOut(
                Duration.ofSeconds(20),
                EXPLANATION);

        assertEquals(PuzzleStatus.TIMED_OUT, result.status());
        assertTrue(result.selectedOptionId().isEmpty());
        assertFalse(result.isSuccessful());
    }

    @Test
    void shouldCreateCancelledResultWithoutSelection() {
        PuzzleResult result = PuzzleResult.cancelled(
                Duration.ofSeconds(3),
                EXPLANATION);

        assertEquals(PuzzleStatus.CANCELLED, result.status());
        assertTrue(result.selectedOptionId().isEmpty());
        assertFalse(result.isSuccessful());
    }

    @Test
    void shouldTrimSelectedOptionAndExplanation() {
        PuzzleResult result = PuzzleResult.solved(
                "  A  ",
                ELAPSED_TIME,
                "  " + EXPLANATION + "  ");

        assertEquals(Optional.of("A"), result.selectedOptionId());
        assertEquals(EXPLANATION, result.explanation());
    }

    @Test
    void shouldAllowZeroElapsedTime() {
        PuzzleResult result = PuzzleResult.cancelled(
                Duration.ZERO,
                EXPLANATION);

        assertEquals(Duration.ZERO, result.elapsedTime());
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                NullPointerException.class,
                () -> new PuzzleResult(
                        null,
                        Optional.of("A"),
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectNullSelectedOptionContainer() {
        assertThrows(
                NullPointerException.class,
                () -> new PuzzleResult(
                        PuzzleStatus.SOLVED,
                        null,
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectNullElapsedTime() {
        assertThrows(
                NullPointerException.class,
                () -> PuzzleResult.solved(
                        "A",
                        null,
                        EXPLANATION));
    }

    @Test
    void shouldRejectNullExplanation() {
        assertThrows(
                NullPointerException.class,
                () -> PuzzleResult.solved(
                        "A",
                        ELAPSED_TIME,
                        null));
    }

    @Test
    void shouldRejectNegativeElapsedTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PuzzleResult.solved(
                        "A",
                        Duration.ofMillis(-1),
                        EXPLANATION));
    }

    @Test
    void shouldRejectBlankExplanation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PuzzleResult.solved(
                        "A",
                        ELAPSED_TIME,
                        "   "));
    }

    @Test
    void shouldRejectBlankSelectedOption() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PuzzleResult.solved(
                        "   ",
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectSolvedResultWithoutSelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleResult(
                        PuzzleStatus.SOLVED,
                        Optional.empty(),
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectIncorrectResultWithoutSelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleResult(
                        PuzzleStatus.INCORRECT,
                        Optional.empty(),
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectTimedOutResultWithSelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleResult(
                        PuzzleStatus.TIMED_OUT,
                        Optional.of("A"),
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectCancelledResultWithSelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleResult(
                        PuzzleStatus.CANCELLED,
                        Optional.of("A"),
                        ELAPSED_TIME,
                        EXPLANATION));
    }

    @Test
    void shouldRejectNullSelectedOptionInFactory() {
        assertThrows(
                NullPointerException.class,
                () -> PuzzleResult.incorrect(
                        null,
                        ELAPSED_TIME,
                        EXPLANATION));
    }
}