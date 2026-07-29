package com.eigenbound.domain.expedition;

import java.util.Objects;

/**
 * Represents a directed connection between two expedition nodes.
 *
 * @param sourceId      identifier of the origin node
 * @param destinationId identifier of the destination node
 */
public record ExpeditionEdge(
        String sourceId,
        String destinationId) {

    public ExpeditionEdge {
        Objects.requireNonNull(
                sourceId,
                "Source ID cannot be null");
        Objects.requireNonNull(
                destinationId,
                "Destination ID cannot be null");

        sourceId = sourceId.trim();
        destinationId = destinationId.trim();

        if (sourceId.isBlank()
                || destinationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Edge IDs cannot be blank");
        }

        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException(
                    "A node cannot connect to itself");
        }
    }
}