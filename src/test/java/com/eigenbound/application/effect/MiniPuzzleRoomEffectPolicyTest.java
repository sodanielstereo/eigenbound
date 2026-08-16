package com.eigenbound.application.effect;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.application.event.MiniPuzzleRoomEvent;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.puzzle.PuzzleResult;
import com.eigenbound.domain.puzzle.cryptography.CaesarCipherPuzzleGenerator;

class MiniPuzzleRoomEffectPolicyTest {

    private static final long SEED = 401L;
    private static final Duration ELAPSED_TIME = Duration.ofSeconds(4);
    private static final String EXPLANATION = "Puzzle feedback";

    private final CaesarCipherPuzzleGenerator generator = new CaesarCipherPuzzleGenerator();

    private final MiniPuzzleRoomEffectPolicy policy = new MiniPuzzleRoomEffectPolicy();

    @Test
    void shouldRestoreStabilityAfterSolvingRestRoom() {
        MiniPuzzleRoomEvent event = event(
                RoomType.REST,
                3);

        RoomEffect effect = policy.determineEffect(
                event,
                solvedResult(event));

        assertEquals(
                RoomEffect.restoreStability(25),
                effect);
    }

    @Test
    void shouldScaleRestorationWithDifficulty() {
        MiniPuzzleRoomEvent easiest = event(
                RoomType.REST,
                1);
        MiniPuzzleRoomEvent hardest = event(
                RoomType.REST,
                5);

        assertEquals(
                RoomEffect.restoreStability(15),
                policy.determineEffect(
                        easiest,
                        solvedResult(easiest)));

        assertEquals(
                RoomEffect.restoreStability(35),
                policy.determineEffect(
                        hardest,
                        solvedResult(hardest)));
    }

    @Test
    void shouldGrantInsightAfterSolvingRewardRoom() {
        MiniPuzzleRoomEvent event = event(
                RoomType.REWARD,
                4);

        RoomEffect effect = policy.determineEffect(
                event,
                solvedResult(event));

        assertEquals(
                RoomEffect.gainInsight(40L),
                effect);
    }

    @Test
    void shouldScaleInsightWithDifficulty() {
        MiniPuzzleRoomEvent easiest = event(
                RoomType.REWARD,
                1);
        MiniPuzzleRoomEvent hardest = event(
                RoomType.REWARD,
                5);

        assertEquals(
                RoomEffect.gainInsight(10L),
                policy.determineEffect(
                        easiest,
                        solvedResult(easiest)));

        assertEquals(
                RoomEffect.gainInsight(50L),
                policy.determineEffect(
                        hardest,
                        solvedResult(hardest)));
    }

    @Test
    void shouldNotChangeResourcesAfterIncorrectAnswer() {
        MiniPuzzleRoomEvent event = event(
                RoomType.REWARD,
                3);

        RoomEffect effect = policy.determineEffect(
                event,
                PuzzleResult.incorrect(
                        "wrong-option",
                        ELAPSED_TIME,
                        EXPLANATION));

        assertFalse(effect.changesResources());
    }

    @Test
    void shouldRemoveStabilityAfterTimeout() {
        MiniPuzzleRoomEvent event = event(
                RoomType.REST,
                3);

        RoomEffect effect = policy.determineEffect(
                event,
                PuzzleResult.timedOut(
                        ELAPSED_TIME,
                        EXPLANATION));

        assertEquals(
                RoomEffect.loseStability(11),
                effect);
    }

    @Test
    void shouldScaleTimeoutDamageWithDifficulty() {
        MiniPuzzleRoomEvent easiest = event(
                RoomType.REST,
                1);
        MiniPuzzleRoomEvent hardest = event(
                RoomType.REWARD,
                5);

        assertEquals(
                RoomEffect.loseStability(7),
                policy.determineEffect(
                        easiest,
                        timedOutResult()));

        assertEquals(
                RoomEffect.loseStability(15),
                policy.determineEffect(
                        hardest,
                        timedOutResult()));
    }

    @Test
    void shouldNotChangeResourcesAfterCancellation() {
        MiniPuzzleRoomEvent event = event(
                RoomType.REST,
                2);

        RoomEffect effect = policy.determineEffect(
                event,
                PuzzleResult.cancelled(
                        ELAPSED_TIME,
                        EXPLANATION));

        assertEquals(RoomEffect.none(), effect);
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                NullPointerException.class,
                () -> policy.determineEffect(
                        null,
                        timedOutResult()));
    }

    @Test
    void shouldRejectNullResult() {
        assertThrows(
                NullPointerException.class,
                () -> policy.determineEffect(
                        event(RoomType.REWARD, 2),
                        null));
    }

    private MiniPuzzleRoomEvent event(
            RoomType roomType,
            int difficulty) {
        ExpeditionNode room = new ExpeditionNode(
                "room-1-0",
                1,
                roomType,
                0);

        return new MiniPuzzleRoomEvent(
                room,
                generator.generate(
                        SEED,
                        difficulty));
    }

    private PuzzleResult solvedResult(
            MiniPuzzleRoomEvent event) {
        return PuzzleResult.solved(
                event.generatedPuzzle()
                        .puzzle()
                        .correctOptionId(),
                ELAPSED_TIME,
                EXPLANATION);
    }

    private PuzzleResult timedOutResult() {
        return PuzzleResult.timedOut(
                ELAPSED_TIME,
                EXPLANATION);
    }
}