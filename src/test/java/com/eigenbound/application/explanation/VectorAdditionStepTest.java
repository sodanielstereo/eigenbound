package com.eigenbound.application.explanation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.math.Vector2;

class VectorAdditionStepTest {

    @Test
    void shouldCreateReadableEquation() {
        VectorAdditionStep step = new VectorAdditionStep(
                1,
                new Vector2(0, 0),
                new Vector2(2, 1),
                new Vector2(2, 1));

        assertEquals(
                "(0, 0) + (2, 1) = (2, 1)",
                step.equation());
    }

    @Test
    void shouldRejectInvalidStepNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorAdditionStep(
                        0,
                        new Vector2(0, 0),
                        new Vector2(1, 1),
                        new Vector2(1, 1)));
    }

    @Test
    void shouldRejectIncorrectResult() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorAdditionStep(
                        1,
                        new Vector2(0, 0),
                        new Vector2(1, 1),
                        new Vector2(5, 5)));
    }

    @Test
    void shouldRejectNullValues() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorAdditionStep(
                        1,
                        null,
                        new Vector2(1, 1),
                        new Vector2(1, 1)));
    }
}