package com.eigenbound.domain.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ExpeditionEdgeTest {

    @Test
    void shouldNormalizeEdgeIds() {
        ExpeditionEdge edge = new ExpeditionEdge(
                " start ",
                " challenge ");

        assertEquals("start", edge.sourceId());
        assertEquals(
                "challenge",
                edge.destinationId());
    }

    @Test
    void shouldRejectBlankIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionEdge(
                        "start",
                        " "));
    }

    @Test
    void shouldRejectSelfConnection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionEdge(
                        "room",
                        "room"));
    }
}