package com.eigenbound.domain.puzzle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PuzzleOptionTest {

    @Test
    void shouldCreatePuzzleOption() {
        PuzzleOption option = new PuzzleOption(
                "A",
                "Vector");

        assertEquals("A", option.id());
        assertEquals("Vector", option.text());
    }

    @Test
    void shouldTrimOptionData() {
        PuzzleOption option = new PuzzleOption(
                "  A  ",
                "  Vector  ");

        assertEquals("A", option.id());
        assertEquals("Vector", option.text());
    }

    @Test
    void shouldCompareOptionsByValue() {
        PuzzleOption first = new PuzzleOption(
                "A",
                "Vector");

        PuzzleOption second = new PuzzleOption(
                "A",
                "Vector");

        PuzzleOption different = new PuzzleOption(
                "B",
                "Matrix");

        assertEquals(first, second);
        assertNotEquals(first, different);
    }

    @Test
    void shouldRejectNullOptionId() {
        assertThrows(
                NullPointerException.class,
                () -> new PuzzleOption(
                        null,
                        "Vector"));
    }

    @Test
    void shouldRejectBlankOptionId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleOption(
                        "   ",
                        "Vector"));
    }

    @Test
    void shouldRejectNullOptionText() {
        assertThrows(
                NullPointerException.class,
                () -> new PuzzleOption(
                        "A",
                        null));
    }

    @Test
    void shouldRejectBlankOptionText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PuzzleOption(
                        "A",
                        "   "));
    }
}