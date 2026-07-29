package com.eigenbound.application.explanation;

import java.util.Objects;

import com.eigenbound.domain.math.Vector2;

/**
 * Describes one vector addition performed during a challenge attempt.
 *
 * @param stepNumber     one-based position of the step
 * @param positionBefore position before applying the movement
 * @param movement       vector applied during the step
 * @param positionAfter  resulting position
 */
public record VectorAdditionStep(
        int stepNumber,
        Vector2 positionBefore,
        Vector2 movement,
        Vector2 positionAfter) {

    public VectorAdditionStep {
        if (stepNumber <= 0) {
            throw new IllegalArgumentException(
                    "Step number must be greater than zero");
        }

        Objects.requireNonNull(
                positionBefore,
                "Previous position cannot be null");
        Objects.requireNonNull(
                movement,
                "Movement cannot be null");
        Objects.requireNonNull(
                positionAfter,
                "Resulting position cannot be null");

        Vector2 expectedPosition = positionBefore.add(movement);

        if (!expectedPosition.equals(positionAfter)) {
            throw new IllegalArgumentException(
                    "Resulting position does not match vector addition");
        }
    }

    /**
     * Returns the vector addition as a readable equation.
     *
     * @return mathematical equation for this step
     */
    public String equation() {
        return format(positionBefore)
                + " + "
                + format(movement)
                + " = "
                + format(positionAfter);
    }

    private String format(Vector2 vector) {
        return "("
                + formatNumber(vector.x())
                + ", "
                + formatNumber(vector.y())
                + ")";
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return String.format("%.2f", value);
    }
}