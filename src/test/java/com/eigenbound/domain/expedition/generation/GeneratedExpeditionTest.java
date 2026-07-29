package com.eigenbound.domain.expedition.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionMapTestFixtures;

class GeneratedExpeditionTest {

    @Test
    void shouldStoreGenerationInformation() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        GeneratedExpedition generated = new GeneratedExpedition(
                map,
                2026L,
                3);

        assertEquals(map, generated.map());
        assertEquals(2026L, generated.seed());
        assertEquals(3, generated.difficulty());
    }

    @Test
    void shouldRejectNullMap() {
        assertThrows(
                NullPointerException.class,
                () -> new GeneratedExpedition(
                        null,
                        2026L,
                        3));
    }

    @Test
    void shouldRejectInvalidDifficulty() {
        ExpeditionMap map = ExpeditionMapTestFixtures.validMap();

        assertThrows(
                IllegalArgumentException.class,
                () -> new GeneratedExpedition(
                        map,
                        2026L,
                        0));
    }
}