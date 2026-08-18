package com.eigenbound.application.session;

import java.util.Objects;
import java.util.Optional;

import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.ExpeditionResources;
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
    private ExpeditionResources resources;
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
        this.resources = ExpeditionResources.initial();
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
     * Returns the immutable resource snapshot currently owned by this run.
     *
     * @return current stability and insight
     */
    public ExpeditionResources resources() {
        return resources;
    }

    /**
     * Derives the current lifecycle state from navigation and resources.
     *
     * @return active, victory or defeat state
     */
    public ExpeditionRunState state() {
        if (resources.isDepleted()) {
            return ExpeditionRunState.DEFEAT;
        }

        if (expeditionSession.isCompleted()) {
            return ExpeditionRunState.VICTORY;
        }

        return ExpeditionRunState.ACTIVE;
    }

    /**
     * Indicates whether the expedition reached victory or defeat.
     *
     * @return {@code true} when the run can no longer change
     */
    public boolean isFinished() {
        return state().isTerminal();
    }

    /**
     * Creates an immutable snapshot of the completed expedition.
     *
     * @return final expedition summary
     * @throws IllegalStateException when the expedition is still active
     */
    public ExpeditionSummary summary() {
        ExpeditionRunState outcome = state();

        if (!outcome.isTerminal()) {
            throw new IllegalStateException(
                    "Active expedition does not have a final summary");
        }

        return new ExpeditionSummary(
                outcome,
                seed(),
                difficulty(),
                resources,
                expeditionSession.visitedNodes().size());
    }

    /**
     * Indicates whether the expedition has exhausted all stability.
     *
     * @return {@code true} when the run can no longer continue
     */
    public boolean isFailed() {
        return state() == ExpeditionRunState.DEFEAT;
    }

    /**
     * Applies stability damage to an active expedition.
     *
     * @param amount stability removed from the run
     * @throws IllegalStateException when the run already finished
     */
    public void loseStability(
            int amount) {
        requireActive();
        resources = resources.loseStability(amount);
    }

    /**
     * Restores stability without exceeding the run's maximum.
     *
     * @param amount stability restored to the run
     * @throws IllegalStateException when the run already finished
     */
    public void restoreStability(
            int amount) {
        requireActive();
        resources = resources.restoreStability(amount);
    }

    /**
     * Adds insight earned during the expedition.
     *
     * @param amount insight added to the run
     * @throws IllegalStateException when the run already finished
     */
    public void gainInsight(
            long amount) {
        requireActive();
        resources = resources.gainInsight(amount);
    }

    /**
     * Spends insight owned by the expedition.
     *
     * @param amount insight removed from the run
     * @throws IllegalStateException when the run already finished
     */
    public void spendInsight(
            long amount) {
        requireActive();
        resources = resources.spendInsight(amount);
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
     *                                  the expedition already finished
     * @throws IllegalArgumentException when the room is not available from the
     *                                  current position
     */
    public void selectRoom(String nodeId) {
        Objects.requireNonNull(
                nodeId,
                "Node ID cannot be null");

        requireActive();

        if (hasPendingRoom()) {
            throw new IllegalStateException(
                    "Another expedition room is already pending");
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
     * @throws IllegalStateException when no room is waiting to be completed or
     *                               the expedition already finished
     */
    public void completePendingRoom() {
        requireActive();

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

    /**
     * Rejects operations that could mutate a run after its terminal outcome.
     */
    private void requireActive() {
        switch (state()) {
            case ACTIVE -> {
                return;
            }
            case VICTORY -> throw new IllegalStateException(
                    "Victorious expedition cannot continue");
            case DEFEAT -> throw new IllegalStateException(
                    "Failed expedition cannot continue");
        }
    }
}