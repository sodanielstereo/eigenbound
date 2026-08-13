package com.eigenbound.application.event;

import com.eigenbound.domain.expedition.ExpeditionNode;

/**
 * Represents playable content generated for one pending expedition room.
 *
 * <p>
 * The sealed hierarchy limits room events to the variants understood by the
 * application. Each implementation exposes common metadata while preserving
 * its strongly typed generated content.
 * </p>
 */
public sealed interface ExpeditionRoomEvent
        permits VectorChallengeRoomEvent,
        MiniPuzzleRoomEvent {

    /**
     * Returns the expedition room resolved by this event.
     *
     * @return event room
     */
    ExpeditionNode room();

    /**
     * Returns the playable content category.
     *
     * @return event kind
     */
    RoomEventKind kind();

    /**
     * Returns the deterministic seed used to generate the content.
     *
     * @return generation seed
     */
    long seed();

    /**
     * Returns the effective difficulty of the generated content.
     *
     * @return difficulty from one to five
     */
    int difficulty();
}