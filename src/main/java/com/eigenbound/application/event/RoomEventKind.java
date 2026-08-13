package com.eigenbound.application.event;

/**
 * Identifies the kind of content used to resolve an expedition room.
 */

public enum RoomEventKind {
    /**
     * A vector challenge resolved in the vector laboratory
     */
    VECTOR_CHALLENGE,

    /**
     * a short timed puzzle resolved in the puzzle laboratory
     */
    MINI_PUZZLE,
}
