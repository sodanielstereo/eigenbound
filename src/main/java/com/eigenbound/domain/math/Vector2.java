package com.eigenbound.domain.math;

import java.util.Objects;

/**
 * Immutable two-dimensional mathematical vector.
 *
 * @param x horizontal component
 * @param y vertical component
 */
public record Vector2(double x, double y) {

    private static final double EPSILON = 1e-9;

    public Vector2 {
        x = normalizeZero(x);
        y = normalizeZero(y);
    }

    private static double normalizeZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    /**
     * Returns the sum of this vector and another vector.
     */
    public Vector2 add(Vector2 other) {
        Objects.requireNonNull(other, "Other vector cannot be null");

        return new Vector2(
                x + other.x(),
                y + other.y());
    }

    /**
     * Returns the result of subtracting another vector from this vector.
     */
    public Vector2 subtract(Vector2 other) {
        Objects.requireNonNull(other, "Other vector cannot be null");

        return new Vector2(
                x - other.x(),
                y - other.y());
    }

    /**
     * Returns this vector multiplied by a scalar.
     */
    public Vector2 scale(double scalar) {
        return new Vector2(
                x * scalar,
                y * scalar);
    }

    /**
     * Returns the magnitude, or length, of this vector.
     */
    public double magnitude() {
        return Math.hypot(x, y);
    }

    /**
     * Returns the dot product between this vector and another vector.
     */
    public double dot(Vector2 other) {
        Objects.requireNonNull(other, "Other vector cannot be null");

        return x * other.x() + y * other.y();
    }

    /**
     * Determines whether this vector is approximately the zero vector.
     */
    public boolean isZero() {
        return Math.abs(x) <= EPSILON
                && Math.abs(y) <= EPSILON;
    }

    /**
     * Determines whether this vector is parallel to another vector.
     *
     * Two vectors in R² are parallel when their determinant is zero.
     * The zero vector is excluded because it has no direction.
     */
    public boolean isParallelTo(Vector2 other) {
        Objects.requireNonNull(other, "Other vector cannot be null");

        if (isZero() || other.isZero()) {
            return false;
        }

        double determinant = x * other.y() - y * other.x();
        double scale = magnitude() * other.magnitude();

        return Math.abs(determinant) <= EPSILON * Math.max(1.0, scale);
    }

    /**
     * Determines whether this vector is orthogonal to another vector.
     *
     * Two vectors are orthogonal when their dot product is zero.
     */
    public boolean isOrthogonalTo(Vector2 other) {
        Objects.requireNonNull(other, "Other vector cannot be null");

        double product = dot(other);
        double scale = magnitude() * other.magnitude();

        return Math.abs(product) <= EPSILON * Math.max(1.0, scale);
    }
}