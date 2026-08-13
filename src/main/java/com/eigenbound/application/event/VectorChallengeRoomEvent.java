package com.eigenbound.application.event;

import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.generation.GeneratedVectorChallenge;

/**
 * Contains the vector challenge generated for a challenge expedition room.
 *
 * @param room               room resolved by this event
 * @param generatedChallenge generated and verified vector challenge
 */
public record VectorChallengeRoomEvent(
        ExpeditionNode room,
        GeneratedVectorChallenge generatedChallenge)
        implements ExpeditionRoomEvent {

    /**
     * Validates that the event content matches the expedition room type.
     */
    public VectorChallengeRoomEvent {
        Objects.requireNonNull(
                room,
                "Event room cannot be null");
        Objects.requireNonNull(
                generatedChallenge,
                "Generated vector challenge cannot be null");

        if (!supports(room.type())) {
            throw new IllegalArgumentException(
                    "Vector challenge event requires a challenge room");
        }
    }

    /**
     * Returns the vector challenge event category.
     */
    @Override
    public RoomEventKind kind() {
        return RoomEventKind.VECTOR_CHALLENGE;
    }

    /**
     * Returns the challenge generation seed.
     */
    @Override
    public long seed() {
        return generatedChallenge.seed();
    }

    /**
     * Returns the difficulty assigned to the challenge room.
     */
    @Override
    public int difficulty() {
        return room.difficulty();
    }

    /**
     * Determines whether a room type is resolved with vector gameplay.
     */
    private static boolean supports(
            RoomType roomType) {
        return switch (roomType) {
            case VECTOR_CHALLENGE,
                    ELITE_CHALLENGE,
                    BOSS ->
                true;

            case START,
                    REST,
                    REWARD ->
                false;
        };
    }
}