package com.eigenbound.domain.expedition.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionMapTestFixtures;
import com.eigenbound.domain.expedition.ExpeditionNode;
import com.eigenbound.domain.expedition.generation.ExpeditionMapGenerator;

class WarshallReachabilityAnalyzerTest {

    private final WarshallReachabilityAnalyzer analyzer = new WarshallReachabilityAnalyzer();

    @Test
    void shouldIncludeDirectConnections() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        ReachabilityMatrix matrix = analyzer.analyze(map);

        assertTrue(
                matrix.canReach(
                        "start",
                        "challenge"));
    }

    @Test
    void shouldDiscoverIndirectConnections() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        ReachabilityMatrix matrix = analyzer.analyze(map);

        assertTrue(
                matrix.canReach(
                        "start",
                        "boss"));
    }

    @Test
    void shouldNotCreateBackwardConnections() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        ReachabilityMatrix matrix = analyzer.analyze(map);

        assertFalse(
                matrix.canReach(
                        "boss",
                        "start"));
    }

    @Test
    void everyNodeShouldReachItself() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        ReachabilityMatrix matrix = analyzer.analyze(map);

        for (ExpeditionNode node : map.nodes()) {
            assertTrue(
                    matrix.canReach(
                            node.id(),
                            node.id()));
        }
    }

    @Test
    void warshallShouldAgreeWithBfs() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        ReachabilityMatrix matrix = analyzer.analyze(map);

        for (ExpeditionNode source : map.nodes()) {
            for (ExpeditionNode destination : map.nodes()) {

                assertEquals(
                        map.isReachable(
                                source.id(),
                                destination.id()),
                        matrix.canReach(
                                source.id(),
                                destination.id()),
                        source.id()
                                + " -> "
                                + destination.id());
            }
        }
    }

    @Test
    void shouldAgreeWithBfsForProceduralMaps() {
        ExpeditionMapGenerator generator = new ExpeditionMapGenerator();

        for (long seed = 0; seed < 50; seed++) {

            for (int difficulty = 1; difficulty <= 5; difficulty++) {

                ExpeditionMap map = generator.generate(
                        seed,
                        difficulty).map();

                ReachabilityMatrix matrix = analyzer.analyze(map);

                for (ExpeditionNode source : map.nodes()) {

                    for (ExpeditionNode destination : map.nodes()) {

                        assertEquals(
                                map.isReachable(
                                        source.id(),
                                        destination.id()),
                                matrix.canReach(
                                        source.id(),
                                        destination.id()),
                                "Mismatch for seed "
                                        + seed
                                        + ": "
                                        + source.id()
                                        + " -> "
                                        + destination.id());
                    }
                }
            }
        }
    }

    @Test
    void shouldRejectNullMap() {
        assertThrows(
                NullPointerException.class,
                () -> analyzer.analyze(null));
    }
}