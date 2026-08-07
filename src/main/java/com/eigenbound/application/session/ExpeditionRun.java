package com.eigenbound.application.session;

import java.util.Objects;
import java.util.Optional;

import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

/**
 * Coordinates the progress and pending room transition of one expedition run.
 *
 * <p>
 * Selecting a room does not immediately move the player. The room remains
 * pending while its event or challenge is being resolved. Completing the room
 * commits the movement to the underlying {@link ExpeditionSession}, while
 * cancelling it preserves the player's previous position.
 * </p>
 */
public final class ExpeditionRun {

    private final GeneratedExpedition generatedExpedition;
    private final ExpeditionSession expeditionSession;
    private ExpeditionNode pendingRoom;

    /**
     * Starts a run from a procedurally generated expedition.
     *
     * @param generatedExpedition generated map and its original settings
     */
    public ExpeditionRun(
            GeneratedExpedition generatedExpedition) {
        this.generatedExpedition = Objects.requireNonNull(
                generatedExpedition,
                "Generated expedition cannot be null");
        this.expeditionSession = new ExpeditionSession(
                generatedExpedition.map());
    }

    /**
     * Returns the navigation session that stores committed progress.
     *
     * @return expedition navigation session
     */
    public ExpeditionSession expeditionSession() {
        return expeditionSession;
    }

    /**
     * Returns the seed used to generate this expedition.
     *
     * @return expedition generation seed
     */
    public long seed() {
        return generatedExpedition.seed();
    }

    /**
     * Returns the difficulty used to generate this expedition.
     *
     * @return expedition difficulty
     */
    public int difficulty() {
        return generatedExpedition.difficulty();
    }

    /**
     * Returns the room currently waiting to be resolved.
     *
     * @return pending room, or an empty optional when no room is selected
     */
    public Optional<ExpeditionNode> pendingRoom() {
        return Optional.ofNullable(pendingRoom);
    }

    /**
     * Indicates whether a selected room is waiting to be resolved.
     *
     * @return {@code true} when the run contains a pending room
     */
    public boolean hasPendingRoom() {
        return pendingRoom != null;
    }

    /**
     * Derives a deterministic challenge seed for the pending room.
     *
     * <p>
     * Bitwise XOR combines the expedition seed with the unsigned room ID hash.
     * The operation cannot overflow and always produces the same challenge seed
     * for the same expedition and room.
     * </p>
     *
     * @return deterministic seed for the pending room challenge
     * @throws IllegalStateException when no room is currently pending
     */
    public long pendingChallengeSeed() {
        ExpeditionNode room = requirePendingRoom();

        long roomHash = Integer.toUnsignedLong(
                room.id().hashCode());

        return seed() ^ roomHash;
    }

    /**
     * Selects an available room without committing the movement yet.
     *
     * @param nodeId identifier of the selected room
     * @throws NullPointerException     when the node identifier is null
     * @throws IllegalStateException    when another room is already pending or
     *                                  the expedition is complete
     * @throws IllegalArgumentException when the room is not available from the
     *                                  current position
     */
    public void selectRoom(String nodeId) {
        Objects.requireNonNull(
                nodeId,
                "Node ID cannot be null");

        if (hasPendingRoom()) {
            throw new IllegalStateException(
                    "Another expedition room is already pending");
        }

        if (expeditionSession.isCompleted()) {
            throw new IllegalStateException(
                    "Completed expedition cannot select another room");
        }

        if (!expeditionSession.canMoveTo(nodeId)) {
            throw new IllegalArgumentException(
                    "Room is not available from the current position");
        }

        pendingRoom = expeditionSession
                .map()
                .findNode(nodeId);
    }

    /**
     * Commits the movement to the pending room.
     *
     * @throws IllegalStateException when no room is waiting to be completed
     */
    public void completePendingRoom() {
        ExpeditionNode room = requirePendingRoom();

        expeditionSession.moveTo(room.id());
        pendingRoom = null;
    }

    /**
     * Discards the pending room without changing expedition progress.
     *
     * @throws IllegalStateException when no room is waiting to be cancelled
     */
    public void cancelPendingRoom() {
        requirePendingRoom();
        pendingRoom = null;
    }

    /**
     * Returns the pending room or rejects an invalid transition.
     */
    private ExpeditionNode requirePendingRoom() {
        if (pendingRoom == null) {
            throw new IllegalStateException(
                    "No expedition room is currently pending");
        }

        return pendingRoom;
    }
}