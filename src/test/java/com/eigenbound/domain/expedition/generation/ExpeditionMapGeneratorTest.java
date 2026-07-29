package com.eigenbound.domain.expedition.generation;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.RoomType;

class ExpeditionMapGeneratorTest {

    private final ExpeditionMapGenerator generator = new ExpeditionMapGenerator();

    @Test
    void sameSeedShouldGenerateSameMap() {
        GeneratedExpedition first = generator.generate(2026L, 3);

        GeneratedExpedition second = generator.generate(2026L, 3);

        assertEquals(
                first.map().nodes(),
                second.map().nodes());

        assertEquals(
                first.map().edges(),
                second.map().edges());
    }

    @Test
    void differentSeedsShouldUsuallyGenerateDifferentMaps() {
        GeneratedExpedition first = generator.generate(100L, 4);

        GeneratedExpedition second = generator.generate(200L, 4);

        boolean sameNodes = first.map().nodes()
                .equals(second.map().nodes());

        boolean sameEdges = first.map().edges()
                .equals(second.map().edges());

        assertTrue(!sameNodes || !sameEdges);
    }

    @Test
    void generatedMapShouldHaveOneStartAndBoss() {
        ExpeditionMap map = generator.generate(2026L, 3).map();

        long startCount = map.nodes().stream()
                .filter(
                        node -> node.type() == RoomType.START)
                .count();

        long bossCount = map.nodes().stream()
                .filter(
                        node -> node.type() == RoomType.BOSS)
                .count();

        assertEquals(1, startCount);
        assertEquals(1, bossCount);
    }

    @Test
    void bossShouldAlwaysBeReachable() {
        for (long seed = 0; seed < 500; seed++) {
            final long currentSeed = seed;

            ExpeditionMap map = generator.generate(
                    currentSeed,
                    5).map();

            assertTrue(
                    map.isReachable(
                            map.startNodeId(),
                            map.bossNodeId()),
                    "Boss unreachable for seed "
                            + currentSeed);
        }
    }

    @Test
    void everyEdgeShouldMoveForward() {
        ExpeditionMap map = generator.generate(2026L, 5).map();

        for (ExpeditionEdge edge : map.edges()) {
            ExpeditionNode source = map.findNode(edge.sourceId());

            ExpeditionNode destination = map.findNode(
                    edge.destinationId());

            assertTrue(
                    source.layer() < destination.layer());
        }
    }

    @Test
    void everyNonStartNodeShouldHaveIncomingEdge() {
        ExpeditionMap map = generator.generate(2026L, 5).map();

        for (ExpeditionNode node : map.nodes()) {
            if (node.type() == RoomType.START) {
                continue;
            }

            boolean hasIncoming = map.edges().stream()
                    .anyMatch(
                            edge -> edge.destinationId()
                                    .equals(node.id()));

            assertTrue(
                    hasIncoming,
                    "No incoming edge for "
                            + node.id());
        }
    }

    @Test
    void everyNonBossNodeShouldHaveOutgoingEdge() {
        ExpeditionMap map = generator.generate(2026L, 5).map();

        for (ExpeditionNode node : map.nodes()) {
            if (node.type() == RoomType.BOSS) {
                continue;
            }

            assertTrue(
                    !map.neighborsOf(node.id())
                            .isEmpty(),
                    "No outgoing edge for "
                            + node.id());
        }
    }

    @Test
    void generatedNodeIdsShouldBeUnique() {
        ExpeditionMap map = generator.generate(2026L, 5).map();

        Set<String> ids = new HashSet<>();

        for (ExpeditionNode node : map.nodes()) {
            assertTrue(ids.add(node.id()));
        }
    }

    @Test
    void eachIntermediateLayerShouldContainChallenge() {
        ExpeditionMap map = generator.generate(2026L, 5).map();

        int bossLayer = map.findNode(
                map.bossNodeId()).layer();

        for (int layer = 1; layer < bossLayer; layer++) {

            final int currentLayer = layer;

            boolean containsChallenge = map.nodes().stream()
                    .anyMatch(
                            node -> node.layer() == currentLayer
                                    && node.type() == RoomType.VECTOR_CHALLENGE);

            assertTrue(
                    containsChallenge,
                    "No vector challenge in layer "
                            + currentLayer);
        }
    }

    @Test
    void greaterDifficultyShouldCreateMoreLayers() {
        ExpeditionMap easy = generator.generate(2026L, 1).map();

        ExpeditionMap hard = generator.generate(2026L, 5).map();

        int easyBossLayer = easy.findNode(
                easy.bossNodeId()).layer();

        int hardBossLayer = hard.findNode(
                hard.bossNodeId()).layer();

        assertTrue(
                hardBossLayer > easyBossLayer);
    }

    @Test
    void shouldRejectDifficultyBelowMinimum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(2026L, 0));
    }

    @Test
    void shouldRejectDifficultyAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(2026L, 6));
    }

    @Test
    void differentDifficultiesShouldGenerateDifferentMaps() {
        GeneratedExpedition easy = generator.generate(2026L, 1);

        GeneratedExpedition hard = generator.generate(2026L, 5);

        assertNotEquals(
                easy.map().nodes(),
                hard.map().nodes());
    }
}