package com.eigenbound.application.event;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eigenbound.application.session.ExpeditionRun;
import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.puzzle.cryptography.CaesarCipherPuzzleGenerator;

class ExpeditionRoomEventFactoryTest {

    private static final long EXPEDITION_SEED = 73L;

    private final ExpeditionRoomEventFactory factory = new ExpeditionRoomEventFactory();

    @Test
    void shouldCreateVectorEventForChallengeRoom() {
        ExpeditionRun run = createRun(3);
        run.selectRoom("vector");

        ExpeditionRoomEvent event = factory.create(run);

        VectorChallengeRoomEvent vectorEvent = assertInstanceOf(
                VectorChallengeRoomEvent.class,
                event);

        assertEquals(RoomEventKind.VECTOR_CHALLENGE, event.kind());
        assertEquals(run.pendingRoom().orElseThrow(), event.room());
        assertEquals(run.pendingChallengeSeed(), event.seed());
        assertEquals(3, event.difficulty());
        assertEquals(
                event.seed(),
                vectorEvent.generatedChallenge().seed());
    }

    @Test
    void shouldCreateMiniPuzzleEventForRestRoom() {
        ExpeditionRun run = createRun(3);
        run.selectRoom("rest");

        ExpeditionRoomEvent event = factory.create(run);

        MiniPuzzleRoomEvent puzzleEvent = assertInstanceOf(
                MiniPuzzleRoomEvent.class,
                event);

        assertEquals(RoomEventKind.MINI_PUZZLE, event.kind());
        assertEquals(run.pendingChallengeSeed(), event.seed());
        assertEquals(3, event.difficulty());
        assertEquals(
                event.seed(),
                puzzleEvent.generatedPuzzle().seed());
    }

    @Test
    void shouldCreateMiniPuzzleEventForRewardRoom() {
        ExpeditionRun run = createRun(2);
        run.selectRoom("reward");

        ExpeditionRoomEvent event = factory.create(run);

        assertInstanceOf(
                MiniPuzzleRoomEvent.class,
                event);
        assertEquals(RoomType.REWARD, event.room().type());
        assertEquals(2, event.difficulty());
    }

    @Test
    void shouldIncreaseMiniPuzzleDifficultyPastHalfwayPoint() {
        ExpeditionRun run = createRun(3);
        moveToLateReward(run);

        ExpeditionRoomEvent event = factory.create(run);

        assertEquals(RoomType.REWARD, event.room().type());
        assertEquals(4, event.difficulty());
    }

    @Test
    void shouldCapMiniPuzzleDifficultyAtFive() {
        ExpeditionRun run = createRun(5);
        moveToLateReward(run);

        ExpeditionRoomEvent event = factory.create(run);

        assertEquals(5, event.difficulty());
    }

    @Test
    void shouldCreateBossVectorEvent() {
        ExpeditionRun run = createRun(4);
        run.selectRoom("vector");
        run.completePendingRoom();
        run.selectRoom("boss");

        ExpeditionRoomEvent event = factory.create(run);

        assertInstanceOf(
                VectorChallengeRoomEvent.class,
                event);
        assertEquals(RoomType.BOSS, event.room().type());
        assertEquals(4, event.difficulty());
    }

    @Test
    void shouldGenerateEquivalentEventsForEquivalentRuns() {
        ExpeditionRun firstRun = createRun(3);
        ExpeditionRun secondRun = createRun(3);

        firstRun.selectRoom("rest");
        secondRun.selectRoom("rest");

        MiniPuzzleRoomEvent firstEvent = assertInstanceOf(
                MiniPuzzleRoomEvent.class,
                factory.create(firstRun));
        MiniPuzzleRoomEvent secondEvent = assertInstanceOf(
                MiniPuzzleRoomEvent.class,
                factory.create(secondRun));

        assertEquals(firstEvent, secondEvent);
    }

    @Test
    void shouldNotCommitPendingRoomWhenCreatingEvent() {
        ExpeditionRun run = createRun(3);
        ExpeditionNode startingRoom = run
                .expeditionSession()
                .currentNode();

        run.selectRoom("vector");
        ExpeditionNode pendingRoom = run
                .pendingRoom()
                .orElseThrow();

        factory.create(run);

        assertEquals(
                startingRoom,
                run.expeditionSession().currentNode());
        assertTrue(run.hasPendingRoom());
        assertEquals(
                pendingRoom,
                run.pendingRoom().orElseThrow());
        assertFalse(
                run.expeditionSession()
                        .visitedNodes()
                        .contains(pendingRoom));
    }

    @Test
    void shouldRejectRunWithoutPendingRoom() {
        ExpeditionRun run = createRun(3);

        assertThrows(
                IllegalStateException.class,
                () -> factory.create(run));
    }

    @Test
    void shouldRejectNullRun() {
        assertThrows(
                NullPointerException.class,
                () -> factory.create(null));
    }

    @Test
    void shouldRejectNullVectorGenerator() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionRoomEventFactory(
                        null,
                        new CaesarCipherPuzzleGenerator()));
    }

    @Test
    void shouldRejectNullMiniPuzzleGenerator() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionRoomEventFactory(
                        new VectorChallengeGenerator(),
                        null));
    }

    private void moveToLateReward(
            ExpeditionRun run) {
        run.selectRoom("rest");
        run.completePendingRoom();
        run.selectRoom("late-reward");
    }

    private ExpeditionRun createRun(
            int difficulty) {
        ExpeditionNode start = node(
                "start",
                0,
                RoomType.START,
                0);
        ExpeditionNode vector = node(
                "vector",
                1,
                RoomType.VECTOR_CHALLENGE,
                difficulty);
        ExpeditionNode rest = node(
                "rest",
                1,
                RoomType.REST,
                0);
        ExpeditionNode reward = node(
                "reward",
                1,
                RoomType.REWARD,
                0);
        ExpeditionNode lateReward = node(
                "late-reward",
                4,
                RoomType.REWARD,
                0);
        ExpeditionNode boss = node(
                "boss",
                5,
                RoomType.BOSS,
                difficulty);

        ExpeditionMap map = new ExpeditionMap(
                List.of(
                        start,
                        vector,
                        rest,
                        reward,
                        lateReward,
                        boss),
                List.of(
                        edge("start", "vector"),
                        edge("start", "rest"),
                        edge("start", "reward"),
                        edge("vector", "boss"),
                        edge("rest", "late-reward"),
                        edge("reward", "boss"),
                        edge("late-reward", "boss")),
                "start",
                "boss");

        return new ExpeditionRun(
                new GeneratedExpedition(
                        map,
                        EXPEDITION_SEED,
                        difficulty));
    }

    private ExpeditionNode node(
            String id,
            int layer,
            RoomType type,
            int difficulty) {
        return new ExpeditionNode(
                id,
                layer,
                type,
                difficulty);
    }

    private ExpeditionEdge edge(
            String source,
            String destination) {
        return new ExpeditionEdge(
                source,
                destination);
    }
}