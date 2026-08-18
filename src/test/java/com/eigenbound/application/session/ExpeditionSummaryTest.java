package com.eigenbound.application.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionResources;

class ExpeditionSummaryTest {

    private static final ExpeditionResources VICTORY_RESOURCES = new ExpeditionResources(
            70,
            100,
            25L);

    private static final ExpeditionResources DEFEAT_RESOURCES = new ExpeditionResources(
            0,
            100,
            10L);

    @Test
    void shouldStoreVictorySummary() {
        ExpeditionSummary summary = new ExpeditionSummary(
                ExpeditionRunState.VICTORY,
                73L,
                3,
                VICTORY_RESOURCES,
                4);

        assertEquals(
                ExpeditionRunState.VICTORY,
                summary.outcome());
        assertEquals(73L, summary.seed());
        assertEquals(3, summary.difficulty());
        assertEquals(
                VICTORY_RESOURCES,
                summary.finalResources());
        assertEquals(4, summary.visitedRoomCount());
    }

    @Test
    void shouldStoreDefeatSummary() {
        ExpeditionSummary summary = new ExpeditionSummary(
                ExpeditionRunState.DEFEAT,
                91L,
                2,
                DEFEAT_RESOURCES,
                3);

        assertEquals(
                ExpeditionRunState.DEFEAT,
                summary.outcome());
        assertEquals(
                DEFEAT_RESOURCES,
                summary.finalResources());
    }

    @Test
    void shouldRejectActiveOutcome() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.ACTIVE,
                        73L,
                        3,
                        VICTORY_RESOURCES,
                        2));
    }

    @Test
    void shouldRejectNullOutcome() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionSummary(
                        null,
                        73L,
                        3,
                        VICTORY_RESOURCES,
                        2));
    }

    @Test
    void shouldRejectNullResources() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.VICTORY,
                        73L,
                        3,
                        null,
                        2));
    }

    @Test
    void shouldRejectDifficultyOutsideSupportedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.VICTORY,
                        73L,
                        0,
                        VICTORY_RESOURCES,
                        2));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.VICTORY,
                        73L,
                        6,
                        VICTORY_RESOURCES,
                        2));
    }

    @Test
    void shouldRejectNonPositiveVisitedRoomCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.VICTORY,
                        73L,
                        3,
                        VICTORY_RESOURCES,
                        0));
    }

    @Test
    void shouldRejectVictoryWithoutRemainingStability() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.VICTORY,
                        73L,
                        3,
                        DEFEAT_RESOURCES,
                        2));
    }

    @Test
    void shouldRejectDefeatWithRemainingStability() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionSummary(
                        ExpeditionRunState.DEFEAT,
                        73L,
                        3,
                        VICTORY_RESOURCES,
                        2));
    }
}