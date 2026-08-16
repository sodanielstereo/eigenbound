package com.eigenbound.application.event;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.application.effect.MiniPuzzleRoomEffectPolicy;
import com.eigenbound.application.effect.RoomEffect;
import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.ExpeditionResources;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;
import com.eigenbound.domain.puzzle.PuzzleResult;
import com.eigenbound.domain.puzzle.cryptography.CaesarCipherPuzzleGenerator;

class ExpeditionRoomResolverTest {

    private static final long SEED = 73L;
    private static final int DIFFICULTY = 3;
    private static final Duration ELAPSED_TIME = Duration.ofSeconds(5);
    private static final String EXPLANATION = "Puzzle feedback";

    private ExpeditionMap map;
    private ExpeditionRun run;
    private ExpeditionRoomEventFactory eventFactory;
    private ExpeditionRoomResolver resolver;

    @BeforeEach
    void setUp() {
        map = createMap();
        run = new ExpeditionRun(
                new GeneratedExpedition(
                        map,
                        SEED,
                        DIFFICULTY));
        eventFactory = new ExpeditionRoomEventFactory();
        resolver = new ExpeditionRoomResolver();
    }

    @Test
    void shouldRestoreStabilityAndCompleteSolvedRestRoom() {
        run.loseStability(40);
        MiniPuzzleRoomEvent event = selectEvent("rest");

        RoomEffect effect = resolver.resolveMiniPuzzle(
                run,
                event,
                solvedResult(event));

        assertEquals(
                RoomEffect.restoreStability(25),
                effect);
        assertEquals(85, run.resources().stability());
        assertEquals(
                map.findNode("rest"),
                run.expeditionSession().currentNode());
        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldGrantInsightAndCompleteSolvedRewardRoom() {
        MiniPuzzleRoomEvent event = selectEvent("reward");

        RoomEffect effect = resolver.resolveMiniPuzzle(
                run,
                event,
                solvedResult(event));

        assertEquals(
                RoomEffect.gainInsight(30L),
                effect);
        assertEquals(30L, run.resources().insight());
        assertEquals(
                map.findNode("reward"),
                run.expeditionSession().currentNode());
        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldCompleteIncorrectRoomWithoutChangingResources() {
        MiniPuzzleRoomEvent event = selectEvent("reward");
        ExpeditionResources resourcesBefore = run.resources();

        RoomEffect effect = resolver.resolveMiniPuzzle(
                run,
                event,
                PuzzleResult.incorrect(
                        "wrong-option",
                        ELAPSED_TIME,
                        EXPLANATION));

        assertEquals(RoomEffect.none(), effect);
        assertEquals(resourcesBefore, run.resources());
        assertEquals(
                map.findNode("reward"),
                run.expeditionSession().currentNode());
    }

    @Test
    void shouldRemoveStabilityAndCompleteTimedOutRoom() {
        MiniPuzzleRoomEvent event = selectEvent("rest");

        RoomEffect effect = resolver.resolveMiniPuzzle(
                run,
                event,
                timedOutResult());

        assertEquals(
                RoomEffect.loseStability(11),
                effect);
        assertEquals(89, run.resources().stability());
        assertEquals(
                map.findNode("rest"),
                run.expeditionSession().currentNode());
    }

    @Test
    void shouldCancelRoomWithoutMovingOrChangingResources() {
        MiniPuzzleRoomEvent event = selectEvent("rest");
        ExpeditionResources resourcesBefore = run.resources();

        RoomEffect effect = resolver.resolveMiniPuzzle(
                run,
                event,
                PuzzleResult.cancelled(
                        ELAPSED_TIME,
                        EXPLANATION));

        assertEquals(RoomEffect.none(), effect);
        assertEquals(resourcesBefore, run.resources());
        assertEquals(
                map.findNode("start"),
                run.expeditionSession().currentNode());
        assertFalse(run.hasPendingRoom());
        assertTrue(
                run.expeditionSession().canMoveTo("rest"));
    }

    @Test
    void shouldClearPendingRoomWithoutMovingWhenTimeoutCollapsesRun() {
        run.loseStability(89);
        MiniPuzzleRoomEvent event = selectEvent("reward");

        resolver.resolveMiniPuzzle(
                run,
                event,
                timedOutResult());

        assertEquals(0, run.resources().stability());
        assertTrue(run.isFailed());
        assertFalse(run.hasPendingRoom());
        assertEquals(
                map.findNode("start"),
                run.expeditionSession().currentNode());
    }

    @Test
    void shouldRejectApplyingSameResultTwice() {
        MiniPuzzleRoomEvent event = selectEvent("reward");
        PuzzleResult result = solvedResult(event);

        resolver.resolveMiniPuzzle(
                run,
                event,
                result);

        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolveMiniPuzzle(
                        run,
                        event,
                        result));

        assertEquals(30L, run.resources().insight());
    }

    @Test
    void shouldRejectEventForDifferentPendingRoom() {
        run.selectRoom("rest");

        MiniPuzzleRoomEvent differentEvent = new MiniPuzzleRoomEvent(
                map.findNode("reward"),
                new CaesarCipherPuzzleGenerator().generate(
                        91L,
                        DIFFICULTY));

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolveMiniPuzzle(
                        run,
                        differentEvent,
                        timedOutResult()));

        assertEquals(100, run.resources().stability());
        assertEquals(
                map.findNode("rest"),
                run.pendingRoom().orElseThrow());
    }

    @Test
    void shouldRejectResolutionWithoutPendingRoom() {
        ExpeditionNode restRoom = map.findNode("rest");
        MiniPuzzleRoomEvent event = new MiniPuzzleRoomEvent(
                restRoom,
                new CaesarCipherPuzzleGenerator().generate(
                        91L,
                        DIFFICULTY));

        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolveMiniPuzzle(
                        run,
                        event,
                        timedOutResult()));
    }

    @Test
    void shouldRejectNullArguments() {
        MiniPuzzleRoomEvent event = selectEvent("rest");
        PuzzleResult result = timedOutResult();

        assertThrows(
                NullPointerException.class,
                () -> resolver.resolveMiniPuzzle(
                        null,
                        event,
                        result));
        assertThrows(
                NullPointerException.class,
                () -> resolver.resolveMiniPuzzle(
                        run,
                        null,
                        result));
        assertThrows(
                NullPointerException.class,
                () -> resolver.resolveMiniPuzzle(
                        run,
                        event,
                        null));
    }

    @Test
    void shouldRejectNullEffectPolicy() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionRoomResolver(
                        (MiniPuzzleRoomEffectPolicy) null));
    }

    private MiniPuzzleRoomEvent selectEvent(
            String roomId) {
        run.selectRoom(roomId);

        return (MiniPuzzleRoomEvent) eventFactory.create(run);
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

    private ExpeditionMap createMap() {
        ExpeditionNode start = new ExpeditionNode(
                "start",
                0,
                RoomType.START,
                0);

        ExpeditionNode rest = new ExpeditionNode(
                "rest",
                1,
                RoomType.REST,
                0);

        ExpeditionNode reward = new ExpeditionNode(
                "reward",
                1,
                RoomType.REWARD,
                0);

        ExpeditionNode boss = new ExpeditionNode(
                "boss",
                2,
                RoomType.BOSS,
                3);

        return new ExpeditionMap(
                List.of(
                        start,
                        rest,
                        reward,
                        boss),
                List.of(
                        new ExpeditionEdge(
                                "start",
                                "rest"),
                        new ExpeditionEdge(
                                "start",
                                "reward"),
                        new ExpeditionEdge(
                                "rest",
                                "boss"),
                        new ExpeditionEdge(
                                "reward",
                                "boss")),
                "start",
                "boss");
    }
}