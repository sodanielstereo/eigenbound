package com.eigenbound.domain.expedition.analysis;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReachabilityMatrixTest {

    private List<String> nodeIds;
    private boolean[][] values;

    @BeforeEach
    void setUp() {
        nodeIds = new ArrayList<>(
                List.of("a", "b", "c"));

        values = new boolean[][] {
                { true, true, true },
                { false, true, true },
                { false, false, true }
        };
    }

    @Test
    void shouldReportMatrixSize() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertEquals(3, matrix.size());
        assertEquals(
                List.of("a", "b", "c"),
                matrix.nodeIds());
    }

    @Test
    void shouldReportReachableNodes() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertTrue(matrix.canReach("a", "c"));
        assertTrue(matrix.canReach("b", "c"));
        assertFalse(matrix.canReach("c", "a"));
    }

    @Test
    void everyNodeShouldReachItself() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertTrue(matrix.canReach("a", "a"));
        assertTrue(matrix.canReach("b", "b"));
        assertTrue(matrix.canReach("c", "c"));
    }

    @Test
    void shouldReturnReachableSubgraphNodes() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertEquals(
                List.of("a", "b", "c"),
                matrix.reachableFrom("a"));

        assertEquals(
                List.of("b", "c"),
                matrix.reachableFrom("b"));

        assertEquals(
                List.of("c"),
                matrix.reachableFrom("c"));
    }

    @Test
    void shouldProtectOriginalInputs() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        nodeIds.clear();
        values[0][2] = false;

        assertEquals(3, matrix.size());
        assertTrue(matrix.canReach("a", "c"));
    }

    @Test
    void returnedMatrixShouldBeIndependent() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        boolean[][] exposed = matrix.matrixCopy();

        exposed[0][2] = false;

        assertTrue(matrix.canReach("a", "c"));
    }

    @Test
    void nodeIdListShouldBeImmutable() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertThrows(
                UnsupportedOperationException.class,
                () -> matrix.nodeIds().clear());
    }

    @Test
    void shouldRejectUnknownNodeId() {
        ReachabilityMatrix matrix = new ReachabilityMatrix(
                nodeIds,
                values);

        assertThrows(
                IllegalArgumentException.class,
                () -> matrix.canReach(
                        "unknown",
                        "a"));
    }

    @Test
    void shouldRejectNonSquareMatrix() {
        boolean[][] invalidMatrix = {
                { true, false },
                { false }
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReachabilityMatrix(
                        List.of("a", "b"),
                        invalidMatrix));
    }

    @Test
    void shouldRejectDuplicateNodeIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ReachabilityMatrix(
                        List.of("a", "a"),
                        new boolean[][] {
                                { true, false },
                                { false, true }
                        }));
    }
}