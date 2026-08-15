package com.eigenbound.domain.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ExpeditionResourcesTest {

    @Test
    void shouldCreateInitialResources() {
        ExpeditionResources resources = ExpeditionResources.initial();

        assertEquals(100, resources.stability());
        assertEquals(100, resources.maxStability());
        assertEquals(0L, resources.insight());
    }

    @Test
    void shouldRejectNonPositiveMaximumStability() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionResources(
                        0,
                        0,
                        0L));
    }

    @Test
    void shouldRejectNegativeStability() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionResources(
                        -1,
                        100,
                        0L));
    }

    @Test
    void shouldRejectStabilityAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionResources(
                        101,
                        100,
                        0L));
    }

    @Test
    void shouldRejectNegativeInsight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpeditionResources(
                        100,
                        100,
                        -1L));
    }

    @Test
    void shouldLoseStability() {
        ExpeditionResources original = new ExpeditionResources(
                80,
                100,
                10L);

        ExpeditionResources updated = original.loseStability(25);

        assertEquals(55, updated.stability());
        assertEquals(80, original.stability());
        assertNotSame(original, updated);
    }

    @Test
    void shouldStopStabilityAtZero() {
        ExpeditionResources resources = new ExpeditionResources(
                20,
                100,
                0L);

        ExpeditionResources updated = resources.loseStability(
                Integer.MAX_VALUE);

        assertEquals(0, updated.stability());
        assertTrue(updated.isDepleted());
    }

    @Test
    void shouldRejectNegativeStabilityLoss() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpeditionResources
                        .initial()
                        .loseStability(-1));
    }

    @Test
    void shouldRestoreStability() {
        ExpeditionResources resources = new ExpeditionResources(
                40,
                100,
                0L);

        ExpeditionResources updated = resources.restoreStability(25);

        assertEquals(65, updated.stability());
    }

    @Test
    void shouldStopRestorationAtMaximumWithoutOverflow() {
        ExpeditionResources resources = new ExpeditionResources(
                90,
                100,
                0L);

        ExpeditionResources updated = resources.restoreStability(
                Integer.MAX_VALUE);

        assertEquals(100, updated.stability());
    }

    @Test
    void shouldRejectNegativeStabilityRestoration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpeditionResources
                        .initial()
                        .restoreStability(-1));
    }

    @Test
    void shouldGainInsight() {
        ExpeditionResources resources = new ExpeditionResources(
                100,
                100,
                15L);

        ExpeditionResources updated = resources.gainInsight(10L);

        assertEquals(25L, updated.insight());
        assertEquals(15L, resources.insight());
    }

    @Test
    void shouldRejectInsightOverflow() {
        ExpeditionResources resources = new ExpeditionResources(
                100,
                100,
                Long.MAX_VALUE);

        assertThrows(
                ArithmeticException.class,
                () -> resources.gainInsight(1L));
    }

    @Test
    void shouldRejectNegativeInsightGain() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpeditionResources
                        .initial()
                        .gainInsight(-1L));
    }

    @Test
    void shouldSpendAvailableInsight() {
        ExpeditionResources resources = new ExpeditionResources(
                100,
                100,
                30L);

        ExpeditionResources updated = resources.spendInsight(12L);

        assertEquals(18L, updated.insight());
    }

    @Test
    void shouldRejectSpendingMoreInsightThanAvailable() {
        ExpeditionResources resources = new ExpeditionResources(
                100,
                100,
                10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> resources.spendInsight(11L));
    }

    @Test
    void shouldRejectNegativeInsightCost() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpeditionResources
                        .initial()
                        .spendInsight(-1L));
    }

    @Test
    void shouldIdentifyAvailableStability() {
        assertFalse(ExpeditionResources.initial().isDepleted());
    }
}