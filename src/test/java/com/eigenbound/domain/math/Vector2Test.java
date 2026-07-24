package com.eigenbound.domain.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class Vector2Test {

    private static final double EPSILON = 1e-9;

    @Test
    void shouldAddTwoVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(4, -1);

        Vector2 result = first.add(second);

        assertEquals(new Vector2(6, 2), result);
    }

    @Test
    void shouldSubtractTwoVectors() {
        Vector2 first = new Vector2(5, 7);
        Vector2 second = new Vector2(2, 3);

        Vector2 result = first.subtract(second);

        assertEquals(new Vector2(3, 4), result);
    }

    @Test
    void shouldScaleVectorByPositiveScalar() {
        Vector2 vector = new Vector2(2, -3);

        Vector2 result = vector.scale(2);

        assertEquals(new Vector2(4, -6), result);
    }

    @Test
    void shouldScaleVectorByNegativeScalar() {
        Vector2 vector = new Vector2(2, -3);

        Vector2 result = vector.scale(-2);

        assertEquals(new Vector2(-4, 6), result);
    }

    @Test
    void shouldReturnZeroVectorWhenScaledByZero() {
        Vector2 vector = new Vector2(2, -3);

        Vector2 result = vector.scale(0);

        assertEquals(new Vector2(0, 0), result);
    }

    @Test
    void shouldCalculateMagnitude() {
        Vector2 vector = new Vector2(3, 4);

        double result = vector.magnitude();

        assertEquals(5.0, result, EPSILON);
    }

    @Test
    void shouldCalculateDotProduct() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(4, -1);

        double result = first.dot(second);

        assertEquals(5.0, result, EPSILON);
    }

    @Test
    void shouldIdentifyZeroVector() {
        assertTrue(new Vector2(0, 0).isZero());
    }

    @Test
    void shouldIdentifyNonZeroVector() {
        assertFalse(new Vector2(1, 0).isZero());
    }

    @Test
    void shouldIdentifyParallelVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(4, 6);

        assertTrue(first.isParallelTo(second));
    }

    @Test
    void shouldIdentifyOppositeParallelVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(-4, -6);

        assertTrue(first.isParallelTo(second));
    }

    @Test
    void shouldRejectNonParallelVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(3, 2);

        assertFalse(first.isParallelTo(second));
    }

    @Test
    void zeroVectorShouldNotBeConsideredParallel() {
        Vector2 zero = new Vector2(0, 0);
        Vector2 vector = new Vector2(2, 3);

        assertFalse(zero.isParallelTo(vector));
    }

    @Test
    void shouldIdentifyOrthogonalVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(-3, 2);

        assertTrue(first.isOrthogonalTo(second));
    }

    @Test
    void shouldRejectNonOrthogonalVectors() {
        Vector2 first = new Vector2(2, 3);
        Vector2 second = new Vector2(4, 1);

        assertFalse(first.isOrthogonalTo(second));
    }

    @Test
    void operationsShouldNotModifyOriginalVector() {
        Vector2 original = new Vector2(2, 3);

        Vector2 result = original.add(new Vector2(1, 1));

        assertEquals(new Vector2(2, 3), original);
        assertEquals(new Vector2(3, 4), result);
        assertNotSame(original, result);
    }

    @Test
    void shouldRejectNullVectorInAddition() {
        Vector2 vector = new Vector2(2, 3);

        assertThrows(
                NullPointerException.class,
                () -> vector.add(null));
    }

    @Test
    void shouldRejectNullVectorInSubtraction() {
        Vector2 vector = new Vector2(2, 3);

        assertThrows(
                NullPointerException.class,
                () -> vector.subtract(null));
    }

    @Test
    void shouldRejectNullVectorInDotProduct() {
        Vector2 vector = new Vector2(2, 3);

        assertThrows(
                NullPointerException.class,
                () -> vector.dot(null));
    }
}