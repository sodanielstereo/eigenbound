package com.eigenbound.domain.puzzle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MiniPuzzleTest {

    @Test
    void shouldCreateMiniPuzzle() {
        MiniPuzzle puzzle = createPuzzle();

        assertEquals("cipher-1", puzzle.id());
        assertEquals(PuzzleTopic.CRYPTOGRAPHY, puzzle.topic());
        assertEquals("Decrypt YHFWRU.", puzzle.prompt());
        assertEquals("A", puzzle.correctOptionId());
        assertEquals(1, puzzle.difficulty());
        assertEquals(Duration.ofSeconds(20), puzzle.timeLimit());
    }

    @Test
    void shouldTrimPuzzleText() {
        MiniPuzzle puzzle = new MiniPuzzle(
                "  cipher-1  ",
                PuzzleTopic.CRYPTOGRAPHY,
                "  Decrypt YHFWRU.  ",
                validOptions(),
                "  A  ",
                "  Caesar shift minus three produces VECTOR.  ",
                1,
                Duration.ofSeconds(20));

        assertEquals("cipher-1", puzzle.id());
        assertEquals("Decrypt YHFWRU.", puzzle.prompt());
        assertEquals("A", puzzle.correctOptionId());
        assertEquals(
                "Caesar shift minus three produces VECTOR.",
                puzzle.explanation());
    }

    @Test
    void shouldRecognizeCorrectOption() {
        MiniPuzzle puzzle = createPuzzle();

        assertTrue(puzzle.isCorrectOption("A"));
        assertTrue(puzzle.isCorrectOption("  A  "));
        assertFalse(puzzle.isCorrectOption("B"));
    }

    @Test
    void shouldRejectNullSelectedOption() {
        assertThrows(
                NullPointerException.class,
                () -> createPuzzle().isCorrectOption(null));
    }

    @Test
    void shouldCreateImmutableOptionList() {
        MiniPuzzle puzzle = createPuzzle();

        assertThrows(
                UnsupportedOperationException.class,
                () -> puzzle.options().add(
                        new PuzzleOption(
                                "C",
                                "Kernel")));
    }

    @Test
    void shouldDefensivelyCopyOptionList() {
        List<PuzzleOption> sourceOptions = new ArrayList<>(
                validOptions());

        MiniPuzzle puzzle = new MiniPuzzle(
                "cipher-1",
                PuzzleTopic.CRYPTOGRAPHY,
                "Decrypt YHFWRU.",
                sourceOptions,
                "A",
                "Caesar shift minus three produces VECTOR.",
                1,
                Duration.ofSeconds(20));

        sourceOptions.add(
                new PuzzleOption(
                        "C",
                        "Kernel"));

        assertEquals(2, puzzle.options().size());
    }

    @Test
    void shouldRejectNullPuzzleId() {
        assertThrows(
                NullPointerException.class,
                () -> new MiniPuzzle(
                        null,
                        PuzzleTopic.CRYPTOGRAPHY,
                        "Prompt",
                        validOptions(),
                        "A",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectBlankPrompt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MiniPuzzle(
                        "cipher-1",
                        PuzzleTopic.CRYPTOGRAPHY,
                        "   ",
                        validOptions(),
                        "A",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectNullTopic() {
        assertThrows(
                NullPointerException.class,
                () -> new MiniPuzzle(
                        "cipher-1",
                        null,
                        "Prompt",
                        validOptions(),
                        "A",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectFewerThanTwoOptions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MiniPuzzle(
                        "cipher-1",
                        PuzzleTopic.CRYPTOGRAPHY,
                        "Prompt",
                        List.of(
                                new PuzzleOption(
                                        "A",
                                        "Vector")),
                        "A",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectDuplicateOptionIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MiniPuzzle(
                        "cipher-1",
                        PuzzleTopic.CRYPTOGRAPHY,
                        "Prompt",
                        List.of(
                                new PuzzleOption(
                                        "A",
                                        "Vector"),
                                new PuzzleOption(
                                        "A",
                                        "Matrix")),
                        "A",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectUnknownCorrectOption() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MiniPuzzle(
                        "cipher-1",
                        PuzzleTopic.CRYPTOGRAPHY,
                        "Prompt",
                        validOptions(),
                        "Z",
                        "Explanation",
                        1,
                        Duration.ofSeconds(20)));
    }

    @Test
    void shouldRejectDifficultyBelowMinimum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> puzzleWithDifficulty(0));
    }

    @Test
    void shouldRejectDifficultyAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> puzzleWithDifficulty(6));
    }

    @Test
    void shouldRejectZeroTimeLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> puzzleWithTimeLimit(Duration.ZERO));
    }

    @Test
    void shouldRejectNegativeTimeLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> puzzleWithTimeLimit(
                        Duration.ofSeconds(-1)));
    }

    private MiniPuzzle createPuzzle() {
        return new MiniPuzzle(
                "cipher-1",
                PuzzleTopic.CRYPTOGRAPHY,
                "Decrypt YHFWRU.",
                validOptions(),
                "A",
                "Caesar shift minus three produces VECTOR.",
                1,
                Duration.ofSeconds(20));
    }

    private MiniPuzzle puzzleWithDifficulty(
            int difficulty) {
        return new MiniPuzzle(
                "cipher-1",
                PuzzleTopic.CRYPTOGRAPHY,
                "Prompt",
                validOptions(),
                "A",
                "Explanation",
                difficulty,
                Duration.ofSeconds(20));
    }

    private MiniPuzzle puzzleWithTimeLimit(
            Duration timeLimit) {
        return new MiniPuzzle(
                "cipher-1",
                PuzzleTopic.CRYPTOGRAPHY,
                "Prompt",
                validOptions(),
                "A",
                "Explanation",
                1,
                timeLimit);
    }

    private List<PuzzleOption> validOptions() {
        return List.of(
                new PuzzleOption(
                        "A",
                        "Vector"),
                new PuzzleOption(
                        "B",
                        "Matrix"));
    }
}