package com.eigenbound.domain.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ExpeditionNodeTest {

    @Test
    void shouldNormalizeNodeId() {
        ExpeditionNode node = new ExpeditionNode(
                "  challenge-1  ",
                1,
                RoomType.VECTOR_CHALLENGE,
                2);

        assertEquals("challenge-1", node.id());
    }

    @Test
    void shouldRejectBlankId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionNode(
                        " ",
                        1,
                        RoomType.REWARD,
                        0));
    }

    @Test
    void shouldRejectNegativeLayer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionNode(
                        "room",
                        -1,
                        RoomType.REST,
                        0));
    }

    @Test
    void shouldRejectDifficultyOutsideRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionNode(
                        "room",
                        1,
                        RoomType.VECTOR_CHALLENGE,
                        6));
    }

    @Test
    void startNodeShouldBelongToLayerZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionNode(
                        "start",
                        1,
                        RoomType.START,
                        0));
    }
}