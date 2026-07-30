package com.eigenbound.presentation.expedition;

import java.util.Objects;

/**
 * Represents the pixel position assigned to an expedition node.
 *
 * @param nodeId expedition node identifier
 * @param x      horizontal canvas coordinate
 * @param y      vertical canvas coordinate
 */
public record ExpeditionNodePosition(
        String nodeId,
        double x,
        double y) {

    public ExpeditionNodePosition {
        Objects.requireNonNull(
                nodeId,
                "Node ID cannot be null");

        nodeId = nodeId.trim();

        if (nodeId.isBlank()) {
            throw new IllegalArgumentException(
                    "Node ID cannot be blank");
        }

        if (!Double.isFinite(x)
                || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Node coordinates must be finite");
        }
    }
}