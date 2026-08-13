package com.eigenbound.application.event;

import java.util.Objects;

import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.puzzle.cryptography.CaesarCipherPuzzleGenerator;
import com.eigenbound.domain.puzzle.generation.MiniPuzzleGenerator;

/**
 * Generates playable content for the pending room of an expedition run.
 *
 * <p>
 * Room selection and expedition progress remain managed by
 * {@link ExpeditionRun}. This factory only translates the selected room into
 * deterministic playable content and does not complete or cancel the room.
 * </p>
 */
public final class ExpeditionRoomEventFactory {

    private static final int MAX_DIFFICULTY = 5;

    private final VectorChallengeGenerator vectorChallengeGenerator;
    private final MiniPuzzleGenerator miniPuzzleGenerator;

    /**
     * Creates a factory using the standard vector and Caesar puzzle
     * generators.
     */
    public ExpeditionRoomEventFactory() {
        this(
                new VectorChallengeGenerator(),
                new CaesarCipherPuzzleGenerator());
    }

    /**
     * Creates a factory with explicit content generators.
     *
     * @param vectorChallengeGenerator generator for challenge rooms
     * @param miniPuzzleGenerator      generator for alternative rooms
     */
    public ExpeditionRoomEventFactory(
            VectorChallengeGenerator vectorChallengeGenerator,
            MiniPuzzleGenerator miniPuzzleGenerator) {
        this.vectorChallengeGenerator = Objects.requireNonNull(
                vectorChallengeGenerator,
                "Vector challenge generator cannot be null");
        this.miniPuzzleGenerator = Objects.requireNonNull(
                miniPuzzleGenerator,
                "Mini-puzzle generator cannot be null");
    }

    /**
     * Generates the event associated with the run's pending room.
     *
     * <p>
     * Generation uses the deterministic room seed derived by the expedition
     * run. Calling this method for equivalent runs therefore produces
     * equivalent content.
     * </p>
     *
     * @param run expedition containing a pending room
     * @return generated room event
     * @throws IllegalStateException when the run has no pending room
     */
    public ExpeditionRoomEvent create(
            ExpeditionRun run) {
        Objects.requireNonNull(
                run,
                "Expedition run cannot be null");

        ExpeditionNode room = run
                .pendingRoom()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No expedition room is currently pending"));

        long seed = run.pendingChallengeSeed();

        return switch (room.type()) {
            case VECTOR_CHALLENGE,
                    ELITE_CHALLENGE,
                    BOSS ->
                createVectorEvent(
                        room,
                        seed);

            case REST,
                    REWARD ->
                createMiniPuzzleEvent(
                        run,
                        room,
                        seed);

            case START -> throw new IllegalStateException(
                    "Start room cannot produce a playable event");
        };
    }

    /**
     * Generates verified vector content using the room difficulty.
     */
    private VectorChallengeRoomEvent createVectorEvent(
            ExpeditionNode room,
            long seed) {
        return new VectorChallengeRoomEvent(
                room,
                vectorChallengeGenerator.generate(
                        seed,
                        room.difficulty()));
    }

    /**
     * Generates a mini-puzzle with difficulty based on expedition progress.
     */
    private MiniPuzzleRoomEvent createMiniPuzzleEvent(
            ExpeditionRun run,
            ExpeditionNode room,
            long seed) {
        int difficulty = miniPuzzleDifficulty(
                run,
                room);

        return new MiniPuzzleRoomEvent(
                room,
                miniPuzzleGenerator.generate(
                        seed,
                        difficulty));
    }

    /**
     * Calculates difficulty for rest and reward rooms.
     *
     * <p>
     * Alternative rooms use zero as their map difficulty because they do not
     * contain vector challenges. Mini-puzzles instead begin at the expedition
     * difficulty and gain one level after the run passes its halfway point.
     * </p>
     */
    private int miniPuzzleDifficulty(
            ExpeditionRun run,
            ExpeditionNode room) {
        int bossLayer = run
                .expeditionSession()
                .map()
                .findNode(
                        run.expeditionSession()
                                .map()
                                .bossNodeId())
                .layer();

        boolean pastHalfwayPoint = room.layer() * 2L > bossLayer;

        int progressionBonus = pastHalfwayPoint
                ? 1
                : 0;

        return Math.min(
                MAX_DIFFICULTY,
                run.difficulty() + progressionBonus);
    }
}