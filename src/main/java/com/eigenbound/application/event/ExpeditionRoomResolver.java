package com.eigenbound.application.event;

import java.util.Objects;

import com.eigenbound.application.effect.MiniPuzzleRoomEffectPolicy;
import com.eigenbound.application.effect.RoomEffect;
import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.puzzle.PuzzleResult;
import com.eigenbound.domain.puzzle.PuzzleStatus;

/**
 * Resolves completed expedition room events against an active run.
 *
 * <p>
 * The resolver coordinates application services that already own one focused
 * responsibility: the effect policy calculates resource changes and the
 * expedition run manages navigation and resources. It verifies the pending
 * room before applying anything, which prevents stale events and repeated
 * results from changing a run more than once.
 * </p>
 */
public final class ExpeditionRoomResolver {

    private final MiniPuzzleRoomEffectPolicy miniPuzzleEffectPolicy;

    /**
     * Creates a resolver using the standard mini-puzzle room policy.
     */
    public ExpeditionRoomResolver() {
        this(new MiniPuzzleRoomEffectPolicy());
    }

    /**
     * Creates a resolver with an explicit mini-puzzle effect policy.
     *
     * @param miniPuzzleEffectPolicy policy used to calculate resource changes
     */
    public ExpeditionRoomResolver(
            MiniPuzzleRoomEffectPolicy miniPuzzleEffectPolicy) {
        this.miniPuzzleEffectPolicy = Objects.requireNonNull(
                miniPuzzleEffectPolicy,
                "Mini-puzzle effect policy cannot be null");
    }

    /**
     * Resolves one terminal mini-puzzle result.
     *
     * <p>
     * Solved, incorrect and timed-out attempts consume the room. Cancellation
     * clears the pending selection without committing movement, so the same
     * room remains available from the player's current position. If timeout
     * damage depletes stability, the pending room is cleared and the failed run
     * does not advance.
     * </p>
     *
     * @param run    expedition that owns the pending room
     * @param event  generated mini-puzzle room event
     * @param result terminal puzzle result
     * @return resource effect applied to the run
     * @throws NullPointerException     when an argument is null
     * @throws IllegalStateException    when the run has no pending room
     * @throws IllegalArgumentException when the event does not match the
     *                                  pending room
     */
    public RoomEffect resolveMiniPuzzle(
            ExpeditionRun run,
            MiniPuzzleRoomEvent event,
            PuzzleResult result) {
        Objects.requireNonNull(
                run,
                "Expedition run cannot be null");
        Objects.requireNonNull(
                event,
                "Mini-puzzle room event cannot be null");
        Objects.requireNonNull(
                result,
                "Puzzle result cannot be null");

        requireMatchingPendingRoom(
                run,
                event.room());

        RoomEffect effect = miniPuzzleEffectPolicy.determineEffect(
                event,
                result);

        if (result.status() == PuzzleStatus.CANCELLED) {
            run.cancelPendingRoom();
            return effect;
        }

        applyEffect(
                run,
                effect);

        if (run.isFailed()) {
            run.cancelPendingRoom();
        } else {
            run.completePendingRoom();
        }

        return effect;
    }

    /**
     * Ensures the event belongs to the room currently selected by the run.
     */
    private void requireMatchingPendingRoom(
            ExpeditionRun run,
            ExpeditionNode eventRoom) {
        ExpeditionNode pendingRoom = run
                .pendingRoom()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No expedition room is currently pending"));

        if (!pendingRoom.equals(eventRoom)) {
            throw new IllegalArgumentException(
                    "Room event does not match the pending expedition room");
        }
    }

    /**
     * Applies each non-zero resource adjustment through the expedition run.
     */
    private void applyEffect(
            ExpeditionRun run,
            RoomEffect effect) {
        if (effect.stabilityRestored() > 0) {
            run.restoreStability(
                    effect.stabilityRestored());
        }

        if (effect.stabilityLost() > 0) {
            run.loseStability(
                    effect.stabilityLost());
        }

        if (effect.insightGained() > 0) {
            run.gainInsight(
                    effect.insightGained());
        }
    }
}