package com.eigenbound.application.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.expedition.ExpeditionMapTestFixtures;
import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

class GameContextTest {

    private GameContext context;

    @BeforeEach
    void setUp() {
        context = new GameContext();
    }

    @Test
    void shouldStartWithoutActiveExpedition() {
        assertFalse(context.hasActiveExpeditionRun());
        assertTrue(context.activeExpeditionRun().isEmpty());
    }

    @Test
    void shouldStartAndStoreExpeditionRun() {
        ExpeditionRun startedRun = context.startExpedition(
                generatedExpedition(11L));

        assertTrue(context.hasActiveExpeditionRun());

        assertSame(
                startedRun,
                context.activeExpeditionRun().orElseThrow());
    }

    @Test
    void shouldRequireStoredExpeditionRun() {
        ExpeditionRun startedRun = context.startExpedition(
                generatedExpedition(11L));

        assertSame(
                startedRun,
                context.requireActiveExpeditionRun());
    }

    @Test
    void shouldRejectRequiredRunWhenNoneExists() {
        assertThrows(
                IllegalStateException.class,
                context::requireActiveExpeditionRun);
    }

    @Test
    void shouldReplaceActiveRunWhenStartingNewExpedition() {
        ExpeditionRun firstRun = context.startExpedition(
                generatedExpedition(11L));

        ExpeditionRun secondRun = context.startExpedition(
                generatedExpedition(12L));

        assertNotSame(firstRun, secondRun);

        assertSame(
                secondRun,
                context.requireActiveExpeditionRun());
    }

    @Test
    void shouldClearActiveExpeditionRun() {
        context.startExpedition(
                generatedExpedition(11L));

        context.clearExpeditionRun();

        assertFalse(context.hasActiveExpeditionRun());
        assertTrue(context.activeExpeditionRun().isEmpty());
    }

    @Test
    void shouldRejectNullGeneratedExpedition() {
        assertThrows(
                NullPointerException.class,
                () -> context.startExpedition(null));
    }

    private GeneratedExpedition generatedExpedition(
            long seed) {

        return new GeneratedExpedition(
                ExpeditionMapTestFixtures.validMap(),
                seed,
                2);
    }
}