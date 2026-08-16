package com.eigenbound.application.effect;

import java.util.Objects;

import com.eigenbound.application.event.MiniPuzzleRoomEvent;
import com.eigenbound.domain.puzzle.PuzzleResult;

/**
 * Calculates the expedition resource effect of a completed mini-puzzle room.
 *
 * <p>
 * This policy is intentionally pure: it reads an event and its result, then
 * returns an immutable {@link RoomEffect} without mutating an expedition run.
 * Rest rooms recover stability after a solved puzzle, reward rooms grant
 * insight, incorrect answers grant nothing and a fully expired timer removes
 * stability.
 * </p>
 */
public final class MiniPuzzleRoomEffectPolicy {

    private static final int REST_BASE_RESTORATION = 10;
    private static final int RESTORATION_PER_DIFFICULTY = 5;
    private static final long INSIGHT_PER_DIFFICULTY = 10L;
    private static final int TIMEOUT_BASE_DAMAGE = 5;
    private static final int TIMEOUT_DAMAGE_PER_DIFFICULTY = 2;

    /**
     * Determines the resource changes produced by one terminal puzzle result.
     *
     * @param event  mini-puzzle room being resolved
     * @param result terminal result produced by its puzzle session
     * @return immutable room effect
     * @throws NullPointerException when the event or result is null
     */
    public RoomEffect determineEffect(
            MiniPuzzleRoomEvent event,
            PuzzleResult result) {
        Objects.requireNonNull(
                event,
                "Mini-puzzle room event cannot be null");
        Objects.requireNonNull(
                result,
                "Puzzle result cannot be null");

        return switch (result.status()) {
            case SOLVED -> solvedEffect(event);
            case TIMED_OUT -> timeoutEffect(event.difficulty());
            case INCORRECT, CANCELLED -> RoomEffect.none();
        };
    }

    /**
     * Calculates the reward associated with a successfully solved room.
     */
    private RoomEffect solvedEffect(
            MiniPuzzleRoomEvent event) {
        return switch (event.room().type()) {
            case REST -> RoomEffect.restoreStability(
                    restRestoration(event.difficulty()));
            case REWARD -> RoomEffect.gainInsight(
                    rewardInsight(event.difficulty()));
            default -> throw new IllegalStateException(
                    "Mini-puzzle event has an unsupported room type");
        };
    }

    /**
     * Scales rest recovery with puzzle difficulty using exact arithmetic.
     */
    private int restRestoration(
            int difficulty) {
        return Math.addExact(
                REST_BASE_RESTORATION,
                Math.multiplyExact(
                        difficulty,
                        RESTORATION_PER_DIFFICULTY));
    }

    /**
     * Scales the insight reward with puzzle difficulty.
     */
    private long rewardInsight(
            int difficulty) {
        return Math.multiplyExact(
                (long) difficulty,
                INSIGHT_PER_DIFFICULTY);
    }

    /**
     * Calculates the stability damage caused by exhausting the timer.
     */
    private RoomEffect timeoutEffect(
            int difficulty) {
        int damage = Math.addExact(
                TIMEOUT_BASE_DAMAGE,
                Math.multiplyExact(
                        difficulty,
                        TIMEOUT_DAMAGE_PER_DIFFICULTY));

        return RoomEffect.loseStability(damage);
    }
}