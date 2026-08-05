package com.eigenbound.presentation.expedition;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpeditionNodeHitTesterTest {

    private static final double NODE_RADIUS = 22.0;

    private ExpeditionNodeHitTester hitTester;
    private Map<String, ExpeditionNodePosition> positions;

    @BeforeEach
    void setUp() {
        hitTester = new ExpeditionNodeHitTester();
        positions = Map.of(
                "start",
                new ExpeditionNodePosition(
                        "start",
                        100,
                        100),
                "challenge",
                new ExpeditionNodePosition(
                        "challenge",
                        200,
                        100));
    }

    @Test
    void shouldFindNodeAtItsCenter() {
        assertEquals(
                Optional.of("start"),
                hitTester.findNodeAt(
                        positions,
                        100,
                        100,
                        NODE_RADIUS));
    }

    @Test
    void shouldFindNodeAtRadiusBoundary() {
        assertEquals(
                Optional.of("start"),
                hitTester.findNodeAt(
                        positions,
                        122,
                        100,
                        NODE_RADIUS));
    }

    @Test
    void shouldReturnEmptyOutsideEveryNode() {
        assertTrue(
                hitTester.findNodeAt(
                        positions,
                        150,
                        150,
                        NODE_RADIUS)
                        .isEmpty());
    }

    @Test
    void shouldChooseClosestNodeWhenAreasOverlap() {
        Map<String, ExpeditionNodePosition> overlapping = Map.of(
                "left",
                new ExpeditionNodePosition(
                        "left",
                        100,
                        100),
                "right",
                new ExpeditionNodePosition(
                        "right",
                        120,
                        100));

        assertEquals(
                Optional.of("right"),
                hitTester.findNodeAt(
                        overlapping,
                        117,
                        100,
                        NODE_RADIUS));
    }

    @Test
    void shouldReturnEmptyForEmptyPositionMap() {
        assertTrue(
                hitTester.findNodeAt(
                        Map.of(),
                        100,
                        100,
                        NODE_RADIUS)
                        .isEmpty());
    }

    @Test
    void shouldRejectNullPositionMap() {
        assertThrows(
                NullPointerException.class,
                () -> hitTester.findNodeAt(
                        null,
                        100,
                        100,
                        NODE_RADIUS));
    }

    @Test
    void shouldRejectNonFinitePointerCoordinates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hitTester.findNodeAt(
                        positions,
                        Double.NaN,
                        100,
                        NODE_RADIUS));

        assertThrows(
                IllegalArgumentException.class,
                () -> hitTester.findNodeAt(
                        positions,
                        100,
                        Double.POSITIVE_INFINITY,
                        NODE_RADIUS));
    }

    @Test
    void shouldRejectInvalidRadius() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hitTester.findNodeAt(
                        positions,
                        100,
                        100,
                        0));

        assertThrows(
                IllegalArgumentException.class,
                () -> hitTester.findNodeAt(
                        positions,
                        100,
                        100,
                        Double.NaN));
    }
}