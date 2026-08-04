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

class ExpeditionSessionTest {

    private ExpeditionMap map;
    private ExpeditionSession session;

    @BeforeEach
    void setUp() {
        map = createMap();
        session = new ExpeditionSession(map);
    }

    @Test
    void shouldBeginAtConfiguredStartNode() {
        assertEquals(
                map.findNode("start"),
                session.currentNode());
    }

    @Test
    void shouldBeginWithStartNodeVisited() {
        assertEquals(
                List.of(map.findNode("start")),
                session.visitedNodes());
        assertTrue(session.hasVisited("start"));
    }

    @Test
    void shouldExposeDirectlyConnectedRooms() {
        assertEquals(
                List.of(
                        map.findNode("challenge"),
                        map.findNode("rest")),
                session.availableNodes());
    }

    @Test
    void shouldIdentifyAvailableMovement() {
        assertTrue(session.canMoveTo("challenge"));
        assertFalse(session.canMoveTo("boss"));
    }

    @Test
    void shouldMoveToConnectedRoom() {
        session.moveTo("challenge");

        assertEquals(
                map.findNode("challenge"),
                session.currentNode());
    }

    @Test
    void shouldTrackVisitedRoomsInTraversalOrder() {
        session.moveTo("challenge");
        session.moveTo("reward");

        assertEquals(
                List.of(
                        map.findNode("start"),
                        map.findNode("challenge"),
                        map.findNode("reward")),
                session.visitedNodes());
    }

    @Test
    void shouldRejectMovementToNonAdjacentRoom() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.moveTo("boss"));

        assertEquals(
                map.findNode("start"),
                session.currentNode());
    }

    @Test
    void shouldRejectMovementToUnknownRoom() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session.moveTo("unknown"));
    }

    @Test
    void shouldRemainIncompleteBeforeBossRoom() {
        session.moveTo("challenge");

        assertFalse(session.isCompleted());
    }

    @Test
    void shouldCompleteExpeditionAtBossRoom() {
        reachBoss();

        assertTrue(session.isCompleted());
        assertEquals(
                map.findNode("boss"),
                session.currentNode());
    }

    @Test
    void shouldExposeNoAvailableRoomsAfterCompletion() {
        reachBoss();

        assertTrue(session.availableNodes().isEmpty());
    }

    @Test
    void shouldRejectMovementAfterCompletion() {
        reachBoss();

        assertThrows(
                IllegalStateException.class,
                () -> session.moveTo("start"));
    }

    @Test
    void visitedRoomListShouldBeImmutable() {
        List<ExpeditionNode> visited = session.visitedNodes();

        assertThrows(
                UnsupportedOperationException.class,
                () -> visited.add(map.findNode("challenge")));
    }

    @Test
    void shouldRejectNullMap() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionSession(null));
    }

    private void reachBoss() {
        session.moveTo("challenge");
        session.moveTo("reward");
        session.moveTo("boss");
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
                1);

        ExpeditionNode rest = new ExpeditionNode(
                "rest",
                1,
                RoomType.REST,
                0);

        ExpeditionNode reward = new ExpeditionNode(
                "reward",
                2,
                RoomType.REWARD,
                0);

        ExpeditionNode boss = new ExpeditionNode(
                "boss",
                3,
                RoomType.BOSS,
                3);

        return new ExpeditionMap(
                List.of(
                        start,
                        challenge,
                        rest,
                        reward,
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
                                "reward"),
                        new ExpeditionEdge(
                                "rest",
                                "reward"),
                        new ExpeditionEdge(
                                "reward",
                                "boss")),
                "start",
                "boss");
    }
}