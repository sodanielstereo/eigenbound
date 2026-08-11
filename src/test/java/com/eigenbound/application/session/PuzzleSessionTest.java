package com.eigenbound.application.session;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.puzzle.MiniPuzzle;
import com.eigenbound.domain.puzzle.PuzzleOption;
import com.eigenbound.domain.puzzle.PuzzleResult;
import com.eigenbound.domain.puzzle.PuzzleStatus;
import com.eigenbound.domain.puzzle.PuzzleTopic;

class PuzzleSessionTest {

    private static final Duration TIME_LIMIT = Duration.ofSeconds(20);
    private static final String EXPLANATION = "A shift of three reveals VECTOR.";

    private MiniPuzzle puzzle;
    private PuzzleSession session;

    @BeforeEach
    void setUp() {
        puzzle = createPuzzle();
        session = new PuzzleSession(puzzle);
    }

    @Test
    void shouldBeginActiveWithItsPuzzle() {
        assertEquals(puzzle, session.puzzle());
        assertFalse(session.isFinished());
        assertTrue(session.result().isEmpty());
    }

    @Test
    void shouldSolvePuzzleWithCorrectAnswerBeforeDeadline() {
        PuzzleResult result = session.submit(
                "A",
                Duration.ofSeconds(8));

        assertEquals(PuzzleStatus.SOLVED, result.status());
        assertEquals("A", result.selectedOptionId().orElseThrow());
        assertEquals(Duration.ofSeconds(8), result.elapsedTime());
        assertEquals(EXPLANATION, result.explanation());
        assertTrue(result.isSuccessful());
        assertTrue(session.isFinished());
        assertEquals(result, session.result().orElseThrow());
    }

    @Test
    void shouldFinishIncorrectlyWithWrongAnswer() {
        PuzzleResult result = session.submit(
                "B",
                Duration.ofSeconds(6));

        assertEquals(PuzzleStatus.INCORRECT, result.status());
        assertEquals("B", result.selectedOptionId().orElseThrow());
        assertFalse(result.isSuccessful());
        assertTrue(session.isFinished());
    }

    @Test
    void shouldNormalizeSelectedOption() {
        PuzzleResult result = session.submit(
                "  A  ",
                Duration.ofSeconds(5));

        assertEquals("A", result.selectedOptionId().orElseThrow());
        assertEquals(PuzzleStatus.SOLVED, result.status());
    }

    @Test
    void shouldRejectUnknownOption() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.submit(
                        "Z",
                        Duration.ofSeconds(5)));

        assertFalse(session.isFinished());
    }

    @Test
    void shouldRejectBlankOption() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.submit(
                        "   ",
                        Duration.ofSeconds(5)));

        assertFalse(session.isFinished());
    }

    @Test
    void shouldRejectNullOption() {
        assertThrows(
                NullPointerException.class,
                () -> session.submit(
                        null,
                        Duration.ofSeconds(5)));
    }

    @Test
    void shouldTimeOutAnswerSubmittedAtDeadline() {
        PuzzleResult result = session.submit(
                "A",
                TIME_LIMIT);

        assertEquals(PuzzleStatus.TIMED_OUT, result.status());
        assertTrue(result.selectedOptionId().isEmpty());
        assertTrue(session.isFinished());
    }

    @Test
    void shouldTimeOutAnswerSubmittedAfterDeadline() {
        PuzzleResult result = session.submit(
                "A",
                Duration.ofSeconds(21));

        assertEquals(PuzzleStatus.TIMED_OUT, result.status());
        assertTrue(result.selectedOptionId().isEmpty());
    }

    @Test
    void shouldTimeOutSessionWhenDeadlineWasReached() {
        PuzzleResult result = session.timeOut(TIME_LIMIT);

        assertEquals(PuzzleStatus.TIMED_OUT, result.status());
        assertEquals(TIME_LIMIT, result.elapsedTime());
        assertEquals(EXPLANATION, result.explanation());
        assertTrue(session.isFinished());
    }

    @Test
    void shouldRejectTimeoutBeforeDeadline() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.timeOut(
                        Duration.ofSeconds(19)));

        assertFalse(session.isFinished());
    }

    @Test
    void shouldCancelActiveSession() {
        PuzzleResult result = session.cancel(
                Duration.ofSeconds(4));

        assertEquals(PuzzleStatus.CANCELLED, result.status());
        assertEquals(Duration.ofSeconds(4), result.elapsedTime());
        assertTrue(result.selectedOptionId().isEmpty());
        assertTrue(session.isFinished());
    }

    @Test
    void shouldRejectNegativeElapsedTimeWhenSubmitting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.submit(
                        "A",
                        Duration.ofMillis(-1)));

        assertFalse(session.isFinished());
    }

    @Test
    void shouldRejectNullElapsedTimeWhenSubmitting() {
        assertThrows(
                NullPointerException.class,
                () -> session.submit(
                        "A",
                        null));
    }

    @Test
    void shouldRejectNegativeElapsedTimeWhenCancelling() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.cancel(
                        Duration.ofSeconds(-1)));

        assertFalse(session.isFinished());
    }

    @Test
    void shouldRejectSecondAnswerAfterSessionFinished() {
        session.submit(
                "A",
                Duration.ofSeconds(5));

        assertThrows(
                IllegalStateException.class,
                () -> session.submit(
                        "B",
                        Duration.ofSeconds(6)));
    }

    @Test
    void shouldRejectTimeoutAfterSessionFinished() {
        session.submit(
                "B",
                Duration.ofSeconds(5));

        assertThrows(
                IllegalStateException.class,
                () -> session.timeOut(TIME_LIMIT));
    }

    @Test
    void shouldRejectCancellationAfterSessionFinished() {
        session.timeOut(TIME_LIMIT);

        assertThrows(
                IllegalStateException.class,
                () -> session.cancel(
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectNullPuzzle() {
        assertThrows(
                NullPointerException.class,
                () -> new PuzzleSession(null));
    }

    private MiniPuzzle createPuzzle() {
        return new MiniPuzzle(
                "caesar-1",
                PuzzleTopic.CRYPTOGRAPHY,
                "Decrypt YHFWRU.",
                List.of(
                        new PuzzleOption(
                                "A",
                                "VECTOR"),
                        new PuzzleOption(
                                "B",
                                "MATRIX")),
                "A",
                EXPLANATION,
                1,
                TIME_LIMIT);
    }
}