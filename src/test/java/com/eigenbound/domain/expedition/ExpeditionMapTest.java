package com.eigenbound.domain.expedition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpeditionMapTest {

    private ExpeditionNode start;
    private ExpeditionNode challenge;
    private ExpeditionNode rest;
    private ExpeditionNode reward;
    private ExpeditionNode boss;
    private List<ExpeditionNode> nodes;
    private List<ExpeditionEdge> edges;

    @BeforeEach
    void setUp() {
        start = new ExpeditionNode(
                "start",
                0,
                RoomType.START,
                0);

        challenge = new ExpeditionNode(
                "challenge",
                1,
                RoomType.VECTOR_CHALLENGE,
                1);

        rest = new ExpeditionNode(
                "rest",
                1,
                RoomType.REST,
                0);

        reward = new ExpeditionNode(
                "reward",
                2,
                RoomType.REWARD,
                0);

        boss = new ExpeditionNode(
                "boss",
                3,
                RoomType.BOSS,
                3);

        nodes = List.of(
                start,
                challenge,
                rest,
                reward,
                boss);

        edges = List.of(
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
                        "boss"));
    }

    @Test
    void shouldFindNodeById() {
        ExpeditionMap map = createMap();

        assertEquals(
                challenge,
                map.findNode("challenge"));
    }

    @Test
    void shouldReturnDirectNeighbors() {
        ExpeditionMap map = createMap();

        assertEquals(
                List.of(challenge, rest),
                map.neighborsOf("start"));
    }

    @Test
    void shouldDetectDirectConnection() {
        ExpeditionMap map = createMap();

        assertTrue(
                map.hasConnection(
                        "start",
                        "challenge"));

        assertFalse(
                map.hasConnection(
                        "start",
                        "boss"));
    }

    @Test
    void shouldDetectReachableNodeUsingBfs() {
        ExpeditionMap map = createMap();

        assertTrue(
                map.isReachable(
                        "start",
                        "boss"));
    }

    @Test
    void shouldDetectUnreachableNode() {
        ExpeditionMap map = createMap();

        assertFalse(
                map.isReachable(
                        "challenge",
                        "rest"));
    }

    @Test
    void nodeShouldReachItself() {
        ExpeditionMap map = createMap();

        assertTrue(
                map.isReachable(
                        "reward",
                        "reward"));
    }

    @Test
    void collectionsShouldBeImmutable() {
        ExpeditionMap map = createMap();

        assertThrows(
                UnsupportedOperationException.class,
                () -> map.nodes().clear());

        assertThrows(
                UnsupportedOperationException.class,
                () -> map.edges().clear());

        assertThrows(
                UnsupportedOperationException.class,
                () -> map.neighborsOf("start").clear());
    }

    @Test
    void shouldProtectOriginalCollections() {
        List<ExpeditionNode> mutableNodes = new ArrayList<>(nodes);

        List<ExpeditionEdge> mutableEdges = new ArrayList<>(edges);

        ExpeditionMap map = new ExpeditionMap(
                mutableNodes,
                mutableEdges,
                "start",
                "boss");

        mutableNodes.clear();
        mutableEdges.clear();

        assertEquals(5, map.nodes().size());
        assertEquals(5, map.edges().size());
    }

    @Test
    void shouldRejectDuplicateNodeIds() {
        List<ExpeditionNode> duplicated = new ArrayList<>(nodes);

        duplicated.add(
                new ExpeditionNode(
                        "challenge",
                        2,
                        RoomType.ELITE_CHALLENGE,
                        3));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        duplicated,
                        edges,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectDuplicateEdges() {
        List<ExpeditionEdge> duplicated = new ArrayList<>(edges);

        duplicated.add(edges.get(0));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        nodes,
                        duplicated,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectUnknownEdgeEndpoint() {
        List<ExpeditionEdge> invalidEdges = new ArrayList<>(edges);

        invalidEdges.add(
                new ExpeditionEdge(
                        "reward",
                        "unknown"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        nodes,
                        invalidEdges,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectBackwardEdge() {
        List<ExpeditionEdge> invalidEdges = new ArrayList<>(edges);

        invalidEdges.add(
                new ExpeditionEdge(
                        "reward",
                        "challenge"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        nodes,
                        invalidEdges,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectMissingStartNode() {
        List<ExpeditionNode> invalidNodes = nodes.stream()
                .filter(
                        node -> node.type() != RoomType.START)
                .toList();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        invalidNodes,
                        List.of(),
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectMissingBossNode() {
        List<ExpeditionNode> invalidNodes = nodes.stream()
                .filter(
                        node -> node.type() != RoomType.BOSS)
                .toList();

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        invalidNodes,
                        List.of(),
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectUnreachableBoss() {
        List<ExpeditionEdge> disconnectedEdges = List.of(
                new ExpeditionEdge(
                        "start",
                        "challenge"),
                new ExpeditionEdge(
                        "start",
                        "rest"),
                new ExpeditionEdge(
                        "challenge",
                        "reward"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        nodes,
                        disconnectedEdges,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectMultipleStartNodes() {
        List<ExpeditionNode> invalidNodes = new ArrayList<>(nodes);

        invalidNodes.add(
                new ExpeditionNode(
                        "another-start",
                        0,
                        RoomType.START,
                        0));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        invalidNodes,
                        edges,
                        "start",
                        "boss"));
    }

    @Test
    void shouldRejectWrongConfiguredStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionMap(
                        nodes,
                        edges,
                        "challenge",
                        "boss"));
    }

    private ExpeditionMap createMap() {
        return new ExpeditionMap(
                nodes,
                edges,
                "start",
                "boss");
    }
}