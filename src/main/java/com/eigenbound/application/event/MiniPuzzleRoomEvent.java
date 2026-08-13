package com.eigenbound.application.event;

import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.puzzle.generation.GeneratedMiniPuzzle;

/**
 * Contains the mini-puzzle generated for an alternative expedition room.
 *
 * @param room            room resolved by this event
 * @param generatedPuzzle generated and validated mini-puzzle
 */
public record MiniPuzzleRoomEvent(
        ExpeditionNode room,
        GeneratedMiniPuzzle generatedPuzzle)
        implements ExpeditionRoomEvent {

    /**
     * Validates that the event content matches the expedition room type.
     */
    public MiniPuzzleRoomEvent {
        Objects.requireNonNull(
                room,
                "Event room cannot be null");
        Objects.requireNonNull(
                generatedPuzzle,
                "Generated mini-puzzle cannot be null");

        if (!supports(room.type())) {
            throw new IllegalArgumentException(
                    "Mini-puzzle event requires a rest or reward room");
        }
    }

    /**
     * Returns the mini-puzzle event category.
     */
    @Override
    public RoomEventKind kind() {
        return RoomEventKind.MINI_PUZZLE;
    }

    /**
     * Returns the mini-puzzle generation seed.
     */
    @Override
    public long seed() {
        return generatedPuzzle.seed();
    }

    /**
     * Returns the generated mini-puzzle difficulty.
     */
    @Override
    public int difficulty() {
        return generatedPuzzle.puzzle().difficulty();
    }

    /**
     * Determines whether a room type is resolved with a mini-puzzle.
     */
    private static boolean supports(
            RoomType roomType) {
        return roomType == RoomType.REST
                || roomType == RoomType.REWARD;
    }
}