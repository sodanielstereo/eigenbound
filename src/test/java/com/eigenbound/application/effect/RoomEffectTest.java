package com.eigenbound.application.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class RoomEffectTest {

    @Test
    void shouldCreateEmptyEffect() {
        RoomEffect effect = RoomEffect.none();

        assertEquals(0, effect.stabilityRestored());
        assertEquals(0, effect.stabilityLost());
        assertEquals(0L, effect.insightGained());
        assertFalse(effect.changesResources());
    }

    @Test
    void shouldCreateStabilityRestorationEffect() {
        RoomEffect effect = RoomEffect.restoreStability(25);

        assertEquals(25, effect.stabilityRestored());
        assertEquals(0, effect.stabilityLost());
        assertEquals(0L, effect.insightGained());
        assertTrue(effect.changesResources());
    }

    @Test
    void shouldCreateStabilityLossEffect() {
        RoomEffect effect = RoomEffect.loseStability(11);

        assertEquals(0, effect.stabilityRestored());
        assertEquals(11, effect.stabilityLost());
        assertEquals(0L, effect.insightGained());
        assertTrue(effect.changesResources());
    }

    @Test
    void shouldCreateInsightGainEffect() {
        RoomEffect effect = RoomEffect.gainInsight(40L);

        assertEquals(0, effect.stabilityRestored());
        assertEquals(0, effect.stabilityLost());
        assertEquals(40L, effect.insightGained());
        assertTrue(effect.changesResources());
    }

    @Test
    void shouldRejectNegativeRestoration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoomEffect(
                        -1,
                        0,
                        0L));
    }

    @Test
    void shouldRejectNegativeLoss() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoomEffect(
                        0,
                        -1,
                        0L));
    }

    @Test
    void shouldRejectNegativeInsight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoomEffect(
                        0,
                        0,
                        -1L));
    }

    @Test
    void shouldRejectSimultaneousRestorationAndLoss() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RoomEffect(
                        10,
                        5,
                        0L));
    }

    @Test
    void shouldRejectNonPositiveFactoryAmounts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RoomEffect.restoreStability(0));
        assertThrows(
                IllegalArgumentException.class,
                () -> RoomEffect.loseStability(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> RoomEffect.gainInsight(0L));
    }
}