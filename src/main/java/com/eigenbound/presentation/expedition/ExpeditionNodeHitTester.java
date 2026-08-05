package com.eigenbound.presentation.expedition;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Detects which expedition node contains a point on the map canvas.
 *
 * <p>
 * Keeping this geometry outside the JavaFX canvas allows pointer detection to
 * be tested without starting the JavaFX application runtime.
 * </p>
 */
public final class ExpeditionNodeHitTester {

        /**
         * Finds the closest node whose circular area contains the supplied point.
         *
         * @param positions node positions produced by the layout calculator
         * @param pointX    horizontal pointer coordinate
         * @param pointY    vertical pointer coordinate
         * @param radius    selectable radius around every node
         * @return identifier of the closest matching node, or an empty result
         */
        public Optional<String> findNodeAt(
                        Map<String, ExpeditionNodePosition> positions,
                        double pointX,
                        double pointY,
                        double radius) {
                Objects.requireNonNull(
                                positions,
                                "Node positions cannot be null");

                validateGeometry(
                                pointX,
                                pointY,
                                radius);

                return positions.values()
                                .stream()
                                .filter(position -> distance(
                                                position,
                                                pointX,
                                                pointY) <= radius)
                                .min(Comparator.comparingDouble(
                                                position -> distance(
                                                                position,
                                                                pointX,
                                                                pointY)))
                                .map(ExpeditionNodePosition::nodeId);
        }

        /**
         * Calculates the Euclidean distance between a node and a canvas point.
         *
         * <p>
         * {@link Math#hypot(double, double)} avoids the intermediate overflow that
         * could occur when manually squaring very large coordinate differences.
         * </p>
         */
        private double distance(
                        ExpeditionNodePosition position,
                        double pointX,
                        double pointY) {
                double deltaX = pointX - position.x();
                double deltaY = pointY - position.y();

                return Math.hypot(
                                deltaX,
                                deltaY);
        }

        /**
         * Validates the pointer coordinates and selectable radius.
         */
        private void validateGeometry(
                        double pointX,
                        double pointY,
                        double radius) {
                if (!Double.isFinite(pointX)
                                || !Double.isFinite(pointY)) {
                        throw new IllegalArgumentException(
                                        "Pointer coordinates must be finite");
                }

                if (!Double.isFinite(radius)
                                || radius <= 0) {
                        throw new IllegalArgumentException(
                                        "Node radius must be finite and positive");
                }
        }
}