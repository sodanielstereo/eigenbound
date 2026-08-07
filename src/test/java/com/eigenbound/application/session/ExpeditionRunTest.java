package com.eigenbound.application.session;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

class ExpeditionRunTest {

    private ExpeditionMap map;
    private ExpeditionRun run;

    @BeforeEach
    void setUp() {
        map = createMap();

        run = new ExpeditionRun(
                new GeneratedExpedition(
                        map,
                        73L,
                        3));
    }

    @Test
    void shouldStartAtExpeditionStartNode() {
        assertEquals(
                map.findNode("start"),
                run.expeditionSession().currentNode());
    }

    @Test
    void shouldExposeOriginalGenerationSettings() {
        assertEquals(73L, run.seed());
        assertEquals(3, run.difficulty());
    }

    @Test
    void shouldStartWithoutPendingRoom() {
        assertTrue(run.pendingRoom().isEmpty());
        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldSelectAvailableRoomWithoutMovingPlayer() {
        run.selectRoom("challenge");

        assertEquals(
                map.findNode("challenge"),
                run.pendingRoom().orElseThrow());

        assertEquals(
                map.findNode("start"),
                run.expeditionSession().currentNode());
    }

    @Test
    void shouldRejectUnavailableRoomSelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> run.selectRoom("boss"));

        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldRejectAnotherSelectionWhileRoomIsPending() {
        run.selectRoom("challenge");

        assertThrows(
                IllegalStateException.class,
                () -> run.selectRoom("rest"));

        assertEquals(
                map.findNode("challenge"),
                run.pendingRoom().orElseThrow());
    }

    @Test
    void shouldCompletePendingRoomAndCommitMovement() {
        run.selectRoom("challenge");

        run.completePendingRoom();

        assertEquals(
                map.findNode("challenge"),
                run.expeditionSession().currentNode());

        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldCancelPendingRoomWithoutMovingPlayer() {
        run.selectRoom("challenge");

        run.cancelPendingRoom();

        assertEquals(
                map.findNode("start"),
                run.expeditionSession().currentNode());

        assertFalse(run.hasPendingRoom());
    }

    @Test
    void shouldRejectCompletionWithoutPendingRoom() {
        assertThrows(
                IllegalStateException.class,
                run::completePendingRoom);
    }

    @Test
    void shouldRejectCancellationWithoutPendingRoom() {
        assertThrows(
                IllegalStateException.class,
                run::cancelPendingRoom);
    }

    @Test
    void shouldRejectNullRoomIdentifier() {
        assertThrows(
                NullPointerException.class,
                () -> run.selectRoom(null));
    }

    @Test
    void shouldRejectNullGeneratedExpedition() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionRun(null));
    }

    private ExpeditionMap createMap() {
        ExpeditionNode start = new ExpeditionNode(
                "start",
                0,
                RoomType.START,
                0);

        ExpeditionNode challenge = new ExpeditionNode(
                "challenge",
                1,
                RoomType.VECTOR_CHALLENGE,
                2);

        ExpeditionNode rest = new ExpeditionNode(
                "rest",
                1,
                RoomType.REST,
                0);

        ExpeditionNode boss = new ExpeditionNode(
                "boss",
                2,
                RoomType.BOSS,
                3);

        return new ExpeditionMap(
                List.of(
                        start,
                        challenge,
                        rest,
                        boss),
                List.of(
                        new ExpeditionEdge(
                                "start",
                                "challenge"),
                        new ExpeditionEdge(
                                "start",
                                "rest"),
                        new ExpeditionEdge(
                                "challenge",
                                "boss"),
                        new ExpeditionEdge(
                                "rest",
                                "boss")),
                "start",
                "boss");
    }
}