package com.eigenbound.domain.expedition;

import java.util.Objects;

/**
 * Represents a room or vertex in an expedition graph. Each node can have a
 * specific type, such as a challenge room, rest area, or reward room.
 * ExpeditionNode
 * 
 * @param id         unique identifier for the node
 * @param layer      horizontal progression level
 * @param type       purpose of the room
 * @param difficulty challenge difficulty level (0,5)
 */

public record ExpeditionNode(
        String id,
        int layer,
        RoomType type,
        int difficulty) {

    public ExpeditionNode {
        Objects.requireNonNull(
                id,
                "Node ID cannot be null");
        Objects.requireNonNull(
                type,
                "Room type cannot be null");

        id = id.trim();

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Node ID cannot be blank");
        }

        if (layer < 0) {
            throw new IllegalArgumentException(
                    "Node layer cannot be negative");
        }

        if (difficulty < 0 || difficulty > 5) {
            throw new IllegalArgumentException(
                    "Difficulty must be between zero and five");
        }

        if (type == RoomType.START && layer != 0) {
            throw new IllegalArgumentException(
                    "Start node must belong to layer zero");
        }
    }
}